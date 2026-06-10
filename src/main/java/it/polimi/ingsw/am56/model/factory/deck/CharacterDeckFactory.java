package it.polimi.ingsw.am56.model.factory.deck;

import it.polimi.ingsw.am56.model.card.Deck;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterCard;
import it.polimi.ingsw.am56.model.factory.DeckFactory;
import it.polimi.ingsw.am56.model.factory.card.CharacterCardFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link DeckFactory} implementation that assembles the shuffled character {@link Deck} for a
 * given era. Delegates the loading and filtering of the individual {@link CharacterCard}s to
 * {@link CharacterCardFactory}, then wraps them in a {@link Deck} (the {@link Deck} constructor
 * shuffles the cards in place).
 * <p>
 * The deck's id and era fields are both set to {@code era} — there is exactly one character
 * deck per era, so the era doubles as a stable identifier.
 */
public class CharacterDeckFactory implements DeckFactory {

    /**
     * Builds the character deck for the given era and player count.
     *
     * @param era      era to build the deck for (1, 2 or 3)
     * @param gameSize number of players in the game
     * @return a shuffled deck of the character cards available for the given era and player count
     */
    @Override
    public Deck createDeck(int era, int gameSize) {

        CharacterCardFactory characterCardFactory = new CharacterCardFactory();

        List<CharacterCard> characterCards = characterCardFactory.createCards(era, gameSize);

        return new Deck(era, era, new ArrayList<>(characterCards));
    }
}
