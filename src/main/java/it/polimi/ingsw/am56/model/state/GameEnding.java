package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;

import java.io.Serial;

public class GameEnding extends GameState {
    @Serial
    private static final long serialVersionUID = 1L;

    public GameEnding(GameModel model) {
        super(model);
    }

    @Override
    public void onEntry() {
        endGameActions();
    }

    public void endGameActions() {
        model.endGameActions();
    }

    @Override
    public State getState() {
        return State.GAME_ENDING;
    }
}
