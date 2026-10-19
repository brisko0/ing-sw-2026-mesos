package it.polimi.ingsw.am56.model.factory.deck;

import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.card.Deck;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterCard;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterType;
import it.polimi.ingsw.am56.model.factory.card.CharacterCardFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CharacterDeckFactoryTest {
    private CharacterDeckFactory factory;

    @BeforeEach
    void setUp() {
        factory = new CharacterDeckFactory();
    }


    @Test
    void shouldReturnEra1Cards() {
        int era = 1;
        int gameSize = 4;
        Deck deck = factory.createDeck(era, gameSize);

        assertNotNull(deck);
        assertFalse(deck.getRemainingCards().isEmpty());

        for (Card card : deck.getRemainingCards()) {
            assertEquals(era, card.getEra());
            assertTrue(gameSize >= card.getMinPlayers());
        }
    }


    @Test
    void shouldFilterGameSize() {
        int era = 1;

        Deck smallDeck = factory.createDeck(era, 2);
        Deck largeDeck = factory.createDeck(era, 5);

        assertTrue(largeDeck.getRemainingCards().size() >= smallDeck.getRemainingCards().size());
    }

}