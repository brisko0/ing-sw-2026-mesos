package it.polimi.ingsw.am56.model;

import it.polimi.ingsw.am56.model.state.GameEvent;

/**
 * Observer of the game model. Implementations register on the {@link GameModel} and are notified
 * whenever it changes, so they can react to the specific {@link GameEvent} that occurred (e.g. a
 * virtual view pushing the matching update to its client, or the end-game service computing results).
 */
public interface GameObserver {
    /**
     * Called by the model when a relevant change occurs, carrying the event that describes it.
     *
     * @param event the event signalling what changed
     */
    void onModelChanged(GameEvent event);
}