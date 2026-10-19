package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.buildingCard.AddPointsEndGame;
import it.polimi.ingsw.am56.model.card.buildingCard.ExtraUpperCardAfterActions;
import it.polimi.ingsw.am56.model.card.characterCard.ArtistCharCard;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardPickingTest {

    private GameModel gameModel;
    private ArtistCharCard artistCard;
    private AddPointsEndGame addPointsEndGame;


    @BeforeEach
    void setUp() throws InvalidActionException {
        gameModel = new GameModel();
        gameModel.initializeGame(2);
        gameModel.getPlayers().add(new Player("a", "a"));
        gameModel.getPlayers().add(new Player("b", "a"));
        gameModel.getPlayerToOfferTile().put(gameModel.getPlayers().getFirst(), gameModel.getBoard().getOfferTiles().get(3));
        gameModel.setCurrentPlayer(gameModel.getPlayers().getFirst());
        gameModel.setState(new CardPicking(gameModel));

        artistCard = new ArtistCharCard(1, 1, 1);
        addPointsEndGame = new AddPointsEndGame(1, 1, 1, 1, 1, 1);
    }

    @Test
    void wrongTurnTest() throws InvalidActionException {
        assertThrows(InvalidActionException.class, () -> { gameModel.pickUpCard(gameModel.getPlayers().get(1), gameModel.getBoard().getUpperRowCharEv().getFirst()); });
    }

    @Test
    void cardNotAvailable() throws InvalidActionException {
        while(!gameModel.getBoard().getUpperRowCharEv().isEmpty()) {
            gameModel.getBoard().getUpperRowCharEv().removeFirst();
        }
        assertThrows(InvalidActionException.class, () -> { gameModel.pickUpCard(gameModel.getPlayers().getFirst(), artistCard); });
    }

    @Test
    void isUpperButCantPick() {
        gameModel.getPlayerToOfferTile().remove(gameModel.getPlayers().getFirst());
        gameModel.getPlayerToOfferTile().put(gameModel.getPlayers().getFirst(), gameModel.getBoard().getOfferTiles().getFirst());

        gameModel.getBoard().getUpperRowCharEv().add(artistCard);

        assertThrows(InvalidActionException.class, () -> { gameModel.pickUpCard(gameModel.getPlayers().getFirst(), artistCard); });
    }

    @Test
    void isLowerButCantPick() {
        gameModel.getPlayerToOfferTile().remove(gameModel.getPlayers().getFirst());
        gameModel.getPlayerToOfferTile().put(gameModel.getPlayers().getFirst(), gameModel.getBoard().getOfferTiles().get(1));

        gameModel.getBoard().getLowerRowCharEv().add(artistCard);

        assertThrows(InvalidActionException.class, () -> { gameModel.pickUpCard(gameModel.getPlayers().getFirst(), artistCard); });
    }

    @Test
    void cantBuyBuildingTest() throws InvalidActionException {
        gameModel.getBoard().getUpperRowBuildings().add(addPointsEndGame);

        gameModel.getBoard().getOrderTiles().getFirst().setIsFree(true);

        assertThrows(InvalidActionException.class, () -> { gameModel.pickUpCard(gameModel.getPlayers().getFirst(), addPointsEndGame); });
    }

    @Test
    void pickLowerTest() throws InvalidActionException {
        gameModel.getPlayerToOfferTile().remove(gameModel.getPlayers().getFirst());
        gameModel.getPlayerToOfferTile().put(gameModel.getPlayers().getFirst(), gameModel.getBoard().getOfferTiles().getFirst());
        gameModel.getPlayerToOfferTile().put(gameModel.getPlayers().get(1), gameModel.getBoard().getOfferTiles().get(1));

        gameModel.getBoard().getLowerRowCharEv().add(artistCard);

        gameModel.getBoard().getOrderTiles().getFirst().setIsFree(true);

        gameModel.pickUpCard(gameModel.getPlayers().getFirst(), artistCard);

        assertEquals(gameModel.getCurrentPlayer(), gameModel.getPlayers().get(1));
    }

    @Test
    void pickLowerBuildingTest() throws InvalidActionException {
        gameModel.getPlayerToOfferTile().remove(gameModel.getPlayers().getFirst());
        gameModel.getPlayerToOfferTile().put(gameModel.getPlayers().getFirst(), gameModel.getBoard().getOfferTiles().getFirst());
        gameModel.getPlayerToOfferTile().put(gameModel.getPlayers().get(1), gameModel.getBoard().getOfferTiles().get(1));

        gameModel.getBoard().getLowerRowBuildings().add(addPointsEndGame);

        gameModel.getBoard().getOrderTiles().getFirst().setIsFree(true);

        gameModel.getPlayers().getFirst().getStats().addFood(10);
        gameModel.pickUpCard(gameModel.getPlayers().getFirst(), addPointsEndGame);

        assertEquals(gameModel.getCurrentPlayer(), gameModel.getPlayers().get(1));
    }

    @Test
    void playerCantPickAnymoreTest() throws InvalidActionException {
        gameModel.getPlayerToOfferTile().put(gameModel.getPlayers().get(1), gameModel.getBoard().getOfferTiles().get(1));

        gameModel.getBoard().getUpperRowCharEv().add(artistCard);
        gameModel.pickUpCard(gameModel.getPlayers().getFirst(), artistCard);

        assertEquals(gameModel.getCurrentPlayer(), gameModel.getPlayers().getFirst());

        gameModel.getBoard().getOrderTiles().getFirst().setIsFree(true);

        gameModel.getBoard().getUpperRowCharEv().add(artistCard);
        gameModel.pickUpCard(gameModel.getPlayers().getFirst(), artistCard);

        assertEquals(gameModel.getCurrentPlayer(), gameModel.getPlayers().get(1));

    }

    @Test
    void lastPlayerCantPickAnymoreTest() throws InvalidActionException {
        gameModel.getPlayerToOrderTile().put(gameModel.getPlayers().get(1), gameModel.getBoard().getOrderTiles().get(1));


        gameModel.getBoard().getUpperRowCharEv().add(artistCard);
        gameModel.pickUpCard(gameModel.getPlayers().getFirst(), artistCard);

        assertEquals(gameModel.getCurrentPlayer(), gameModel.getPlayers().getFirst());

        gameModel.getBoard().getOrderTiles().getFirst().setIsFree(true);

        gameModel.getBoard().getUpperRowCharEv().add(artistCard);
        gameModel.pickUpCard(gameModel.getPlayers().getFirst(), artistCard);

        assertEquals(gameModel.getCurrentPlayer(), gameModel.getPlayers().getFirst());
    }

    @Test
    void shouldReturnCorrectState() {
        assertEquals(State.CARD_PICKING, gameModel.getCurrentState().getState());
    }






    @Test
    void wrongStateTest() {

        gameModel.setState(new TotemPlacement(gameModel));

        assertThrows(InvalidActionException.class, () -> { gameModel.pickUpCard(gameModel.getCurrentPlayer(), gameModel.getBoard().getUpperRowCharEv().getFirst()); });
    }

    @Test
    void lastPlayerTriggersExtraUpperPickTest() {
        Player p0 = gameModel.getPlayers().getFirst();

        p0.getStats().getBuildingCards().add(new ExtraUpperCardAfterActions(99, 1, 1, 0, 0));

        var ot = gameModel.getPlayerToOfferTile().get(p0);
        p0.setPickedUpper(ot.getUpperCards());
        p0.setPickedLower(ot.getLowerCards());

        gameModel.getBoard().getUpperRowCharEv().clear();
        gameModel.getBoard().getUpperRowCharEv().add(new ArtistCharCard(2, 1, 1));
        gameModel.getBoard().getLowerRowCharEv().clear();
        gameModel.getBoard().getLowerRowBuildings().clear();

        gameModel.getBoard().getOrderTiles().getFirst().setIsFree(true);

        gameModel.setState(new CardPicking(gameModel));

        assertEquals(State.EXTRA_UPPER_CARD_PICKING, gameModel.getCurrentState().getState());
        assertEquals(p0, gameModel.getCurrentPlayer());
    }
}