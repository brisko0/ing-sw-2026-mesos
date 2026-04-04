package it.polimi.ingsw.am56.model.card.eventCard;

import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.card.CardType;

public abstract class EventCard extends Card {

    public EventCard(int cardId, int era, int minPlayers) {
        super(cardId, era, minPlayers);
    }

    public abstract EvType getEventType();

    @Override
    public CardType getCardType(){
        return CardType.EVENT;
    }
}
