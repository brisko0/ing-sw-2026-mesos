package it.polimi.ingsw.am56.network.message.update;

/**
 * Sent to the remaining clients when the game is aborted because a player disconnected and did
 * not reconnect in time. Carries no payload — it simply signals that the game cannot continue.
 */
public record GameAbortedMessage() implements ServerMessage {
    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}
