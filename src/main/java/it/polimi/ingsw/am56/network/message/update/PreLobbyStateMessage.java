package it.polimi.ingsw.am56.network.message.update;

public record PreLobbyStateMessage(boolean isGameAlreadyCreated) implements ServerMessage {

    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}
