package it.polimi.ingsw.am56.network;

import it.polimi.ingsw.am56.controller.EndGameService;
import it.polimi.ingsw.am56.controller.GameController;
import it.polimi.ingsw.am56.controller.PlayerVirtualView;
import it.polimi.ingsw.am56.database.LeaderboardDAO;
import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.board.OfferTile;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;
import it.polimi.ingsw.am56.model.state.GameEnding;
import it.polimi.ingsw.am56.model.state.State;
import it.polimi.ingsw.am56.network.message.classesDTO.EndGameDTO;
import it.polimi.ingsw.am56.network.message.request.AddPlayerRequest;
import it.polimi.ingsw.am56.network.message.request.ClientRequest;
import it.polimi.ingsw.am56.network.message.request.CreateGameRequest;
import it.polimi.ingsw.am56.network.message.request.GameSyncRequest;
import it.polimi.ingsw.am56.network.message.request.TotemMoveRequest;
import it.polimi.ingsw.am56.network.message.update.CurrentPlayerUpdateMessage;
import it.polimi.ingsw.am56.network.message.update.GameAbortedMessage;
import it.polimi.ingsw.am56.network.message.update.GameEndedMessage;
import it.polimi.ingsw.am56.network.message.update.GameSnapshotMessage;
import it.polimi.ingsw.am56.network.message.update.GameStateUpdateMessage;
import it.polimi.ingsw.am56.network.message.update.LobbyStateMessage;
import it.polimi.ingsw.am56.network.message.update.PreLobbyStateMessage;
import it.polimi.ingsw.am56.network.message.update.TotemsStateMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServerIntegrationTest {

    private GameModel model;
    private GameController controller;
    private FakeLeaderboardDAO dao;
    private FakeClientConnection conn1;
    private FakeClientConnection conn2;

    static class FakeLeaderboardDAO implements LeaderboardDAO {
        int saveCalls = 0;
        int lastGameSize = -1;
        List<EndGameDTO> lastResults = List.of();

        @Override
        public void saveMatchResults(int gameSize, List<EndGameDTO> results, LocalDate date) {
            saveCalls++;
            lastGameSize = gameSize;
            lastResults = results;
        }

        @Override
        public List<EndGameDTO> getGlobalLeaderboard(int gameSize) {
            return List.of();
        }
    }

    @BeforeEach
    void setUp() {
        model = new GameModel();
        controller = new GameController(model);
        dao = new FakeLeaderboardDAO();
        EndGameService endGameService = new EndGameService(model, dao);
        conn1 = new FakeClientConnection();
        conn2 = new FakeClientConnection();

        model.addObserver(endGameService);
        model.addObserver(new PlayerVirtualView(model, conn1, endGameService));
        model.addObserver(new PlayerVirtualView(model, conn2, endGameService));
    }

    private void handleFor(FakeClientConnection conn, ClientRequest request) {
        try {
            request.executeOnServer(controller);
        } catch (InvalidActionException e) {
            conn.sendError(e.getMessage());
        }
    }

    private void startTwoPlayerGame() {
        handleFor(conn1, new CreateGameRequest("alice", "RED", 2));
        handleFor(conn2, new AddPlayerRequest("bob", "BLUE"));
    }

    private FakeClientConnection connFor(String nickname) {
        return nickname.equals("alice") ? conn1 : conn2;
    }

    private String firstFreeOfferLetter() {
        return model.getBoard().getOfferTiles().stream()
                .filter(OfferTile::isFree)
                .findFirst()
                .orElseThrow()
                .getLetter();
    }

    @Test
    void lobbyCreateAndJoinBroadcastsToAllClients() {
        handleFor(conn1, new CreateGameRequest("alice", "RED", 2));

        assertTrue(conn1.hasMessageOfType(PreLobbyStateMessage.class));
        assertTrue(conn2.hasMessageOfType(PreLobbyStateMessage.class));
        assertTrue(conn1.hasMessageOfType(LobbyStateMessage.class));
        assertTrue(conn2.hasMessageOfType(LobbyStateMessage.class));
        assertTrue(model.hasPlayer("alice"));
        assertEquals(State.PLAYERS_FILLING, model.getCurrentState().getState());

        handleFor(conn2, new AddPlayerRequest("bob", "BLUE"));

        assertEquals(2, model.getPlayers().size());
        assertEquals(State.TOTEM_PLACEMENT, model.getCurrentState().getState());
        assertNotNull(model.getCurrentPlayer());
        assertTrue(conn1.hasMessageOfType(GameStateUpdateMessage.class));
        assertTrue(conn2.hasMessageOfType(CurrentPlayerUpdateMessage.class));
    }

    @Test
    void duplicateNicknameRejectedAndOnlyCulpritNotified() {
        handleFor(conn1, new CreateGameRequest("alice", "RED", 2));

        handleFor(conn2, new AddPlayerRequest("alice", "BLUE"));

        assertEquals(1, conn2.errors.size());
        assertEquals(0, conn1.errors.size());
        assertEquals(1, model.getPlayers().size());
    }

    @Test
    void gameSyncRequestSendsFullSnapshotToClients() {
        startTwoPlayerGame();
        conn1.messages.clear();
        conn2.messages.clear();

        handleFor(conn1, new GameSyncRequest());

        assertTrue(conn1.hasMessageOfType(GameSnapshotMessage.class));
        assertTrue(conn2.hasMessageOfType(GameSnapshotMessage.class));

        GameSnapshotMessage snap = conn1.messagesOfType(GameSnapshotMessage.class).getFirst();
        assertEquals(2, snap.snapshot().playersStats().size());
    }

    @Test
    void totemMoveOutOfTurnNotifiesOnlyCulprit() {
        startTwoPlayerGame();
        Player current = model.getCurrentPlayer();
        String other = current.getNickname().equals("alice") ? "bob" : "alice";

        handleFor(connFor(other), new TotemMoveRequest(other, firstFreeOfferLetter()));

        assertEquals(1, connFor(other).errors.size());
        assertEquals(0, connFor(current.getNickname()).errors.size());
    }

    @Test
    void fullTotemPlacementAdvancesToCardPicking() {
        startTwoPlayerGame();

        while (model.getCurrentState().getState() == State.TOTEM_PLACEMENT) {
            Player current = model.getCurrentPlayer();
            handleFor(connFor(current.getNickname()),
                    new TotemMoveRequest(current.getNickname(), firstFreeOfferLetter()));
        }

        assertEquals(State.CARD_PICKING, model.getCurrentState().getState());
        assertTrue(conn1.hasMessageOfType(TotemsStateMessage.class));
        assertTrue(conn2.hasMessageOfType(TotemsStateMessage.class));
    }

    @Test
    void disconnectionBroadcastsGameAbortedToAllClients() {
        startTwoPlayerGame();

        model.notifyDisconnection();

        assertTrue(conn1.hasMessageOfType(GameAbortedMessage.class));
        assertTrue(conn2.hasMessageOfType(GameAbortedMessage.class));
    }

    @Test
    void endGamePersistsRankingAndSendsResultsToAllClients() {
        startTwoPlayerGame();

        model.setState(new GameEnding(model));

        assertEquals(1, dao.saveCalls);
        assertEquals(2, dao.lastGameSize);
        assertEquals(2, dao.lastResults.size());
        assertTrue(conn1.hasMessageOfType(GameEndedMessage.class));
        assertTrue(conn2.hasMessageOfType(GameEndedMessage.class));
    }
}
