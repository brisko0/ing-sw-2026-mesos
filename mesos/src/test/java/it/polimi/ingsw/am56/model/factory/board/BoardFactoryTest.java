package it.polimi.ingsw.am56.model.factory.board;

import it.polimi.ingsw.am56.model.board.Board;
import it.polimi.ingsw.am56.model.board.OfferTile;
import it.polimi.ingsw.am56.model.board.OrderTile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardFactoryTest {
    private BoardFactory boardFactory;
    private Board board;
    private int gameSize;

    @BeforeEach
    void setUp() {
        gameSize = 5;
        boardFactory = new BoardFactory();
        board = boardFactory.createBoard(gameSize);
    }

    @Test
    void shouldHaveAddedTails() {

        assertNotNull(board.getOrderTiles());
        assertNotNull(board.getOfferTiles());
        assertFalse(board.getOrderTiles().isEmpty());
        assertFalse(board.getOfferTiles().isEmpty());

        assertNotNull(board.getUpperRowCharEv());
        assertTrue(board.getUpperRowCharEv().isEmpty());

        assertNotNull(board.getLowerRowCharEv());
        assertTrue(board.getLowerRowCharEv().isEmpty());

        assertNotNull(board.getUpperRowBuildings());
        assertTrue(board.getUpperRowBuildings().isEmpty());

        assertNotNull(board.getLowerRowBuildings());
        assertTrue(board.getLowerRowBuildings().isEmpty());

        for(OrderTile o : board.getOrderTiles()) {
            assertEquals(gameSize, o.getNumPlayers());
        }

        for(OfferTile o : board.getOfferTiles()) {
            assertTrue(gameSize >= o.getMinPlayers());
        }
    }

}