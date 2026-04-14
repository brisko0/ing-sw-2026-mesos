package it.polimi.ingsw.am56.model.factory.board;

import it.polimi.ingsw.am56.model.board.OfferTile;
import it.polimi.ingsw.am56.model.board.OrderTile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderTilesFactoryTest {
    private OrderTilesFactory factory;

    @BeforeEach
    void setUp() {
        factory = new OrderTilesFactory();
    }


    @Test
    void shouldReturnCorrectTiles() {
        int gameSize = 4;
        List<OrderTile> orderTiles = factory.createOrderTiles(gameSize);

        assertNotNull(orderTiles);
        assertFalse(orderTiles.isEmpty());

        for (OrderTile orderTile : orderTiles) {
            assertTrue(gameSize >= orderTile.getNumPlayers());
        }
    }


    @Test
    void shouldFilterCorrectly() {
        List<OrderTile> orderTiles2 = factory.createOrderTiles( 2);
        List<OrderTile> orderTiles5 = factory.createOrderTiles( 5);

        assertTrue(orderTiles5.size() >= orderTiles2.size());

        for (OrderTile offerTile : orderTiles2) {
            assertTrue(2 >= offerTile.getNumPlayers());
        }

        for (OrderTile offerTile : orderTiles5) {
            assertTrue(5 >= offerTile.getNumPlayers());
        }
    }


    @Test
    void shouldReturnEmptyList() {
        List<OrderTile> tilesWrongSize = factory.createOrderTiles(0);

        assertNotNull(tilesWrongSize);
        assertTrue(tilesWrongSize.isEmpty());
    }


    @Test
    void shouldReturnSameResult() {
        List<OrderTile> firstCall = factory.createOrderTiles(3);
        List<OrderTile> secondCall = factory.createOrderTiles(3);

        assertEquals(firstCall.size(), secondCall.size());
    }


}