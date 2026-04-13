package it.polimi.ingsw.am56.model.factory.deck;

import it.polimi.ingsw.am56.model.card.Deck;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterCard;
import it.polimi.ingsw.am56.model.factory.DeckFactory;
import it.polimi.ingsw.am56.model.factory.card.CharacterCardFactory;

import java.util.ArrayList;
import java.util.List;

public class CharacterDeckFactory implements DeckFactory {

    @Override
    public Deck createDeck(int era, int gameSize) {

        CharacterCardFactory characterCardFactory = new CharacterCardFactory();

        List<CharacterCard> characterCards = characterCardFactory.createCards(era, gameSize);

        return new Deck(era, era, new ArrayList<>(characterCards));
    }
}