package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.player.Player;
import it.polimi.ingsw.am56.model.tile.OfferTile;

public class TotemPlacement extends GameState{

    public TotemPlacement(GameModel model) {
        super(model);
    }

    @Override
    public void handleTotemPlacement(Player p, OfferTile t) { }
}
