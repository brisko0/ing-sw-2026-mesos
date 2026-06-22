package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.buildingCard.AddPointsEndGame;
import it.polimi.ingsw.am56.model.card.characterCard.ArtistCharCard;
import it.polimi.ingsw.am56.model.card.eventCard.ShamansEvCard;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExtraUpperCardPickingTest {

    private GameModel gameModel;
    private Player p0;
    private Player p1;

    @BeforeEach
    void setUp() throws InvalidActionException {
        gameModel = new GameModel();
        gameModel.initializeGame(2);

        p0 = new Player("a", "a");
        p1 = new Player("b", "b");
        gameModel.getPlayers().add(p0);
        gameModel.getPlayers().add(p1);

        gameModel.getPlayerToOrderTile().put(p0, gameModel.getBoard().getOrderTiles().getFirst());
        gameModel.getBoard().getOrderTiles().getFirst().setIsFree(false);
        gameModel.getPlayerToOrderTile().put(p1, gameModel.getBoard().getOrderTiles().get(1));
        gameModel.getBoard().getOrderTiles().get(1).setIsFree(false);

        gameModel.setCurrentPlayer(p0);
        gameModel.setState(new ExtraUpperCardPicking(gameModel));
    }

    @Test
    void shouldReturnCorrectState() {
        assertEquals(State.EXTRA_UPPER_CARD_PICKING, gameModel.getCurrentState().getState());
    }

    @Test
    void wrongTurnTest() {
        ArtistCharCard c = new ArtistCharCard(7, 1, 1);
        gameModel.getBoard().getUpperRowCharEv().add(c);
        assertThrows(InvalidActionException.class, () -> gameModel.pickUpCard(p1, c));
    }

    @Test
    void cardNotOnUpperRowTest() {
        ArtistCharCard c = new ArtistCharCard(7, 1, 1);
        gameModel.getBoard().getLowerRowCharEv().add(c);
        assertThrows(InvalidActionException.class, () -> gameModel.pickUpCard(p0, c));
    }

    @Test
    void eventCardRejectedTest() {
        ShamansEvCard event = new ShamansEvCard(7, 1, 1, false, 5, 3);
        gameModel.getBoard().getUpperRowCharEv().add(event);
        assertThrows(InvalidActionException.class, () -> gameModel.pickUpCard(p0, event));
    }

    @Test
    void cantAffordBuildingTest() {
        AddPointsEndGame building = new AddPointsEndGame(7, 1, 1, 5, 1, 1);
        gameModel.getBoard().getUpperRowBuildings().add(building);
        assertThrows(InvalidActionException.class, () -> gameModel.pickUpCard(p0, building));
    }

    @Test
    void validPickIsTakenAndRoundAdvancesTest() throws InvalidActionException {
        gameModel.getBoard().getUpperRowCharEv().clear();
        gameModel.getBoard().getLowerRowCharEv().clear();

        ArtistCharCard c = new ArtistCharCard(7, 1, 1);
        gameModel.getBoard().getUpperRowCharEv().add(c);

        gameModel.pickUpCard(p0, c);

        assertTrue(p0.getStats().getCharacterCards().contains(c));
        assertFalse(gameModel.getBoard().getUpperRowCharEv().contains(c));
        assertEquals(0, p0.getPickedUpper());
        assertEquals(2, gameModel.getCurrentRound());
        assertEquals(State.TOTEM_PLACEMENT, gameModel.getCurrentState().getState());
    }

    @Test
    void skipWrongTurnTest() {
        assertThrows(InvalidActionException.class, () -> gameModel.skipExtraUpperCardPick(p1));
    }

    @Test
    void skipAdvancesRoundWithoutPickingTest() throws InvalidActionException {
        gameModel.getBoard().getUpperRowCharEv().clear();
        gameModel.getBoard().getLowerRowCharEv().clear();

        gameModel.skipExtraUpperCardPick(p0);

        assertTrue(p0.getStats().getCharacterCards().isEmpty());
        assertEquals(2, gameModel.getCurrentRound());
        assertEquals(State.TOTEM_PLACEMENT, gameModel.getCurrentState().getState());
    }
}
