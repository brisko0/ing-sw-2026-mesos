package it.polimi.ingsw.am56.model.factory.card;

import it.polimi.ingsw.am56.model.card.buildingCard.BuildingCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BuildingCardFactoryTest {
    private BuildingCardFactory factory;

    @BeforeEach
    void setUp() {
        factory = new BuildingCardFactory();
    }


    @Test
    void shouldReturnEra1Cards() {
        int era = 1;
        int gameSize = 4;
        List<BuildingCard> cards = factory.createCards(era, gameSize);

        assertNotNull(cards);
        assertFalse(cards.isEmpty());

        for (BuildingCard card : cards) {
            assertEquals(era, card.getEra());
            assertTrue(gameSize >= card.getMinPlayers());
        }
    }


    @Test
    void shouldFilterCorrectly() {
        int era = 1;
        List<BuildingCard> cardsFor2 = factory.createCards(era, 2);
        List<BuildingCard> cardsFor5 = factory.createCards(era, 5);

        assertTrue(cardsFor5.size() >= cardsFor2.size());

        for (BuildingCard card : cardsFor2) {
            assertEquals(era, card.getEra());
            assertTrue(2 >= card.getMinPlayers());
        }

        for (BuildingCard card : cardsFor2) {
            assertEquals(era, card.getEra());
            assertTrue(5 >= card.getMinPlayers());
        }
    }


    @Test
    void shouldReturnEmptyList() {
        List<BuildingCard> cardsWrongEra = factory.createCards(99, 3);

        assertNotNull(cardsWrongEra);
        assertTrue(cardsWrongEra.isEmpty());
    }


    @Test
    void shouldReturnSameResult() {
        List<BuildingCard> firstCall = factory.createCards(2, 3);
        List<BuildingCard> secondCall = factory.createCards(2, 3);

        assertEquals(firstCall.size(), secondCall.size());
    }

}