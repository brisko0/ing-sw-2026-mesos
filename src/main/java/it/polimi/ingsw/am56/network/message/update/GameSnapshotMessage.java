package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.message.classesDTO.GameSnapshotDTO;

public record GameSnapshotMessage(GameSnapshotDTO snapshot) implements ServerMessage {
    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}