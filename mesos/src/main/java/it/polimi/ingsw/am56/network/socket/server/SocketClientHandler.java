package it.polimi.ingsw.am56.network.socket.server;

import it.polimi.ingsw.am56.controller.GameController;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;
import it.polimi.ingsw.am56.network.message.update.ErrorMessage;
import it.polimi.ingsw.am56.network.message.update.PingMessage;
import it.polimi.ingsw.am56.network.message.update.ServerMessage;
import it.polimi.ingsw.am56.network.message.request.ClientRequest;
import it.polimi.ingsw.am56.network.ClientConnection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Server-side {@link ClientConnection} for one socket client. As a {@link Runnable} it runs the read
 * loop that deserializes incoming {@link ClientRequest}s and executes them on the
 * {@link GameController} (reporting any {@link InvalidActionException} back to the client), while its
 * {@link ClientConnection} methods serialize outgoing updates and errors back over the socket.
 * <p>
 * Writes are synchronized on the output stream so the read loop and the observer pushing updates do not
 * interleave on the wire. To detect a half-open connection (network drop with no FIN/RST) it arms a read
 * timeout and runs a heartbeat that pings the client; a read/write failure or a read timeout disconnects
 * the client: the socket is closed and the controller is notified so it can decide whether to abort the
 * game. This mirrors the RMI heartbeat.
 */
public class SocketClientHandler implements Runnable, ClientConnection {
    private final Socket socket;
    private final GameController controller;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private volatile boolean active = true;

    private static final int HEARTBEAT_INTERVAL_MS = 2000;
    private static final int READ_TIMEOUT_MS = 6000;

    /**
     * Creates a handler for an accepted client socket, sets up its object streams, arms the read
     * timeout used to detect a half-open connection and starts the heartbeat toward the client.
     *
     * @param socket     the accepted client socket
     * @param controller the game controller that executes the client's requests
     * @throws IOException if the socket streams cannot be opened
     */
    public SocketClientHandler(Socket socket, GameController controller) throws IOException {
        this.socket = socket;
        this.controller = controller;

        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());

        // Se entro READ_TIMEOUT_MS non arriva nulla (nemmeno un ping del client), readObject lancia
        // SocketTimeoutException: così una connessione half-open viene rilevata invece di restare appesa.
        socket.setSoTimeout(READ_TIMEOUT_MS);
        startHeartbeat();
    }

    /**
     * Starts the daemon thread that pushes a {@link PingMessage} to the client on a fixed interval, so
     * the client's own read timeout can detect a half-open connection toward this server.
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
                if (active) sendMessage(new PingMessage());
            }
        }, "socket-server-heartbeat");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Read loop: deserializes client requests from the socket and executes each on the controller,
     * sending an error back to the client if a request is invalid. A connection failure disconnects
     * the client.
     */
    @Override
    public void run() {
        try {
            while (active) {
                ClientRequest request = (ClientRequest) in.readObject();

                try {
                    request.executeOnServer(controller);
                } catch (InvalidActionException e) {
                    this.sendError(e.getMessage());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[SOCKET SERVER] Connessione chiusa col client o classe non trovata.");
            disconnect();
        }
    }


    /**
     * Serializes and sends an update to the client; writes are synchronized on the output stream. A
     * write failure disconnects the client.
     *
     * @param message the update to deliver
     */
    @Override
    public void sendMessage(ServerMessage message) {
        try {
            synchronized (out) {
                out.writeObject(message);
                out.reset();
                out.flush();
            }
        } catch (IOException e) {
            disconnect();
        }
    }

    /**
     * Sends an error to the client by wrapping it in an {@link ErrorMessage} and delivering it like any
     * other update.
     *
     * @param errorDetails a human-readable description of the error
     */
    @Override
    public void sendError(String errorDetails) {
        System.err.println("[SOCKET SERVER] Inviato errore al client: " + errorDetails);
        sendMessage(new ErrorMessage(errorDetails));
    }

    /**
     * Disconnects this client (once): stops the loop, closes the socket and notifies the controller,
     * which decides whether to abort the game.
     */
    @Override
    public void disconnect() {
        if (!active) return;
        this.active = false;
        try {
            socket.close();
        } catch (IOException ignored) {}
        controller.handleDisconnect();
    }
}