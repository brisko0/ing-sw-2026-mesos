package it.polimi.ingsw.am56.network.rmi.server;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.GameObserver;
import it.polimi.ingsw.am56.model.GameEvent;
import it.polimi.ingsw.am56.network.message.update.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RmiClientHandler implements GameObserver, Runnable {
    private final VirtualViewRmi clientStub;
    private final GameModel model;
    private final BlockingQueue<Runnable> taskQueue;
    private boolean active = true;

    public RmiClientHandler(VirtualViewRmi clientStub, GameModel model) {
        this.clientStub = clientStub;
        this.model = model;
        this.taskQueue = new LinkedBlockingQueue<>();
    }

    @Override
    public void onModelChanged(GameEvent event) {

        // 2. Creiamo il Messaggio (DTO) estraendo SOLO i dati sicuri e necessari per quell'evento
        ServerMessage msg = switch (event) {

            default ->
                    new GameStateMessage();
        };

        // 3. Mettiamo l'invio in coda
        taskQueue.add(() -> {
            try {
                clientStub.showUpdate(msg);
            } catch (Exception e) {
                System.err.println("[HANDLER] Errore di invio, disconnessione client...");
                active = false;
            }
        });
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