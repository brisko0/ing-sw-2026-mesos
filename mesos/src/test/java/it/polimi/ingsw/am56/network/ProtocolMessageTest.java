package it.polimi.ingsw.am56.network;

import it.polimi.ingsw.am56.controller.GameController;
import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.state.State;
import it.polimi.ingsw.am56.network.message.classesDTO.*;
import it.polimi.ingsw.am56.network.message.request.*;
import it.polimi.ingsw.am56.network.message.update.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProtocolMessageTest {

    @SuppressWarnings("unchecked")
    private static <T extends Serializable> T roundTrip(T obj) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            return (T) ois.readObject();
        }
    }

    private static BoardDTO sampleBoard() {
        return new BoardDTO(List.of(1), List.of(2), List.of(3), List.of(4), List.of("C"), 5, List.of(6));
    }

    private static PlayerStatsDTO sampleStats() {
        return new PlayerStatsDTO(
                "alice", "RED", 3, 7,
                List.of(1), List.of(2), List.of(3), List.of(4), List.of(5), List.of(6),
                1, 2, 3, 4,
                true, false,
                List.of(7), List.of(8));
    }

    private static CurrentPlayerDTO sampleCurrent() {
        return new CurrentPlayerDTO("alice", "RED");
    }

    private static List<ClientRequest> allRequests() {
        return List.of(
                new CreateGameRequest("alice", "RED", 2),
                new AddPlayerRequest("bob", "BLUE"),
                new TotemMoveRequest("alice", "C"),
                new CardPickRequest("alice", 42),
                new SkipExtraPickRequest("alice"),
                new ReconnectRequest("alice"),
                new LobbyStateRequest(),
                new GameSyncRequest(),
                new PingRequest());
    }

    private static List<ServerMessage> allMessages() {
        TotemPositionDTO totem = new TotemPositionDTO("RED", "OFFER", "C");
        GameStateDTO gameState = new GameStateDTO(State.CARD_PICKING);
        EndGameDTO endGame = new EndGameDTO(1, "alice", 30, 5, true, "2026-01-01");
        EventOutcomeDTO outcome = new EventOutcomeDTO("alice", "RED", 1, 2);

        GameSnapshotDTO snapshot = new GameSnapshotDTO(
                sampleBoard(), List.of(sampleStats()), List.of(totem), sampleCurrent(), gameState, 1, 1);
        EndRoundSnapshotDTO endRound = new EndRoundSnapshotDTO(
                sampleBoard(), List.of(sampleStats()), sampleCurrent(), 1, 1);

        return List.of(
                new PreLobbyStateMessage(true, false, List.of("alice"), List.of("RED")),
                new LobbyStateMessage(List.of(new PlayerDTO("alice", "RED")), 2),
                new BoardUpdatedMessage(sampleBoard()),
                new PlayerStatsUpdateMessage(sampleStats()),
                new TotemsStateMessage(List.of(totem)),
                new CurrentPlayerUpdateMessage(sampleCurrent()),
                new GameSnapshotMessage(snapshot),
                new GameStateUpdateMessage(gameState),
                new EndRoundUpdateMessage(endRound),
                new EventResolvedMessage(7, List.of(outcome)),
                new GameEndedMessage(List.of(endGame), List.of(endGame), 2),
                new GameAbortedMessage(),
                new ErrorMessage("invalid action"),
                new PingMessage());
    }

    @Test
    void allClientRequestsSurviveSerialization() throws Exception {
        for (ClientRequest request : allRequests()) {
            assertEquals(request, roundTrip(request));
        }
    }

    @Test
    void allServerMessagesSurviveSerialization() throws Exception {
        for (ServerMessage message : allMessages()) {
            assertEquals(message, roundTrip(message));
        }
    }

    @Test
    void serverMessageAcceptDispatchesToMatchingVisitOverload() {
        for (ServerMessage message : allMessages()) {
            RecordingMessageVisitor visitor = new RecordingMessageVisitor();
            message.accept(visitor);
            assertEquals(message.getClass(), visitor.visited);
        }
    }

    @Test
    void clientRequestRoutesToCorrectControllerMethod() throws Exception {
        assertRoutesTo(new CreateGameRequest("alice", "RED", 2), "createGame");
        assertRoutesTo(new AddPlayerRequest("bob", "BLUE"), "addPlayer");
        assertRoutesTo(new TotemMoveRequest("alice", "C"), "moveTotem");
        assertRoutesTo(new CardPickRequest("alice", 42), "cardPick");
        assertRoutesTo(new SkipExtraPickRequest("alice"), "skipExtraPick");
        assertRoutesTo(new ReconnectRequest("alice"), "reconnectPlayer");
        assertRoutesTo(new LobbyStateRequest(), "checkLobbyState");
        assertRoutesTo(new GameSyncRequest(), "sendGameState");
    }

    @Test
    void pingRequestDoesNotInvokeAnyControllerMethod() throws Exception {
        RecordingGameController controller = new RecordingGameController();
        new PingRequest().executeOnServer(controller);
        assertNull(controller.lastCall);
    }

    private void assertRoutesTo(ClientRequest request, String expectedMethod) throws Exception {
        RecordingGameController controller = new RecordingGameController();
        request.executeOnServer(controller);
        assertEquals(expectedMethod, controller.lastCall);
    }

    static class RecordingGameController extends GameController {
        String lastCall;

        RecordingGameController() {
            super(new GameModel());
        }

        @Override
        public void createGame(String nickname, String totemColor, int gameSize) {
            lastCall = "createGame";
        }

        @Override
        public void addPlayer(String nickname, String totemColor) {
            lastCall = "addPlayer";
        }

        @Override
        public void moveTotem(String nickname, String offerTileLetter) {
            lastCall = "moveTotem";
        }

        @Override
        public void cardPick(String nickname, int cardId) {
            lastCall = "cardPick";
        }

        @Override
        public void skipExtraPick(String nickname) {
            lastCall = "skipExtraPick";
        }

        @Override
        public void reconnectPlayer(String nickname) {
            lastCall = "reconnectPlayer";
        }

        @Override
        public void checkLobbyState() {
            lastCall = "checkLobbyState";
        }

        @Override
        public void sendGameState() {
            lastCall = "sendGameState";
        }
    }

    static class RecordingMessageVisitor implements MessageVisitor {
        Class<?> visited;

        @Override public void visit(PreLobbyStateMessage message) { visited = PreLobbyStateMessage.class; }
        @Override public void visit(LobbyStateMessage message) { visited = LobbyStateMessage.class; }
        @Override public void visit(BoardUpdatedMessage message) { visited = BoardUpdatedMessage.class; }
        @Override public void visit(PlayerStatsUpdateMessage message) { visited = PlayerStatsUpdateMessage.class; }
        @Override public void visit(TotemsStateMessage message) { visited = TotemsStateMessage.class; }
        @Override public void visit(CurrentPlayerUpdateMessage message) { visited = CurrentPlayerUpdateMessage.class; }
        @Override public void visit(GameSnapshotMessage message) { visited = GameSnapshotMessage.class; }
        @Override public void visit(GameStateUpdateMessage message) { visited = GameStateUpdateMessage.class; }
        @Override public void visit(EndRoundUpdateMessage message) { visited = EndRoundUpdateMessage.class; }
        @Override public void visit(EventResolvedMessage message) { visited = EventResolvedMessage.class; }
        @Override public void visit(GameEndedMessage message) { visited = GameEndedMessage.class; }
        @Override public void visit(GameAbortedMessage message) { visited = GameAbortedMessage.class; }
        @Override public void visit(ErrorMessage message) { visited = ErrorMessage.class; }
        @Override public void visit(PingMessage message) { visited = PingMessage.class; }
    }
}
