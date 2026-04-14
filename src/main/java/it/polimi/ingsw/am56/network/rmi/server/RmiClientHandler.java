package it.polimi.ingsw.am56.network.rmi.server;

import it.polimi.ingsw.am56.model.GameModel; // <-- Importante
import it.polimi.ingsw.am56.model.GameObserver;
import it.polimi.ingsw.am56.model.state.GameEvent;
import it.polimi.ingsw.am56.network.message.update.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RmiClientHandler implements GameObserver, Runnable {
    private final VirtualViewRmi clientStub;
    private final GameModel model; // <-- Riferimento al modello
    private final BlockingQueue<Runnable> taskQueue;
    private boolean active = true;

    // Aggiorna il costruttore
    public RmiClientHandler(VirtualViewRmi clientStub, GameModel model) {
        this.clientStub = clientStub;
        this.model = model;
        this.taskQueue = new LinkedBlockingQueue<>();
    }

    @Override
    public void onModelChanged(GameEvent event) {

        // 2. Creiamo il Messaggio (DTO) estraendo SOLO i dati sicuri e necessari per quell'evento
        ServerMessage msg = switch (event) {

            case PLAYER_ADDED ->
                    new PlayerAddedMessage(); // Magari potresti passargli model.getPlayers().size() per sapere quanti sono!

            case STATE_CHANGED ->
                    new StateChangedMessage(model.getState().getClass().getSimpleName());

            case GAME_STARTED ->
                    new GameStartedMessage(model.getPlayers().size());

            case TURN_CHANGED ->
                    new TurnChangedMessage(model.getCurrentPlayer().getNickname());

            case ROUND_ENDED ->
                    new RoundEndedMessage(model.getCurrentRound());

            case CARD_PICKED ->
                    new CardPickedMessage(model.getCurrentPlayer().getNickname(), model.getState().getClass().getSimpleName());

            // Per i messaggi complessi apriamo un blocco e usiamo "yield"
            case TOTEM_PLACED ->
                    new TotemPlacedMessage(model.getCurrentPlayer().getNickname(),
                            model.getPlayerToOfferTile().get(model.getCurrentPlayer()).getLetter());

            case TOTEM_RESET ->
                    new TotemResetMessage(model.getCurrentPlayer().getNickname(),
                            model.getPlayerToOrderTile().get(model.getCurrentPlayer()).getOrderTileId());

            // Messaggio di default ultra-sicuro (non chiama metodi su oggetti potenzialmente nulli)
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