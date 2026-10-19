package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.message.classesDTO.CurrentPlayerDTO;

/**
 * Sent when the turn passes to a different player, so the clients can update which player is
 * shown as active.
 *
 * @param currentPlayer the player whose turn it now is
 */
public record CurrentPlayerUpdateMessage(CurrentPlayerDTO currentPlayer) implements ServerMessage {
    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}