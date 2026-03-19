package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.player.Player;
import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.tile.OfferTile;

public abstract class GameState {
    protected final GameModel model;

    public GameState(GameModel model) {
        this.model = model;
    }

    public void addPlayer(Player p) { }

    public void handleTotemPlacement(Player p, OfferTile t){}

    public void pickUpCard(Player p, Card c) {}
}
