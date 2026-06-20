package it.polimi.ingsw.am56.network.socket.client;

import it.polimi.ingsw.am56.core.ServerAdapter;
import it.polimi.ingsw.am56.core.UpdateHandler;

import it.polimi.ingsw.am56.network.message.request.ClientRequest;
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
 * If reading fails (the connection is lost), the listener closes the socket and notifies the update
 * handler of the disconnection, so the {@code ConnectionManager} can attempt to reconnect.
 */
// implementa ServerAdapter invece di NetworkClient
public class SocketClient implements ServerAdapter {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    private UpdateHandler updateHandler;

    /**
     * Opens a socket connection to the server, sets up the object streams and starts the listener
     * thread.
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

        startListening();
    }

    /**
     * Registers the handler that receives the updates and disconnection events read from the socket.
     *
     * @param handler the update handler
     */
    // metodo per registrare la GUI
    @Override
    public void setUpdateHandler(UpdateHandler handler) {
        this.updateHandler = handler;
    }

    /**
     * Serializes and sends a request to the server over the socket. A failure is logged.
     *
     * @param request the request to send
     */
    // deve accettare solo ClientRequests non object
    @Override
    public void sendRequest(ClientRequest request) {
        try {
            out.writeObject(request);
            out.reset();
            out.flush();
        } catch (IOException e) {
            System.err.println("Impossibile inviare la richiesta.");
        }
    }

    /**
     * Starts the background thread that continuously reads server messages from the socket and
     * forwards them to the update handler; on a read failure it closes the socket and reports the
     * disconnection.
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
                System.err.println("[SOCKET] Connessione persa: " + e.getMessage());
                // Chiude il socket (e a cascata gli stream in/out) per non lasciarlo appeso.
                try {
                    socket.close();
                } catch (IOException ignored) {}
                if (updateHandler != null) {
                    updateHandler.handleDisconnect();
                }
            }
        }).start();
    }
}