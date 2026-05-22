package it.polimi.ingsw.am56.network.rmi.server;

import it.polimi.ingsw.am56.controller.GameController;
import it.polimi.ingsw.am56.network.message.update.ServerMessage;
import it.polimi.ingsw.am56.network.ClientConnection;

import java.rmi.RemoteException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RmiClientHandler implements ClientConnection, Runnable {
    private final VirtualViewRmi clientStub;
    private final GameController controller;
    private final BlockingQueue<Runnable> taskQueue;
    private volatile boolean active = true;

    private static final int HEARTBEAT_INTERVAL_MS = 3000;
    private static final int PING_TIMEOUT_MS = 2000;

    public RmiClientHandler(VirtualViewRmi clientStub, GameController controller) {
        this.clientStub = clientStub;
        this.controller = controller;
        this.taskQueue = new LinkedBlockingQueue<>();
        startHeartbeat();
    }

    @Override
    public void sendMessage(ServerMessage message) {
        taskQueue.add(() -> {
            try {
                clientStub.showUpdate(message);
            } catch (RemoteException e) {
                System.err.println("[RMI CONNECTION] Errore di invio, disconnessione client...");
                disconnect();
            }
        });
    }

    @Override
    public void sendError(String errorDetails) {
        taskQueue.add(() -> {
            try {
                clientStub.reportError(errorDetails);
            } catch (RemoteException e) {
                disconnect();
            }
        });
    }

    @Override
    public void disconnect() {
        if (!active) return;
        this.active = false;
        controller.handleDisconnect();
    }

    @Override
    public void run() {
        while (active) {
            try {
                Runnable task = taskQueue.take();
                task.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                active = false;
            }
        }
    }

    private void startHeartbeat() {
        Thread t = new Thread(() -> {
            while (active) {
                try {
                    Thread.sleep(HEARTBEAT_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                if (!active) return;

                Thread pingThread = new Thread(() -> {
                    try {
                        clientStub.ping();
                    } catch (RemoteException e) {
                        System.err.println("[RMI CONNECTION] Heartbeat fallito, disconnessione client...");
                        disconnect();
                    }
                }, "rmi-server-ping");
                pingThread.setDaemon(true);
                pingThread.start();

                try {
                    pingThread.join(PING_TIMEOUT_MS);
                    if (pingThread.isAlive()) {
                        pingThread.interrupt();
                        System.err.println("[RMI CONNECTION] Heartbeat in timeout, disconnessione client...");
                        disconnect();
                        return;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "rmi-server-heartbeat");
        t.setDaemon(true);
        t.start();
    }
}
