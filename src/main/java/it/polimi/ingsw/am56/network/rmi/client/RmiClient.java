package it.polimi.ingsw.am56.network.rmi.client;

import it.polimi.ingsw.am56.core.ServerAdapter;
import it.polimi.ingsw.am56.core.UpdateHandler;

import it.polimi.ingsw.am56.network.message.update.ServerMessage;
import it.polimi.ingsw.am56.network.message.request.ClientRequest;
import it.polimi.ingsw.am56.network.rmi.server.VirtualViewRmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Client-side RMI endpoint. It plays a double role: as a {@link ServerAdapter} it lets the client send
 * requests to the server, and as an exported {@link VirtualViewRmi} it is the remote callback the
 * server invokes to push updates and errors back. Incoming calls are forwarded to the registered
 * {@link UpdateHandler}.
 * <p>
 * It also runs a heartbeat: a daemon thread periodically pings the server stub and, if a ping fails or
 * times out, marks the connection dead and notifies the update handler of the disconnection (so the
 * {@code ConnectionManager} can attempt to reconnect).
 */
public class RmiClient extends UnicastRemoteObject implements VirtualViewRmi, ServerAdapter {

    private VirtualServerRmi serverStub;
    private UpdateHandler updateHandler;

    private volatile boolean heartbeatActive = true;
    private static final int HEARTBEAT_INTERVAL_MS = 3000;
    private static final int PING_TIMEOUT_MS = 2000;

    /**
     * Creates and exports the RMI client object.
     *
     * @throws RemoteException if the object cannot be exported
     */
    public RmiClient() throws RemoteException {
        super();
    }

    /**
     * Sets the remote server stub this client talks to and starts the heartbeat against it.
     *
     * @param serverStub the remote server stub
     */
    public void setServerStub(VirtualServerRmi serverStub) {
        this.serverStub = serverStub;
        startHeartbeat();
    }

    /**
     * Starts the daemon heartbeat thread that pings the server on a fixed interval and triggers a
     * disconnection if a ping throws or does not return within the timeout.
     */
    private void startHeartbeat() {
        Thread t = new Thread(() -> {
            while (heartbeatActive) {
                try {
                    Thread.sleep(HEARTBEAT_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                if (!heartbeatActive) return;

                Thread pingThread = new Thread(() -> {
                    try {
                        serverStub.ping();
                    } catch (RemoteException e) {
                        triggerDisconnect();
                    }
                }, "rmi-ping");
                pingThread.setDaemon(true);
                pingThread.start();

                try {
                    pingThread.join(PING_TIMEOUT_MS);
                    if (pingThread.isAlive()) {
                        pingThread.interrupt();
                        triggerDisconnect();
                        return;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "rmi-heartbeat");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Marks the connection as dead (stopping the heartbeat) and notifies the update handler of the
     * disconnection, once.
     */
    private void triggerDisconnect() {
        if (!heartbeatActive) return;
        heartbeatActive = false;
        System.err.println("[RMI CLIENT] Server non raggiungibile (heartbeat).");
        if (updateHandler != null) updateHandler.handleDisconnect();
    }

    /**
     * Registers the handler that receives the updates, errors and disconnection events relayed by this
     * client.
     *
     * @param handler the update handler
     */
    @Override
    public void setUpdateHandler(UpdateHandler handler) {
        this.updateHandler = handler;
    }

    /**
     * Sends a request to the server through the remote stub. If the remote call fails, the connection
     * is marked dead and the update handler is notified of the disconnection.
     *
     * @param request the request to send to the server
     */
    @Override
    public void sendRequest(ClientRequest request) {
        try {
            if (serverStub != null)
                serverStub.sendRequest(request, this);
        } catch (RemoteException e) {
            heartbeatActive = false;
            System.err.println("[CLIENT] Impossibile inviare la richiesta al server.");
            if (updateHandler != null)
                updateHandler.handleDisconnect();
        }
    }

    /**
     * Remote callback invoked by the server to deliver an update; forwards it to the update handler,
     * or logs it if no handler is attached.
     *
     * @param message the update pushed by the server
     * @throws RemoteException if the remote call fails
     */
    @Override
    public void showUpdate(ServerMessage message) throws RemoteException {
        if (updateHandler != null) {
            updateHandler.handleUpdate(message);
        } else {
            System.out.println("[RMI CLIENT] Messaggio ricevuto ma nessuna View collegata: " + message);
        }
    }

    /**
     * Remote callback invoked by the server to report an error; forwards it to the update handler, or
     * logs it if no handler is attached.
     *
     * @param details a human-readable description of the error
     * @throws RemoteException if the remote call fails
     */
    @Override
    public void reportError(String details) throws RemoteException {
        if (updateHandler != null) {
            updateHandler.handleError(details);
        } else {
            System.err.println("[CLIENT] Errore dal server: " + details);
        }
    }

    /**
     * No-op remote method the server pings to verify this client is still alive.
     *
     * @throws RemoteException if the remote call fails
     */
    @Override
    public void ping() throws RemoteException {
        // no-op: solo per verificare che il client sia vivo
    }
}