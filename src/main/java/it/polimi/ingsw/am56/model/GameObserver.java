package it.polimi.ingsw.am56.model;

import it.polimi.ingsw.am56.model.state.GameEvent;

public interface GameObserver {
    void onModelChanged(GameEvent event);
}