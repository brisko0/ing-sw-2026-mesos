package it.polimi.ingsw.am56.model.factory.card;

import it.polimi.ingsw.am56.model.card.characterCard.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class CharacterCardFactoryTest {
    private CharacterCardFactory factory;

    @BeforeEach
    void setUp() {
        factory = new CharacterCardFactory();
    }


    @Test
    void shouldReturnEra1Cards() {
        int era = 1;
        int gameSize = 4;
        List<CharacterCard> cards = factory.createCards(era, gameSize);

        assertNotNull(cards);
        assertFalse(cards.isEmpty());

        for (CharacterCard card : cards) {
            assertEquals(era, card.getEra());
            assertTrue(gameSize >= card.getMinPlayers());
        }
    }


    @Test
    void shouldFilterCorrectly() {
        int era = 1;
        List<CharacterCard> cardsFor2 = factory.createCards(era, 2);
        List<CharacterCard> cardsFor5 = factory.createCards(era, 5);

        assertTrue(cardsFor5.size() >= cardsFor2.size());

        for (CharacterCard card : cardsFor2) {
            assertEquals(era, card.getEra());
            assertTrue(2 >= card.getMinPlayers());
        }

        for (CharacterCard card : cardsFor2) {
            assertEquals(era, card.getEra());
            assertTrue(5 >= card.getMinPlayers());
        }
    }


    @Test
    void shouldInstantiateAllSubclasses() {
        List<CharacterCard> cards = factory.createCards(1, 5);

        boolean hasArtist = cards.stream().anyMatch(c -> c.getCharacterType() == CharacterType.ARTIST);
        boolean hasBuilder = cards.stream().anyMatch(c -> c.getCharacterType() == CharacterType.BUILDER);
        boolean hasCollector = cards.stream().anyMatch(c -> c.getCharacterType() == CharacterType.COLLECTOR);
        boolean hasHunter = cards.stream().anyMatch(c -> c.getCharacterType() == CharacterType.HUNTER);
        boolean hasInventor = cards.stream().anyMatch(c -> c.getCharacterType() == CharacterType.INVENTOR);
        boolean hasShaman = cards.stream().anyMatch(c -> c.getCharacterType() == CharacterType.SHAMAN);

        assertTrue(hasArtist);
        assertTrue(hasBuilder);
        assertTrue(hasCollector);
        assertTrue(hasHunter);
        assertTrue(hasInventor);
        assertTrue(hasShaman);
    }


    @Test
    void shouldReturnEmptyList() {
        List<CharacterCard> cardsWrongEra = factory.createCards(99, 3);
        List<CharacterCard> cardsWrongGameSize = factory.createCards(1, 0);

        assertNotNull(cardsWrongEra);
        assertTrue(cardsWrongEra.isEmpty());

        assertNotNull(cardsWrongGameSize);
        assertTrue(cardsWrongGameSize.isEmpty());
    }


    @Test
    void shouldReturnSameResult() {
        List<CharacterCard> firstCall = factory.createCards(2, 3);
        List<CharacterCard> secondCall = factory.createCards(2, 3);

        assertEquals(firstCall.size(), secondCall.size());
    }
}