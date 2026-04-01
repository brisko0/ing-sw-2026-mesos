package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.BuildingCard;
import it.polimi.ingsw.am56.model.board.OfferTile;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterCard;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;

public abstract class GameState {
    protected final GameModel model;

    public GameState(GameModel model) {
        this.model = model;
    }

    public void addPlayer(Player p) throws InvalidActionException {
        throw new InvalidActionException("Cannot add players in this phase.");
    }

    public void handleTotemPlacement(Player p, OfferTile t) throws InvalidActionException {
        throw new InvalidActionException("Cannot place totems right now.");
    }

    public void pickUpCharacterCard(Player p, CharacterCard c) throws InvalidActionException {
        throw new InvalidActionException("Cannot pick up character cards right now.");
    }

    public void pickUpBuildingCard(Player p, BuildingCard c) throws InvalidActionException {
        throw new InvalidActionException("Cannot pick up building cards right now.");
    }

    public void setGameSize(int gameSize) throws InvalidActionException {
        throw new InvalidActionException("Cannot change the game size.");
    }
}
