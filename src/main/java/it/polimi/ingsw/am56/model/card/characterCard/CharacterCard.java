package it.polimi.ingsw.am56.model.card.characterCard;

import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.card.CardType;

public abstract class CharacterCard extends Card {

    public CharacterCard(int cardId, int era, int minPlayers) {
        super( cardId, era, minPlayers);
    }

    public abstract CharacterType getCharacterType();

    @Override
    public CardType getCardType() {
        return CardType.CHARACTER;
    }

}