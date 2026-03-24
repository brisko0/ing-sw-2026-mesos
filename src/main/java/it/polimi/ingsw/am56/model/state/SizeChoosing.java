package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;

public class SizeChoosing extends GameState{

    public SizeChoosing(GameModel model) {
        super(model);
    }

    @Override
    public void setGameSize(int gameSize) throws InvalidActionException {
        if(gameSize < 2 || gameSize > 5) {
            throw new InvalidActionException("il numero di giocatori deve" +
                    "essere compreso tra 2 e 4");
        }

        model.setGameSize(gameSize);

        model.setState(new PlayersFilling(model));
    }
}
