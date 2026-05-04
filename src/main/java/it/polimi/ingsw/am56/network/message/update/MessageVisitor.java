package it.polimi.ingsw.am56.network.message.update;

public interface MessageVisitor {
    // Un metodo per ogni DTO specifico
    default void visit(TemporaryDefaultMessage message) {}
    default void visit(PreLobbyStateMessage message) {}
    default void visit(LobbyStateMessage message) {}
}
