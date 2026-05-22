package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;

import java.io.Serial;

public class RoundEnding extends GameState{
    @Serial
    private static final long serialVersionUID = 1L;

    public RoundEnding(GameModel model) {
        super(model);
    }

    @Override
    public void onEntry() {
        endRoundActions();
    }

    private void endRoundActions(){
        model.endRound();

        if(model.getCurrentRound() == 3){
            model.setState(new GameEnding(model));
            return;
        }

        model.setState(new TotemPlacement(model));
        model.setCurrentPlayerOrder();

    }


    @Override
    public State getState() {
        return State.ROUND_ENDING;
    }
}
