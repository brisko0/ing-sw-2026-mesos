package it.polimi.ingsw.am56.model.factory.card;

import it.polimi.ingsw.am56.model.card.eventCard.EvType;
import it.polimi.ingsw.am56.model.card.eventCard.EventCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventCardFactoryTest {
    private EventCardFactory factory;

    @BeforeEach
    void setUp() {
        factory = new EventCardFactory();
    }

    @Test
    void shouldReturnEra1Cards() {
        int era = 1;
        int gameSize = 4;
        List<EventCard> cards = factory.createCards(era, gameSize);

        assertNotNull(cards);
        assertFalse(cards.isEmpty());

        for (EventCard card : cards) {
            assertEquals(era, card.getEra());
            assertTrue(gameSize >= card.getMinPlayers());
        }
    }


    @Test
    void shouldFilterCorrectly() {
        int era = 1;
        List<EventCard> cardsFor2 = factory.createCards(era, 2);
        List<EventCard> cardsFor5 = factory.createCards(era, 5);

        assertTrue(cardsFor5.size() >= cardsFor2.size());

        for (EventCard card : cardsFor2) {
            assertEquals(era, card.getEra());
            assertTrue(2 >= card.getMinPlayers());
        }

        for (EventCard card : cardsFor2) {
            assertEquals(era, card.getEra());
            assertTrue(5 >= card.getMinPlayers());
        }
    }


    @Test
    void shouldInstantiateAllSubclasses() {
        List<EventCard> cards = factory.createCards(1, 5);

        boolean artistCreated = cards.stream().anyMatch(c -> c.getEventType() == EvType.ARTIST);
        boolean hunterCreated = cards.stream().anyMatch(c -> c.getEventType() == EvType.HUNTING);
        boolean shamanCreated = cards.stream().anyMatch(c -> c.getEventType() == EvType.SHAMAN);
        boolean sustenanceCreated = cards.stream().anyMatch(c -> c.getEventType() == EvType.SUSTENANCE);

        assertTrue(artistCreated);
        assertTrue(hunterCreated);
        assertTrue(shamanCreated);
        assertTrue(sustenanceCreated);
    }


    @Test
    void shouldReturnEmptyList() {
        List<EventCard> cardsWrongEra = factory.createCards(99, 3);
        List<EventCard> cardsWrongGameSize = factory.createCards(1, 0);

        assertNotNull(cardsWrongEra);
        assertTrue(cardsWrongEra.isEmpty());

        assertNotNull(cardsWrongGameSize);
        assertTrue(cardsWrongGameSize.isEmpty());
    }


    @Test
    void shouldReturnSameResult() {
        List<EventCard> firstCall = factory.createCards(2, 3);
        List<EventCard> secondCall = factory.createCards(2, 3);

        assertEquals(firstCall.size(), secondCall.size());
    }
}