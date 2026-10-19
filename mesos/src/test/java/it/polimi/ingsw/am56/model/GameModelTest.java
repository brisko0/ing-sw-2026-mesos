package it.polimi.ingsw.am56.model;

import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.card.CardType;
import it.polimi.ingsw.am56.model.card.buildingCard.AddPointsEndGame;
import it.polimi.ingsw.am56.model.card.buildingCard.BuildingCard;
import it.polimi.ingsw.am56.model.card.buildingCard.ExtraUpperCardAfterActions;
import it.polimi.ingsw.am56.model.card.buildingCard.ShamanEventDouble;
import it.polimi.ingsw.am56.model.card.buildingCard.ShamanNoLoss;
import it.polimi.ingsw.am56.model.card.characterCard.ArtistCharCard;
import it.polimi.ingsw.am56.model.card.characterCard.BuilderCharCard;
import it.polimi.ingsw.am56.model.card.characterCard.InventorCharCard;
import it.polimi.ingsw.am56.model.card.eventCard.EventCard;
import it.polimi.ingsw.am56.model.card.eventCard.ShamansEvCard;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;
import it.polimi.ingsw.am56.model.state.CardPicking;
import it.polimi.ingsw.am56.model.state.GameEvent;
import it.polimi.ingsw.am56.model.state.State;
import it.polimi.ingsw.am56.model.state.TotemPlacement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameModelTest {

    private GameModel gameModel;

    @BeforeEach
    void setUp() throws InvalidActionException {
        gameModel = new GameModel();
        gameModel.initializeGame(2);
    }

    @Test
    void constructorTest() {
        assertNotNull(gameModel);
        assertNotNull(gameModel.getPlayers());
        assertNotNull(gameModel.getPlayerToOfferTile());
        assertNotNull(gameModel.getPlayerToOrderTile());
        assertNotNull(gameModel.getEraToBuildingsdeck());
        assertNotNull(gameModel.getEraToCharEvdeck());
    }

    @Test
    void observersTest() {
        GameObserver gameObserver = new GameObserver() {
            @Override
            public void onModelChanged(GameEvent event) {}
        };

        gameModel.addObserver(gameObserver);
        gameModel.addObserver(gameObserver);

        gameModel.removeObserver(gameObserver);

        gameModel.addObserver(gameObserver);

        gameModel.setState(new TotemPlacement(gameModel));

    }

    @Test
    void wrongGameSizeInitialize() {

        assertThrows(InvalidActionException.class, () -> { gameModel.initializeGame(1); });
        assertThrows(InvalidActionException.class, () -> { gameModel.initializeGame(6); });
    }

    @Test
    void initializeTest() {

        assertEquals(2, gameModel.getGameSize());
        assertNotNull(gameModel.getBoard());

        assertEquals(gameModel.getGameSize() + 1, gameModel.getBoard().getLowerRowCharEv().size());
        assertEquals(gameModel.getGameSize() + 4, gameModel.getBoard().getUpperRowCharEv().size());
        assertFalse(gameModel.getBoard().getUpperRowBuildings().isEmpty());

        for(int era = 1; era <= 3; era++) {
            assertNotNull(gameModel.getEraToCharEvdeck().get(era));
            assertFalse(gameModel.getEraToCharEvdeck().get(era).isEmpty());

            assertNotNull(gameModel.getEraToBuildingsdeck().get(era));
            // assertFalse(gameModel.getEraToBuildingsdeck().get(era).isEmpty());
        }
    }

    @Test
    void setOrderPlayerTest() {
        gameModel.getPlayers().add(new Player("a", "a"));
        gameModel.getPlayers().add(new Player("b", "b"));

        gameModel.getPlayerToOrderTile().put(gameModel.getPlayers().getFirst(), gameModel.getBoard().getOrderTiles().get(1));
        gameModel.getPlayerToOrderTile().put(gameModel.getPlayers().get(1), gameModel.getBoard().getOrderTiles().getFirst());

        gameModel.setCurrentPlayerOrder();

        assertEquals(gameModel.getPlayers().get(1), gameModel.getCurrentPlayer());
    }

    @Test
    void setOfferPlayerTest() {
        gameModel.getPlayers().add(new Player("a", "a"));
        gameModel.getPlayers().add(new Player("b", "b"));

        gameModel.getPlayerToOfferTile().put(gameModel.getPlayers().getFirst(), gameModel.getBoard().getOfferTiles().get(1));
        gameModel.getPlayerToOfferTile().put(gameModel.getPlayers().get(1), gameModel.getBoard().getOfferTiles().getFirst());

        gameModel.setCurrentPlayerOffer();

        assertEquals(gameModel.getPlayers().get(1), gameModel.getCurrentPlayer());
    }

    @Test
    void TotemPlacementPhaseTest() throws InvalidActionException {
        gameModel.getPlayers().add(new Player("a", "a"));
        gameModel.getPlayers().add(new Player("b", "b"));

        gameModel.getPlayerToOrderTile().put(gameModel.getPlayers().getFirst(), gameModel.getBoard().getOrderTiles().getFirst());
        gameModel.getPlayerToOrderTile().put(gameModel.getPlayers().get(1), gameModel.getBoard().getOrderTiles().get(1));

        gameModel.getBoard().getOrderTiles().getFirst().setIsFree(false);
        gameModel.getBoard().getOrderTiles().get(1).setIsFree(false);

        gameModel.setState(new TotemPlacement(gameModel));

        gameModel.setCurrentPlayerOrder();

        gameModel.handleTotemPlacement(gameModel.getCurrentPlayer(), gameModel.getBoard().getOfferTiles().getFirst());

        assertTrue(gameModel.getBoard().getOrderTiles().getFirst().isFree());
        assertFalse(gameModel.getBoard().getOfferTiles().getFirst().isFree());

        assertFalse(gameModel.getPlayerToOrderTile().containsKey(gameModel.getPlayers().getFirst()));
        assertTrue(gameModel.getPlayerToOfferTile().containsKey(gameModel.getPlayers().getFirst()));
        assertEquals(gameModel.getBoard().getOfferTiles().getFirst(), gameModel.getPlayerToOfferTile().get(gameModel.getPlayers().getFirst()));

        assertEquals(gameModel.getCurrentPlayer(), gameModel.getPlayers().get(1));

        gameModel.handleTotemPlacement(gameModel.getCurrentPlayer(), gameModel.getBoard().getOfferTiles().get(1));

        assertTrue(gameModel.getBoard().getOrderTiles().get(1).isFree());
        assertFalse(gameModel.getBoard().getOfferTiles().get(1).isFree());

        assertFalse(gameModel.getPlayerToOrderTile().containsKey(gameModel.getPlayers().get(1)));
        assertTrue(gameModel.getPlayerToOfferTile().containsKey(gameModel.getPlayers().get(1)));
        assertEquals(gameModel.getBoard().getOfferTiles().get(1), gameModel.getPlayerToOfferTile().get(gameModel.getPlayers().get(1)));

        assertEquals(gameModel.getCurrentPlayer(), gameModel.getPlayers().getFirst());
        assertEquals(State.CARD_PICKING, gameModel.getCurrentState().getState());
    }

    @Test
    void checkCanPickTest() {
        gameModel.getPlayers().add(new Player("a", "a"));
        gameModel.getPlayers().add(new Player("b", "b"));

        gameModel.getPlayerToOfferTile().put(gameModel.getPlayers().getFirst(), gameModel.getBoard().getOfferTiles().get(1));
        gameModel.getPlayerToOfferTile().put(gameModel.getPlayers().get(1), gameModel.getBoard().getOfferTiles().get(0));

        gameModel.setCurrentPlayer(gameModel.getPlayers().getFirst());

        gameModel.getBoard().getUpperRowCharEv().clear();
        assertFalse(gameModel.checkCanPick());

        gameModel.getCurrentPlayer().getStats().addFood(10);
        assertTrue(gameModel.checkCanPick());

        gameModel.setCurrentPlayer(gameModel.getPlayers().get(1));

        gameModel.getBoard().getLowerRowBuildings().add(new AddPointsEndGame(1, 1, 1, 10, 1, 1));
        gameModel.getBoard().getLowerRowCharEv().clear();
        assertFalse(gameModel.checkCanPick());

        gameModel.getCurrentPlayer().getStats().addFood(10);
        assertTrue(gameModel.checkCanPick());

    }

    @Test
    void CardPickingPhaseTest() throws InvalidActionException {
        gameModel.getPlayers().add(new Player("a", "a"));
        gameModel.getPlayers().add(new Player("b", "b"));

        gameModel.getPlayerToOfferTile().put(gameModel.getPlayers().getFirst(), gameModel.getBoard().getOfferTiles().get(2));
        gameModel.getPlayerToOfferTile().put(gameModel.getPlayers().get(1), gameModel.getBoard().getOfferTiles().get(3));

        gameModel.getBoard().getOfferTiles().get(2).setIsFree(false);
        gameModel.getBoard().getOfferTiles().get(3).setIsFree(false);

        // si può rimuovere
        gameModel.getBoard().getOrderTiles().getFirst().setIsFree(true);
        gameModel.getBoard().getOrderTiles().get(1).setIsFree(true);

        gameModel.setCurrentPlayerOffer();

        gameModel.setState(new CardPicking(gameModel));

        BuilderCharCard builderCharCard = new BuilderCharCard(1, 1, 1, 2, 1);
        gameModel.getBoard().getLowerRowCharEv().add(builderCharCard);

        gameModel.pickUpCard(gameModel.getCurrentPlayer(), builderCharCard);

        assertTrue(gameModel.getPlayers().getFirst().getStats().getCharacterCards().contains(builderCharCard));
        assertFalse(gameModel.getBoard().getLowerRowCharEv().contains(builderCharCard));

        assertEquals(2, gameModel.getCurrentPlayer().getStats().getBuildingSale());

        assertEquals(1, gameModel.getPlayers().getFirst().getPickedLower());
        assertEquals(0, gameModel.getPlayers().getFirst().getPickedUpper());


        BuildingCard buildingPicked =  new AddPointsEndGame(1, 1, 1, 5, 1, 1);
        gameModel.getBoard().getUpperRowBuildings().add(buildingPicked);

        gameModel.getCurrentPlayer().getStats().addFood(10);
        gameModel.pickUpCard(gameModel.getCurrentPlayer(), buildingPicked);

        assertEquals(10 - buildingPicked.getCost() + gameModel.getPlayers().getFirst().getStats().getBuildingSale(),
                gameModel.getPlayers().getFirst().getStats().getFood() - gameModel.getPlayerToOrderTile().get(gameModel.getPlayers().getFirst()).getFoodEffect());
        assertTrue(gameModel.getPlayers().getFirst().getStats().getBuildingCards().contains(buildingPicked));
        assertFalse(gameModel.getBoard().getUpperRowBuildings().contains(buildingPicked));

        assertEquals(gameModel.getCurrentPlayer(), gameModel.getPlayers().get(1));
    }

    @Test
    void resetTotemOrderTest() throws InvalidActionException {
        gameModel.getPlayers().add(new Player("a", "a"));
        gameModel.getPlayers().add(new Player("b", "b"));

        gameModel.getPlayerToOfferTile().put(gameModel.getPlayers().getFirst(), gameModel.getBoard().getOfferTiles().getFirst());
        gameModel.getPlayerToOfferTile().put(gameModel.getPlayers().get(1), gameModel.getBoard().getOfferTiles().get(1));

        gameModel.getBoard().getOfferTiles().getFirst().setIsFree(false);
        gameModel.getBoard().getOfferTiles().get(1).setIsFree(false);

        // si può rimuovere
        gameModel.getBoard().getOrderTiles().getFirst().setIsFree(true);
        gameModel.getBoard().getOrderTiles().get(1).setIsFree(true);

        assertEquals(0, gameModel.getPlayers().getFirst().getStats().getFood());
        assertEquals(0, gameModel.getPlayers().get(1).getStats().getFood());

        gameModel.setCurrentPlayerOffer();

        gameModel.resetTotemToOrder();

        assertTrue(gameModel.getPlayerToOrderTile().containsKey(gameModel.getPlayers().getFirst()));
        assertEquals(gameModel.getBoard().getOrderTiles().getFirst(), gameModel.getPlayerToOrderTile().get(gameModel.getPlayers().getFirst()));

        assertFalse(gameModel.getBoard().getOrderTiles().getFirst().isFree());
        assertTrue(gameModel.getBoard().getOfferTiles().getFirst().isFree());

        assertEquals(0, gameModel.getPlayers().getFirst().getPickedLower());
        assertEquals(0, gameModel.getPlayers().getFirst().getPickedUpper());

        assertEquals(1, gameModel.getPlayers().getFirst().getStats().getFood());
        assertEquals(0, gameModel.getPlayers().getFirst().getStats().getPoints());

        gameModel.setCurrentPlayerOffer();

        gameModel.resetTotemToOrder();

        assertTrue(gameModel.getPlayerToOrderTile().containsKey(gameModel.getPlayers().get(1)));
        assertEquals(gameModel.getBoard().getOrderTiles().get(1), gameModel.getPlayerToOrderTile().get(gameModel.getPlayers().get(1)));

        assertFalse(gameModel.getBoard().getOrderTiles().get(1).isFree());
        assertTrue(gameModel.getBoard().getOfferTiles().get(1).isFree());

        assertEquals(0, gameModel.getPlayers().get(1).getPickedLower());
        assertEquals(0, gameModel.getPlayers().get(1).getPickedUpper());

        assertEquals(0, gameModel.getPlayers().get(1).getStats().getFood());
        assertEquals(-2, gameModel.getPlayers().get(1).getStats().getPoints());


        gameModel.getPlayerToOfferTile().put(gameModel.getPlayers().getFirst(), gameModel.getBoard().getOfferTiles().getFirst());
        gameModel.getPlayerToOrderTile().remove(gameModel.getPlayers().getFirst());
        gameModel.setCurrentPlayerOffer();
        gameModel.getBoard().getOrderTiles().get(1).setIsFree(true);

        gameModel.resetTotemToOrder();

        assertEquals(0, gameModel.getPlayers().getFirst().getStats().getFood());
        assertEquals(0, gameModel.getPlayers().getFirst().getStats().getPoints());
    }

    @Test
    void endRoundTest() {
        gameModel.getPlayers().add(new Player("a", "a"));
        gameModel.getPlayers().add(new Player("b", "b"));

        gameModel.getPlayerToOrderTile().put(gameModel.getPlayers().getFirst(), gameModel.getBoard().getOrderTiles().getFirst());
        gameModel.getPlayerToOrderTile().put(gameModel.getPlayers().get(1), gameModel.getBoard().getOrderTiles().get(1));

        gameModel.getBoard().getLowerRowCharEv().clear();
        gameModel.getBoard().getUpperRowCharEv().clear();

        ShamansEvCard shamansEvCard = new ShamansEvCard(1, 1, 1, false, 5, 3);
        ShamanNoLoss shamanNoLoss = new ShamanNoLoss(1, 1, 1, 1, 1);
        ShamanEventDouble shamanEventDouble = new ShamanEventDouble(1, 1, 1, 1, 1);

        gameModel.getBoard().getLowerRowCharEv().add(shamansEvCard);
        gameModel.getPlayers().getFirst().getStats().addShamanStars(3);
        gameModel.getPlayers().getFirst().getStats().getBuildingCards().add(shamanEventDouble);
        gameModel.getPlayers().get(1).getStats().getBuildingCards().add(shamanNoLoss);

        gameModel.endRound();

        assertEquals(10, gameModel.getPlayers().getFirst().getStats().getPoints());
        assertEquals(0, gameModel.getPlayers().get(1).getStats().getPoints());

        assertEquals(0, gameModel.getBoard().getLowerRowCharEv().size());
        assertEquals(gameModel.getGameSize() + 4, gameModel.getBoard().getUpperRowCharEv().size());

        assertTrue(gameModel.getBoard().getLowerRowBuildings().isEmpty());
        assertEquals(2, gameModel.getCurrentRound());
    }

    @Test
    void endEraTest() {

        gameModel.getBoard().getLowerRowCharEv().clear();
        gameModel.getBoard().getUpperRowCharEv().clear();
        assertTrue(gameModel.getBoard().getLowerRowBuildings().isEmpty());

        gameModel.getEraToCharEvdeck().get(1).getRemainingCards().clear();

        gameModel.endRound();

        assertEquals(2, gameModel.getCurrentEra());
        assertEquals(2, gameModel.getCurrentRound());

        assertFalse(gameModel.getBoard().getLowerRowBuildings().isEmpty());
        assertFalse(gameModel.getBoard().getUpperRowBuildings().isEmpty());

        assertEquals(0, gameModel.getBoard().getLowerRowCharEv().size());
        assertEquals(gameModel.getGameSize() + 4, gameModel.getBoard().getUpperRowCharEv().size());


    }

    @Test
    void endGameActionsTest() {
        gameModel.getPlayers().add(new Player("a", "a"));
        gameModel.getPlayers().add(new Player("b", "b"));
        gameModel.getPlayers().add(new Player("c", "c"));

        gameModel.getPlayers().getFirst().getStats().addBuilderPoints(3);

        gameModel.getPlayers().get(1).getStats().getCharacterCards().add(new InventorCharCard(1, 1, 1, "PANE"));
        gameModel.getPlayers().get(1).getStats().getInventions().add("PANE");
        gameModel.getPlayers().get(1).getStats().getBuildingCards().add(new AddPointsEndGame(1, 1, 1, 1, 9, 10));

        gameModel.getPlayers().get(2).getStats().getCharacterCards().add(new ArtistCharCard(1, 1, 1));
        gameModel.getPlayers().get(2).getStats().getCharacterCards().add(new ArtistCharCard(2, 1, 1));

        gameModel.endGameActions();

        assertEquals(3, gameModel.getPlayers().getFirst().getStats().getPoints());
        assertEquals(20, gameModel.getPlayers().get(1).getStats().getPoints());
        assertEquals(10, gameModel.getPlayers().get(2).getStats().getPoints());
    }

    @Test
    void computeRankingDistinctScoresTest() {
        Player a = new Player("a", "a");
        Player b = new Player("b", "b");
        Player c = new Player("c", "c");
        gameModel.getPlayers().add(a);
        gameModel.getPlayers().add(b);
        gameModel.getPlayers().add(c);

        a.getStats().addPoints(10);
        b.getStats().addPoints(5);
        c.getStats().addPoints(1);

        List<RankingEntry> ranking = gameModel.computeRanking();

        assertEquals(3, ranking.size());
        assertEquals(a, ranking.get(0).player());
        assertEquals(1, ranking.get(0).rank());
        assertTrue(ranking.get(0).isWinner());
        assertEquals(2, ranking.get(1).rank());
        assertFalse(ranking.get(1).isWinner());
        assertEquals(3, ranking.get(2).rank());
        assertFalse(ranking.get(2).isWinner());
    }

    @Test
    void computeRankingTieBreakOnFoodTest() {
        Player a = new Player("a", "a");
        Player b = new Player("b", "b");
        gameModel.getPlayers().add(a);
        gameModel.getPlayers().add(b);

        a.getStats().addPoints(10);
        a.getStats().addFood(2);
        b.getStats().addPoints(10);
        b.getStats().addFood(5);

        List<RankingEntry> ranking = gameModel.computeRanking();

        assertEquals(b, ranking.get(0).player());
        assertEquals(1, ranking.get(0).rank());
        assertTrue(ranking.get(0).isWinner());
        assertEquals(a, ranking.get(1).player());
        assertEquals(2, ranking.get(1).rank());
        assertFalse(ranking.get(1).isWinner());
    }

    @Test
    void computeRankingSharedVictoryTest() {
        Player a = new Player("a", "a");
        Player b = new Player("b", "b");
        Player c = new Player("c", "c");
        gameModel.getPlayers().add(a);
        gameModel.getPlayers().add(b);
        gameModel.getPlayers().add(c);

        a.getStats().addPoints(10);
        a.getStats().addFood(5);
        b.getStats().addPoints(10);
        b.getStats().addFood(5);
        c.getStats().addPoints(1);

        List<RankingEntry> ranking = gameModel.computeRanking();

        assertEquals(1, ranking.get(0).rank());
        assertEquals(1, ranking.get(1).rank());
        assertTrue(ranking.get(0).isWinner());
        assertTrue(ranking.get(1).isWinner());
        assertEquals(c, ranking.get(2).player());
        assertEquals(3, ranking.get(2).rank());
        assertFalse(ranking.get(2).isWinner());
    }

    @Test
    void computeRankingEmptyTest() {
        assertTrue(gameModel.computeRanking().isEmpty());
    }

    @Test
    void hasPlayerIsCaseInsensitiveTest() {
        gameModel.getPlayers().add(new Player("Anna", "a"));

        assertTrue(gameModel.hasPlayer("anna"));
        assertTrue(gameModel.hasPlayer("ANNA"));
        assertFalse(gameModel.hasPlayer("bob"));
    }

    @Test
    void syncAndDisconnectNotifyObserversTest() {
        List<GameEvent> received = new ArrayList<>();
        gameModel.addObserver(received::add);

        gameModel.syncLobbyState();
        gameModel.syncGameState();
        gameModel.notifyDisconnection();

        assertTrue(received.contains(GameEvent.LOBBY_STATE_SYNC));
        assertTrue(received.contains(GameEvent.GAME_STARTED));
        assertTrue(received.contains(GameEvent.PLAYER_DISCONNECTED));
    }

    @Test
    void startExtraUpperCardPickingIfNeededTest() {
        Player p = new Player("a", "a");
        gameModel.getPlayers().add(p);
        p.getStats().getBuildingCards().add(new ExtraUpperCardAfterActions(99, 1, 1, 0, 0));

        gameModel.getBoard().getUpperRowCharEv().clear();
        gameModel.getBoard().getUpperRowCharEv().add(new ArtistCharCard(1, 1, 1));

        assertTrue(gameModel.startExtraUpperCardPickingIfNeeded());
        assertEquals(p, gameModel.getCurrentPlayer());
    }

    @Test
    void startExtraUpperCardPickingNotNeededTest() {
        gameModel.getPlayers().add(new Player("a", "a"));
        assertFalse(gameModel.startExtraUpperCardPickingIfNeeded());
    }

    @Test
    void executeExtraUpperCardPickTest() throws InvalidActionException {
        Player p = new Player("a", "a");
        gameModel.getPlayers().add(p);
        gameModel.setCurrentPlayer(p);

        ArtistCharCard c = new ArtistCharCard(1, 1, 1);
        gameModel.getBoard().getUpperRowCharEv().add(c);

        gameModel.executeExtraUpperCardPick(p, c);

        assertTrue(p.getStats().getCharacterCards().contains(c));
        assertFalse(gameModel.getBoard().getUpperRowCharEv().contains(c));
        assertEquals(0, p.getPickedUpper());
        assertEquals(0, p.getPickedLower());
    }

    @Test
    void lastEventDeltasTest() {
        assertTrue(gameModel.getLastEventDeltas().isEmpty());

        gameModel.getPlayers().add(new Player("a", "a"));
        gameModel.getPlayers().add(new Player("b", "b"));

        gameModel.getPlayerToOrderTile().put(gameModel.getPlayers().getFirst(), gameModel.getBoard().getOrderTiles().getFirst());
        gameModel.getPlayerToOrderTile().put(gameModel.getPlayers().get(1), gameModel.getBoard().getOrderTiles().get(1));

        gameModel.getBoard().getLowerRowCharEv().clear();
        gameModel.getBoard().getUpperRowCharEv().clear();

        ShamansEvCard shamansEvCard = new ShamansEvCard(42, 1, 1, false, 5, 3);
        gameModel.getBoard().getLowerRowCharEv().add(shamansEvCard);
        gameModel.getPlayers().getFirst().getStats().addShamanStars(3);
        gameModel.getPlayers().getFirst().getStats().getBuildingCards().add(new ShamanEventDouble(1, 1, 1, 1, 1));
        gameModel.getPlayers().get(1).getStats().getBuildingCards().add(new ShamanNoLoss(1, 1, 1, 1, 1));

        gameModel.endRound();

        assertEquals(42, gameModel.getLastResolvedEventId());
        assertEquals(10, gameModel.getLastEventDeltas().get("a")[1]);
        assertEquals(0, gameModel.getLastEventDeltas().get("b")[1]);
    }

    @Test
    void moveFinalEventsLastInEra3Test() {
        List<Card> remaining = gameModel.getEraToCharEvdeck().get(3).getRemainingCards();
        int n = remaining.size();

        long finalCount = remaining.stream()
                .filter(card -> card.getCardType() == CardType.EVENT && ((EventCard) card).isFinal())
                .count();

        assertTrue(finalCount > 0);

        for (int i = n - (int) finalCount; i < n; i++) {
            Card card = remaining.get(i);
            assertTrue(card.getCardType() == CardType.EVENT && ((EventCard) card).isFinal());
        }
    }

    @Test
    void serializationRoundTripReinitializesObserversTest() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(gameModel);
        }

        GameModel loaded;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            loaded = (GameModel) ois.readObject();
        }

        assertNotNull(loaded);
        assertEquals(gameModel.getGameSize(), loaded.getGameSize());
        assertEquals(gameModel.getCurrentState().getState(), loaded.getCurrentState().getState());

        loaded.addObserver(event -> {});
        assertDoesNotThrow(loaded::syncLobbyState);
    }

    @Test
    void skipExtraPickInWrongPhaseThrowsTest() {
        assertThrows(InvalidActionException.class, () -> gameModel.skipExtraUpperCardPick(new Player("a", "a")));
    }
}