package it.polimi.ingsw.am56.model.board;

import it.polimi.ingsw.am56.model.card.buildingCard.FeedingSaleCharType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderTileTest {
    private OrderTile orderTile;

    @BeforeEach
    void setUp() {
        orderTile = new OrderTile(1, 2, 3, 4, true);
    }

    @Test
    void testGetters() {
        assertEquals(1, orderTile.getOrderTileId());
        assertEquals(2, orderTile.getFoodEffect());
        assertEquals(3, orderTile.getPointsEffect());
        assertEquals(4, orderTile.getNumPlayers());
        assertTrue(orderTile.isFree());
    }

    @Test
    void shouldSetNotFree() {
        assertTrue(orderTile.isFree());

        orderTile.setIsFree(false);

        assertFalse(orderTile.isFree());
    }

}