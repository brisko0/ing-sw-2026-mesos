package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;

public class RoundEnding extends GameState{

    public RoundEnding(GameModel model) {
        super(model);
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
}
