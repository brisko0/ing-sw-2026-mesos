package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;

import java.io.Serial;

/**
 * Terminal state. Runs the model's end-of-game actions on entry — final scoring from
 * buildings and characters — and then stays here; all player actions inherit the rejecting
 * defaults from {@link GameState} so no further moves are accepted. The final ranking and
 * winner (tie-break points → food, with shared victory) are exposed separately by
 * {@link it.polimi.ingsw.am56.model.GameModel#computeRanking()}, queried once scoring is done.
 */
public class GameEnding extends GameState {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates the game-ending state.
     *
     * @param model the game model this state operates on
     */
    public GameEnding(GameModel model) {
        super(model);
    }

    /**
     * Runs the end-of-game actions on entry.
     */
    @Override
    public void onEntry() {
        endGameActions();
    }

    /**
     * Delegates final scoring to the model. The ranking/winner is computed on demand via
     * {@link it.polimi.ingsw.am56.model.GameModel#computeRanking()} after this runs.
     */
    public void endGameActions() {
        model.endGameActions();
    }

    /**
     * @return {@link State#GAME_ENDING}
     */
    @Override
    public State getState() {
        return State.GAME_ENDING;
    }
}
