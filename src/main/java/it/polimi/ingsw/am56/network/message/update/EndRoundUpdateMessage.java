package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.message.classesDTO.EndRoundSnapshotDTO;

public record EndRoundUpdateMessage(EndRoundSnapshotDTO snapshot) implements ServerMessage {
    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}