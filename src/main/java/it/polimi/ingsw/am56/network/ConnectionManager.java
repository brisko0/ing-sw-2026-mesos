package it.polimi.ingsw.am56.network;

import it.polimi.ingsw.am56.core.ConnectionFactory;
import it.polimi.ingsw.am56.core.ServerAdapter;
import it.polimi.ingsw.am56.core.UpdateHandler;
import it.polimi.ingsw.am56.network.message.request.ClientRequest;
import it.polimi.ingsw.am56.network.message.update.ServerMessage;

public class ConnectionManager implements ServerAdapter, UpdateHandler {

    private static final int RETRY_DELAY_MS = 2000;

    private final String ip;
    private final int port;
    private final boolean useRmi;
    private final ConnectionFactory factory;

    private ServerAdapter currentAdapter;
    private UpdateHandler activeHandler;

    private volatile boolean reconnecting = false;

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

    @Override
    public void sendRequest(ClientRequest request) {
        if (currentAdapter != null) {
            currentAdapter.sendRequest(request);
        }
    }

    @Override
    public void setUpdateHandler(UpdateHandler handler) {
        this.activeHandler = handler;
    }

    // -------------------------------------------------------------------------
    // UpdateHandler — intercetta i messaggi dal SocketClient/RmiClient sottostante
    // -------------------------------------------------------------------------

    @Override
    public void handleUpdate(ServerMessage message) {
        if (activeHandler != null) {
            activeHandler.handleUpdate(message);
        }
    }

    @Override
    public void handleError(String message) {
        if (activeHandler != null) {
            activeHandler.handleError(message);
        }
    }

    @Override
    public void handleDisconnect() {
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