package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;

import java.io.Serial;

/**
 * Transient end-of-round state. Runs the model's {@code endRound} bookkeeping on entry — event
 * resolution, board sliding, era roll-overs, etc. — then either transitions back to
 * {@link TotemPlacement} for the next round or to {@link GameEnding} once round 11 has been
 * reached.
 */
public class RoundEnding extends GameState{
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates the round-ending state.
     *
     * @param model the game model this state operates on
     */
    public RoundEnding(GameModel model) {
        super(model);
    }

    /**
     * Runs the end-of-round actions and decides the next state automatically.
     */
    @Override
    public void onEntry() {
        endRoundActions();
    }

    private void endRoundActions(){
        model.endRound();

        if(model.getCurrentRound() == 11){
            model.setState(new GameEnding(model));
            return;
        }

        model.setState(new TotemPlacement(model));
        model.setCurrentPlayerOrder();

    }


    /**
     * @return {@link State#ROUND_ENDING}
     */
    @Override
    public State getState() {
        return State.ROUND_ENDING;
    }
}
