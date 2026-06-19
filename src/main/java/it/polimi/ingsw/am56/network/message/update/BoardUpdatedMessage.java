package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.message.classesDTO.BoardDTO;

/**
 * Sent when the shared board changes (e.g. after a card is picked) to push the refreshed board
 * state to the clients.
 *
 * @param board the updated board snapshot
 */
public record BoardUpdatedMessage(BoardDTO board) implements ServerMessage {
    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}