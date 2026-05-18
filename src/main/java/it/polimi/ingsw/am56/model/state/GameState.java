package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.board.OfferTile;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;

import java.io.Serial;
import java.io.Serializable;

public abstract class GameState implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    protected final GameModel model;

    public GameState(GameModel model) {
        this.model = model;
    }

    public abstract State getState();

    public void addPlayer(Player p) throws InvalidActionException {
        throw new InvalidActionException("Cannot add players in this phase.");
    }

    public void handleTotemPlacement(Player p, OfferTile t) throws InvalidActionException {
        throw new InvalidActionException("Cannot place totems right now.");
    }

    public void pickUpCard(Player p, Card c) throws InvalidActionException {
        throw new InvalidActionException("Cannot pick up cards right now.");
    }

    public void onEntry() {}
}
