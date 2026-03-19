package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.player.Player;
import it.polimi.ingsw.am56.model.card.Card;

public class CardPicking extends GameState{

    public CardPicking(GameModel model) {
        super(model);
    }

    @Override
    public void pickUpCard(Player p, Card c) { }
}
