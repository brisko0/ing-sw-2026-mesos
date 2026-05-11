package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.message.classesDTO.CurrentPlayerDTO;

public record CurrentPlayerUpdateMessage(CurrentPlayerDTO currentPlayer) implements ServerMessage {
    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}