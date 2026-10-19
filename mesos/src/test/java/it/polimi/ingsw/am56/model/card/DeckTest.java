package it.polimi.ingsw.am56.model.card;

import it.polimi.ingsw.am56.model.factory.card.EventCardFactory;
import it.polimi.ingsw.am56.model.factory.deck.CharacterDeckFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {
    private CharacterDeckFactory characterDeckFactory;
    private EventCardFactory eventCardFactory;
    private Deck deck;
    private Deck deck2;


    @BeforeEach
    void setUp() {
        characterDeckFactory = new CharacterDeckFactory();
        eventCardFactory = new EventCardFactory();
        deck = characterDeckFactory.createDeck(1, 5);
        deck.getRemainingCards().addAll(eventCardFactory.createCards(1, 5));
    }

    @Test
    void shouldDrawCard() {
        int initialSize = deck.getRemainingCards().size();
        Card c = deck.getRemainingCards().getFirst();

        Card card = deck.drawCard();

        assertEquals(deck.getRemainingCards().size(), initialSize - 1);
        assertEquals(c, card);
    }

    @Test
    void shouldShuffle() {
        deck2 = characterDeckFactory.createDeck(1, 5);
        deck2.getRemainingCards().addAll(eventCardFactory.createCards(1, 5));

        deck.shuffle();

        assertNotEquals(deck.getRemainingCards(), deck2.getRemainingCards());
    }

    @Test
    void shouldReturnEmpty() {
        while(!deck.getRemainingCards().isEmpty()) {
            deck.drawCard();
        }

        assertTrue(deck.isEmpty());
    }

}