package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.message.classesDTO.EndRoundSnapshotDTO;

/**
 * Sent when a round ends, carrying the end-of-round snapshot (board after the rows have
 * advanced, players' stats and the player who opens the new round).
 *
 * @param snapshot the end-of-round snapshot
 */
public record EndRoundUpdateMessage(EndRoundSnapshotDTO snapshot) implements ServerMessage {
    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}