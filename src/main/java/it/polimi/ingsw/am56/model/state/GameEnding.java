package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;

public class GameEnding extends GameState{

    public GameEnding(GameModel model) {
        super(model);
    }

    @Override
    public void onEntry() {
        model.endGameActions();
    }

    @Override
    public State getState() {
        return State.GAME_ENDING;
    }
}
