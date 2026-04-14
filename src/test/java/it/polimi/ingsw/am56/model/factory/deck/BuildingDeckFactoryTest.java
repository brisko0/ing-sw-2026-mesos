package it.polimi.ingsw.am56.model.factory.deck;


import it.polimi.ingsw.am56.model.card.BuildingDeck;
import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.card.CardType;
import it.polimi.ingsw.am56.model.card.Deck;
import it.polimi.ingsw.am56.model.card.buildingCard.BuildingCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuildingDeckFactoryTest {
    int era = 1;
    int gameSize = 3;
    private BuildingDeckFactory factory;
    private BuildingDeck deck;

    @BeforeEach
    void setUp() {
        factory = new BuildingDeckFactory();
        deck = factory.createDeck(era, gameSize);
    }


    @Test
    void shouldReturnEra1Cards() {

        assertNotNull(deck);
        assertFalse(deck.getRemainingCards().isEmpty());

        for (Card card : deck.getRemainingCards()) {
            assertEquals(era, card.getEra());
            assertTrue(gameSize >= card.getMinPlayers());
            assertEquals(CardType.BUILDING, card.getCardType());
        }
    }


    @Test
    void shouldFilterGameSize() {
        int era = 1;

        BuildingDeck smallDeck = factory.createDeck(era, 2);
        BuildingDeck largeDeck = factory.createDeck(era, 5);

        assertTrue(largeDeck.getRemainingCards().size() >= smallDeck.getRemainingCards().size());
    }


}