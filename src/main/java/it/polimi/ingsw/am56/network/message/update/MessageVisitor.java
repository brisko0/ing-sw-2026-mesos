package it.polimi.ingsw.am56.network.message.update;

public interface MessageVisitor {
    default void visit(TemporaryDefaultMessage message) {}
    default void visit(PreLobbyStateMessage message) {}
    default void visit(LobbyStateMessage message) {}
    default void visit(GameSnapshotMessage message) {}
}
