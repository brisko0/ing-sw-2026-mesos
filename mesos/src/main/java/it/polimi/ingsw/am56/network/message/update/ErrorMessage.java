package it.polimi.ingsw.am56.network.message.update;

/**
 * Sent by the server to a single client to report a rejected action
 * (e.g. a duplicate nickname or an illegal move). Carries a human-readable
 * description meant to be shown to the player.
 *
 * @param details the error message to display to the user
 */
public record ErrorMessage(String details) implements ServerMessage {
    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}
