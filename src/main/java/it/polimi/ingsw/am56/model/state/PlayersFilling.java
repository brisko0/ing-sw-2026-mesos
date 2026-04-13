package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;

public class PlayersFilling extends GameState{

    public PlayersFilling(GameModel model) {
        super(model);
    }

    @Override
    public void addPlayer(Player p) throws InvalidActionException {
        for (Player existing : model.getPlayers()) {
            if (existing.getNickname().equalsIgnoreCase(p.getNickname())) {
                throw new InvalidActionException("Nickname già in uso!");
            }
        }

        model.executeAddPlayer(p);
    }
}
