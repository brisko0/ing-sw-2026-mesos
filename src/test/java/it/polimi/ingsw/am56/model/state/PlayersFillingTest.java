package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.board.OrderTile;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayersFillingTest {

    private GameModel gameModel;


    @BeforeEach
    void setUp() throws InvalidActionException {
        gameModel = new GameModel();
        gameModel.initializeGame(5);
        gameModel.addPlayer(new Player("a", "a"));

        assertEquals(State.PLAYERS_FILLING, gameModel.getCurrentState().getState());
    }

    @Test
    void shouldAddPlayer() throws InvalidActionException {
        gameModel.getCurrentState().addPlayer(new Player("b", "b"));
    }

    @Test
    void shouldNotAddDuplicatedNickname() throws InvalidActionException {
        assertThrows(InvalidActionException.class, () -> { gameModel.getCurrentState().addPlayer(new Player("a", "b")); });
    }

    @Test
    void shouldNotAddDuplicatedColor() throws InvalidActionException {
        assertThrows(InvalidActionException.class, () -> { gameModel.getCurrentState().addPlayer(new Player("b", "a")); });
    }

    @Test
    void shouldNotAddToFull() {
        gameModel.getPlayers().add(new Player("b", "b"));
        gameModel.getPlayers().add(new Player("c", "c"));
        gameModel.getPlayers().add(new Player("d", "d"));
        gameModel.getPlayers().add(new Player("e", "e"));

        assertThrows(InvalidActionException.class, () -> { gameModel.getCurrentState().addPlayer(new Player("f", "f")); });
    }

    @Test
    void wrongPhaseTest() throws InvalidActionException {
        gameModel.setState(new TotemPlacement(gameModel));
        assertThrows(InvalidActionException.class, () -> { gameModel.addPlayer(new Player("f", "f")); });
    }

    @Test
    void shouldDoFinalSetUp() throws InvalidActionException {
        gameModel.addPlayer(new Player("b", "b"));
        gameModel.addPlayer(new Player("c", "c"));
        gameModel.addPlayer(new Player("d", "d"));
        gameModel.addPlayer(new Player("e", "e"));

        assertEquals(2, gameModel.getPlayers().getFirst().getStats().getFood());
        assertEquals(3, gameModel.getPlayers().get(1).getStats().getFood());
        assertEquals(3, gameModel.getPlayers().get(2).getStats().getFood());
        assertEquals(4, gameModel.getPlayers().get(3).getStats().getFood());
        assertEquals(4, gameModel.getPlayers().get(4).getStats().getFood());

        for(Player player : gameModel.getPlayers()) {
            assertTrue(gameModel.getPlayerToOrderTile().containsKey(player));
        }

        for(OrderTile orderTile : gameModel.getBoard().getOrderTiles()) {
            assertFalse(orderTile.isFree());
        }

        assertEquals(State.TOTEM_PLACEMENT, gameModel.getCurrentState().getState());

        assertEquals(gameModel.getCurrentPlayer(), gameModel.getPlayers().getFirst());
    }

    @Test
    void shouldReturnCorrectState() {
        assertEquals(State.PLAYERS_FILLING, gameModel.getCurrentState().getState());
    }

}