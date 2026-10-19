package it.polimi.ingsw.am56.model.board;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OfferTileTest {
    private OfferTile offerTile;

    @BeforeEach
    void setUp() {
        offerTile = new OfferTile("A", 1,2, 3, 4, true);
    }

    @Test
    void testGetters() {
        assertEquals("A", offerTile.getLetter());
        assertEquals(1, offerTile.getFoodBonus());
        assertEquals(2, offerTile.getUpperCards());
        assertEquals(3, offerTile.getLowerCards());
        assertEquals(4, offerTile.getMinPlayers());
        assertTrue(offerTile.isFree());
    }

    @Test
    void shouldSetNotFree() {
        assertTrue(offerTile.isFree());

        offerTile.setIsFree(false);

        assertFalse(offerTile.isFree());
    }

}
