package it.polimi.ingsw.am56.network.message.update;

public interface MessageVisitor {
    // Un metodo per ogni DTO specifico
    default void visit(PreLobbyStateMessage message) {}
    default void visit(LobbyStateMessage message) {}
    default void visit(BoardUpdatedMessage message) {}
    default void visit(PlayerStatsUpdateMessage message) {}
    default void visit(TotemsStateMessage message) {}
    default void visit(CurrentPlayerUpdateMessage message) {}
    default void visit(GameSnapshotMessage message) {}
    default void visit(GameStateUpdateMessage message) {}
    default void visit(EndRoundUpdateMessage message) {}
    default void visit(EventResolvedMessage message) {}
    default void visit(GameEndedMessage message) {}
    default void visit(GameAbortedMessage message) {}
    default void visit(ErrorMessage message) {}

    default void visit(TemporaryDefaultMessage message) {}
}