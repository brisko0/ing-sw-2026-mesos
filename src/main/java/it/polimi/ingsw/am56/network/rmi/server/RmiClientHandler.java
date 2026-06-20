package it.polimi.ingsw.am56.network.rmi.server;

import it.polimi.ingsw.am56.controller.GameController;
import it.polimi.ingsw.am56.network.message.update.ServerMessage;
import it.polimi.ingsw.am56.network.ClientConnection;

import java.rmi.RemoteException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Server-side {@link ClientConnection} for one RMI client, wrapping that client's remote
 * {@link VirtualViewRmi} stub. Outgoing updates and errors are not sent inline: they are enqueued on a
 * blocking task queue drained by this handler's own thread (it is also {@link Runnable}), so that
 * remote callbacks to a single client are serialized and never block the caller.
 * <p>
 * It also runs a heartbeat that periodically pings the client stub; if a ping fails or times out the
 * client is disconnected. A disconnection (from a failed send or the heartbeat) is reported once to the
 * {@link GameController}, which decides whether to abort the game.
 */
public class RmiClientHandler implements ClientConnection, Runnable {
    private final VirtualViewRmi clientStub;
    private final GameController controller;
    private final BlockingQueue<Runnable> taskQueue;
    private volatile boolean active = true;

    private static final int HEARTBEAT_INTERVAL_MS = 3000;
    private static final int PING_TIMEOUT_MS = 2000;

    /**
     * Creates a handler bound to one client stub and starts its heartbeat.
     *
     * @param clientStub the remote view stub of the client this handler serves
     * @param controller the game controller notified on disconnection
     */
    public RmiClientHandler(VirtualViewRmi clientStub, GameController controller) {
        this.clientStub = clientStub;
        this.controller = controller;
        this.taskQueue = new LinkedBlockingQueue<>();
        startHeartbeat();
    }

    /**
     * Enqueues an update to be pushed to the client by the handler thread; a failed remote call
     * disconnects the client.
     *
     * @param message the update to deliver
     */
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

    /**
     * Enqueues an error notification to be sent to the client by the handler thread; a failed remote
     * call disconnects the client.
     *
     * @param errorDetails a human-readable description of the error
     */
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

    /**
     * Disconnects this client (once): stops the handler and notifies the controller, which decides
     * whether to abort the game.
     */
    @Override
    public void disconnect() {
        if (!active) return;
        this.active = false;
        controller.handleDisconnect();
    }

    /**
     * Handler loop: takes queued tasks (outgoing updates/errors) and runs them one at a time until the
     * connection is closed.
     */
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

    /**
     * Starts the daemon heartbeat thread that pings the client on a fixed interval and disconnects it
     * if a ping throws or does not return within the timeout.
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
