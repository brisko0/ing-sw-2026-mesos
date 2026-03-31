package it.polimi.ingsw.am56.model.card.eventCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.card.CardType;

import java.util.List;

public abstract class EventCard extends Card {

    public EventCard(int cardId, int era, int minPlayers, CardType type) {
        super(cardId, era, minPlayers, CardType.EVENT);
    }

    public abstract void resolveEffect(GameModel gameModel);

}
