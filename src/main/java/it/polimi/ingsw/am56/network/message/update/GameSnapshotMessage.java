package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.message.classesDTO.GameSnapshotDTO;

/**
 * Sent when the game starts (and on resync) to deliver a full game snapshot, giving the clients
 * everything needed to build their in-game view from scratch.
 *
 * @param snapshot the full game snapshot
 */
public record GameSnapshotMessage(GameSnapshotDTO snapshot) implements ServerMessage {
    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}