package it.polimi.ingsw.am56.network.rmi.server;

import it.polimi.ingsw.am56.network.ClientConnection;
import it.polimi.ingsw.am56.network.message.update.*;

import java.rmi.RemoteException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RmiClientHandler implements ClientConnection, Runnable {
    private final VirtualViewRmi clientStub;
    private final BlockingQueue<Runnable> taskQueue;
    private boolean active = true;

    public RmiClientHandler(VirtualViewRmi clientStub) {
        this.clientStub = clientStub;
        this.taskQueue = new LinkedBlockingQueue<>();
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
        this.active = false;
        // da avvisare il server che il client si è disconesso
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
}