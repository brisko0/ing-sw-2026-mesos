package it.polimi.ingsw.am56.network.message.update;

public interface MessageVisitor {
    default void visit(TemporaryDefaultMessage message) {}
    default void visit(PreLobbyStateMessage message) {}
    default void visit(LobbyStateMessage message) {}
    default void visit(GameSnapshotMessage message) {}
    default void visit(TotemsStateMessage message) {}
    default void visit(CurrentPlayerUpdateMessage message) {}
    default void visit(GameStateUpdateMessage message) {}
    default void visit(PlayerStatsUpdateMessage message) {}
    default void visit(EventResolvedMessage message) {}
    default void visit(EndRoundUpdateMessage message) {}
    default void visit(BoardUpdatedMessage message) {}
}
