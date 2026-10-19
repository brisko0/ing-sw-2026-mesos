package it.polimi.ingsw.am56.model.factory.board;

import it.polimi.ingsw.am56.model.board.OfferTile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OfferTilesFactoryTest {
    private OfferTilesFactory factory;

    @BeforeEach
    void setUp() {
        factory = new OfferTilesFactory();
    }


    @Test
    void shouldReturnCorrectTiles() {
        int gameSize = 4;
        List<OfferTile> OfferTiles = factory.createOfferTiles(gameSize);

        assertNotNull(OfferTiles);
        assertFalse(OfferTiles.isEmpty());

        for (OfferTile offerTile : OfferTiles) {
            assertTrue(gameSize >= offerTile.getMinPlayers());
        }
    }


    @Test
    void shouldFilterCorrectly() {
        List<OfferTile> OfferTiles2 = factory.createOfferTiles( 2);
        List<OfferTile> OfferTiles5 = factory.createOfferTiles( 5);

        assertTrue(OfferTiles5.size() >= OfferTiles2.size());

        for (OfferTile offerTile : OfferTiles2) {
            assertTrue(2 >= offerTile.getMinPlayers());
        }

        for (OfferTile offerTile : OfferTiles5) {
            assertTrue(5 >= offerTile.getMinPlayers());
        }
    }


    @Test
    void shouldReturnEmptyList() {
        List<OfferTile> tilesWrongSize = factory.createOfferTiles(0);

        assertNotNull(tilesWrongSize);
        assertTrue(tilesWrongSize.isEmpty());
    }


    @Test
    void shouldReturnSameResult() {
        List<OfferTile> firstCall = factory.createOfferTiles(3);
        List<OfferTile> secondCall = factory.createOfferTiles(3);

        assertEquals(firstCall.size(), secondCall.size());
    }

}