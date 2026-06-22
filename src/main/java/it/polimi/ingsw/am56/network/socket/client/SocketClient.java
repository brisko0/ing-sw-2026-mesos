package it.polimi.ingsw.am56.network.socket.client;

import it.polimi.ingsw.am56.core.ServerAdapter;
import it.polimi.ingsw.am56.core.UpdateHandler;

import it.polimi.ingsw.am56.network.message.request.ClientRequest;
import it.polimi.ingsw.am56.network.message.request.PingRequest;
import it.polimi.ingsw.am56.network.message.update.ServerMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Client-side socket endpoint and {@link ServerAdapter}. It opens a socket to the server, serializes
 * outgoing {@link ClientRequest}s over an {@link ObjectOutputStream} and runs a background listener
 * thread that reads incoming {@link ServerMessage}s and forwards them to the registered
 * {@link UpdateHandler}.
 * <p>
 * To detect a half-open connection (network drop with no FIN/RST) it arms a read timeout and runs a
 * heartbeat that pings the server, mirroring the RMI heartbeat. If reading fails or times out (the
 * connection is lost), the listener closes the socket and notifies the update handler of the
 * disconnection, so the {@code ConnectionManager} can attempt to reconnect.
 */
public class SocketClient implements ServerAdapter {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    private UpdateHandler updateHandler;
    private volatile boolean active = true;

    private static final int HEARTBEAT_INTERVAL_MS = 2000;
    private static final int READ_TIMEOUT_MS = 6000;

    /**
     * Opens a socket connection to the server, sets up the object streams, arms the read timeout used
     * to detect a half-open connection, and starts the listener and heartbeat threads.
     *
     * @param host the server host
     * @param port the server's socket port
     * @throws IOException if the socket or its streams cannot be opened
     */
    public SocketClient(String host, int port) throws IOException {
        this.socket = new Socket(host, port);

        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());

        socket.setSoTimeout(READ_TIMEOUT_MS);

        startListening();
        startHeartbeat();
    }

    /**
     * Registers the handler that receives the updates and disconnection events read from the socket.
     *
     * @param handler the update handler
     */
    @Override
    public void setUpdateHandler(UpdateHandler handler) {
        this.updateHandler = handler;
    }

    /**
     * Serializes and sends a request to the server over the socket. A send failure is swallowed
     * here: the loss of connection is detected and reported by the listener thread (which then
     * drives the reconnection), so logging it would only spam the client console.
     *
     * @param request the request to send
     */
    @Override
    public void sendRequest(ClientRequest request) {
        try {
            synchronized (out) {
                out.writeObject(request);
                out.reset();
                out.flush();
            }
        } catch (IOException e) {
        }
    }

    /**
     * Starts the background thread that continuously reads server messages from the socket and
     * forwards them to the update handler; on a read failure or timeout it closes the socket and
     * reports the disconnection.
     */
    private void startListening() {
        new Thread(() -> {
            try {
                while (true) {
                    ServerMessage msg = (ServerMessage) in.readObject();
                    if (updateHandler != null) {
                        updateHandler.handleUpdate(msg);
                    } else {
                        System.out.println("[RETE] Messaggio ricevuto, ma nessuna View registrata: " + msg);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                active = false;
                try {
                    socket.close();
                } catch (IOException ignored) {}
                if (updateHandler != null) {
                    updateHandler.handleDisconnect();
                }
            }
        }).start();
    }

    /**
     * Starts the daemon thread that sends a {@link PingRequest} to the server on a fixed interval, so
     * the server's read timeout can detect a half-open connection from this client.
     */
    private void startHeartbeat() {
        Thread t = new Thread(() -> {
            while (active) {
                try {
                    Thread.sleep(HEARTBEAT_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                if (active) sendRequest(new PingRequest());
            }
        }, "socket-client-heartbeat");
        t.setDaemon(true);
        t.start();
    }
}