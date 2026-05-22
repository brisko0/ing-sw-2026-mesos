package it.polimi.ingsw.am56.network.rmi.client;

import it.polimi.ingsw.am56.core.ServerAdapter;
import it.polimi.ingsw.am56.core.UpdateHandler;

import it.polimi.ingsw.am56.network.message.update.ServerMessage;
import it.polimi.ingsw.am56.network.message.request.ClientRequest;
import it.polimi.ingsw.am56.network.rmi.server.VirtualViewRmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RmiClient extends UnicastRemoteObject implements VirtualViewRmi, ServerAdapter {

    private VirtualServerRmi serverStub;
    private UpdateHandler updateHandler;

    private volatile boolean heartbeatActive = true;
    private static final int HEARTBEAT_INTERVAL_MS = 3000;
    private static final int PING_TIMEOUT_MS = 2000;

    public RmiClient() throws RemoteException {
        super();
    }

    public void setServerStub(VirtualServerRmi serverStub) {
        this.serverStub = serverStub;
        startHeartbeat();
    }

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

    private void triggerDisconnect() {
        if (!heartbeatActive) return;
        heartbeatActive = false;
        System.err.println("[RMI CLIENT] Server non raggiungibile (heartbeat).");
        if (updateHandler != null) updateHandler.handleDisconnect();
    }

    @Override
    public void setUpdateHandler(UpdateHandler handler) {
        this.updateHandler = handler;
    }

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

    @Override
    public void showUpdate(ServerMessage message) throws RemoteException {
        if (updateHandler != null) {
            updateHandler.handleUpdate(message);
        } else {
            System.out.println("[RMI CLIENT] Messaggio ricevuto ma nessuna View collegata: " + message);
        }
    }

    @Override
    public void reportError(String details) throws RemoteException {
        // da aggiungere un metodo handleError(String msg) nell'UpdateHandler
        // per mostrare popup di errore nella GUI
        System.err.println("[CLIENT] Errore dal server: " + details);
    }
}