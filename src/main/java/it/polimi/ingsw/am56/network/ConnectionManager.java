package it.polimi.ingsw.am56.network;

import it.polimi.ingsw.am56.core.ConnectionFactory;
import it.polimi.ingsw.am56.core.ServerAdapter;
import it.polimi.ingsw.am56.core.UpdateHandler;
import it.polimi.ingsw.am56.network.message.request.ClientRequest;
import it.polimi.ingsw.am56.network.message.update.GameAbortedMessage;
import it.polimi.ingsw.am56.network.message.update.GameEndedMessage;
import it.polimi.ingsw.am56.network.message.update.MessageVisitor;
import it.polimi.ingsw.am56.network.message.update.ServerMessage;

/**
 * Client-side wrapper around the active server connection that adds transparent automatic
 * reconnection. It implements both {@link ServerAdapter} (so the client code keeps sending requests
 * through it as if it were the real adapter) and {@link UpdateHandler} (so it intercepts the updates,
 * errors and disconnections coming from the underlying {@link RmiClient}/{@code SocketClient}).
 * <p>
 * Normal traffic is simply forwarded: requests go to the current adapter, while updates and errors are
 * relayed to the registered downstream handler. On a disconnection it notifies the downstream handler
 * and then spins up a background thread that keeps recreating the connection (via the
 * {@link ConnectionFactory}, every {@code RETRY_DELAY_MS}) until it succeeds, after which it swaps in
 * the new adapter and signals reconnection. The {@code reconnecting} flag guards against starting more
 * than one reconnection loop at a time.
 * <p>
 * Reconnection is skipped after a terminal message: when a {@code GameAbortedMessage} or
 * {@code GameEndedMessage} is seen, the game is over, so the connection drop that follows (e.g. the
 * server shutting down on an abort) is expected and must not trigger a retry loop.
 */
public class ConnectionManager implements ServerAdapter, UpdateHandler {

    private static final int RETRY_DELAY_MS = 2000;

    private final String ip;
    private final int port;
    private final boolean useRmi;
    private final ConnectionFactory factory;

    private ServerAdapter currentAdapter;
    private UpdateHandler activeHandler;

    private volatile boolean reconnecting = false;

    private volatile boolean terminated = false;

    /**
     * Visitor used by {@link #handleUpdate(ServerMessage)} to detect terminal notifications without
     * switching on the message type: it raises the {@link #terminated} flag on a
     * {@link GameAbortedMessage} or {@link GameEndedMessage} and ignores every other message.
     */
    private final MessageVisitor terminalDetector = new MessageVisitor() {
        @Override
        public void visit(GameAbortedMessage message) {
            terminated = true;
        }
        @Override
        public void visit(GameEndedMessage message) {
            terminated = true;
        }
    };

    /**
     * Creates a connection manager wrapping an already established adapter and registers itself as
     * that adapter's update handler, so it can intercept incoming traffic and drive reconnection.
     *
     * @param ip             the server host, used when recreating the connection
     * @param port           the server port, used when recreating the connection
     * @param useRmi         {@code true} to reconnect over RMI, {@code false} to reconnect over socket
     * @param factory        the factory used to create new connections on reconnection
     * @param initialAdapter the adapter for the currently open connection
     */
    public ConnectionManager(String ip, int port, boolean useRmi,
                             ConnectionFactory factory, ServerAdapter initialAdapter) {
        this.ip = ip;
        this.port = port;
        this.useRmi = useRmi;
        this.factory = factory;
        this.currentAdapter = initialAdapter;
        this.currentAdapter.setUpdateHandler(this);
    }

    // -------------------------------------------------------------------------
    // ServerAdapter — i controller chiamano questi metodi normalmente
    // -------------------------------------------------------------------------

    /**
     * Forwards a client request to the current underlying adapter (no-op if none is set).
     *
     * @param request the request to send to the server
     */
    @Override
    public void sendRequest(ClientRequest request) {
        if (currentAdapter != null) {
            currentAdapter.sendRequest(request);
        }
    }

    /**
     * Registers the downstream handler that should receive the updates, errors and connection events
     * relayed by this manager.
     *
     * @param handler the downstream update handler
     */
    @Override
    public void setUpdateHandler(UpdateHandler handler) {
        this.activeHandler = handler;
    }

    // -------------------------------------------------------------------------
    // UpdateHandler — intercetta i messaggi dal SocketClient/RmiClient sottostante
    // -------------------------------------------------------------------------

    /**
     * Relays an update received from the underlying connection to the downstream handler. Before
     * relaying, the message is inspected for a terminal notification (game aborted or ended): if it
     * is one, reconnection is disabled so the connection drop that follows is not retried.
     *
     * @param message the update received from the server
     */
    @Override
    public void handleUpdate(ServerMessage message) {
        message.accept(terminalDetector);
        if (activeHandler != null) {
            activeHandler.handleUpdate(message);
        }
    }

    /**
     * Relays an error received from the underlying connection to the downstream handler.
     *
     * @param message the error message received from the server
     */
    @Override
    public void handleError(String message) {
        if (activeHandler != null) {
            activeHandler.handleError(message);
        }
    }

    /**
     * Handles the loss of the underlying connection by notifying the downstream handler and starting a
     * background loop that retries the connection (over RMI or socket, per {@code useRmi}) every
     * {@code RETRY_DELAY_MS} until it succeeds; on success the new adapter is installed and the
     * downstream handler is told it has reconnected. Re-entrant calls while already reconnecting are
     * ignored.
     * <p>
     * If a terminal message (game aborted or ended) has already been received, the drop is expected
     * and this call returns immediately without reconnecting: there is nothing left to come back to.
     */
    @Override
    public void handleDisconnect() {
        if (terminated) return;
        if (reconnecting) return;
        reconnecting = true;

        System.out.println("[CONNECTION MANAGER] Server disconnesso. Riconnessione ogni "
                + RETRY_DELAY_MS + "ms...");

        if (activeHandler != null) {
            activeHandler.handleDisconnect();
        }

        new Thread(() -> {
            while (reconnecting) {
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                    ServerAdapter newAdapter = useRmi
                            ? factory.createRmiConnection(ip, port)
                            : factory.createSocketConnection(ip, port);
                    newAdapter.setUpdateHandler(this);
                    this.currentAdapter = newAdapter;
                    this.reconnecting = false;
                    System.out.println("[CONNECTION MANAGER] Riconnesso al server!");
                    if (activeHandler != null) {
                        activeHandler.handleReconnected();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (Exception ignored) {
                    System.out.println("[CONNECTION MANAGER] Server non raggiungibile, riprovo...");
                }
            }
        }, "reconnect-thread").start();
    }
}