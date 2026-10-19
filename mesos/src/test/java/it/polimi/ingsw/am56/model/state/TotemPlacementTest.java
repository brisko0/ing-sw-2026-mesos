package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TotemPlacementTest {
    private GameModel gameModel;
    private TotemPlacement totemPlacement;

    @BeforeEach
    void setUp() throws InvalidActionException {
        gameModel = new GameModel();
        gameModel.initializeGame(2);
        gameModel.addPlayer(new Player("a", "a"));
        gameModel.addPlayer(new Player("b", "b"));
        assertEquals(State.TOTEM_PLACEMENT, gameModel.getCurrentState().getState());

        totemPlacement = new TotemPlacement(gameModel);
    }

    @Test
    void wrongTurnTest() throws InvalidActionException {
        assertThrows(InvalidActionException.class, () -> { gameModel.handleTotemPlacement(gameModel.getPlayers().get(1), gameModel.getBoard().getOfferTiles().getFirst()); });
    }

    @Test
    void alreadyChosenTileTest() throws InvalidActionException {
        gameModel.handleTotemPlacement(gameModel.getCurrentPlayer(), gameModel.getBoard().getOfferTiles().getFirst());
        assertThrows(InvalidActionException.class, () -> { gameModel.handleTotemPlacement(gameModel.getPlayers().get(1), gameModel.getBoard().getOfferTiles().getFirst()); });
    }

    @Test
    void wrongPhaseTest() throws InvalidActionException {
        gameModel.setState(new PlayersFilling(gameModel));
        assertThrows(InvalidActionException.class, () -> { gameModel.handleTotemPlacement(gameModel.getPlayers().getFirst(), gameModel.getBoard().getOfferTiles().getFirst()); });
    }

    @Test
    void shouldPlaceTotems() throws InvalidActionException {
        gameModel.handleTotemPlacement(gameModel.getCurrentPlayer(), gameModel.getBoard().getOfferTiles().getFirst());
        gameModel.handleTotemPlacement(gameModel.getCurrentPlayer(), gameModel.getBoard().getOfferTiles().get(1));

        assertEquals(State.CARD_PICKING, gameModel.getCurrentState().getState());
    }


    @Test
    void shouldReturnCorrectState() {
        gameModel.setState(totemPlacement);
        assertEquals(State.TOTEM_PLACEMENT, totemPlacement.getState());
    }
}