package it.polimi.ingsw.am56.network.message.update;

public record GameAbortedMessage() implements ServerMessage {
    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}
