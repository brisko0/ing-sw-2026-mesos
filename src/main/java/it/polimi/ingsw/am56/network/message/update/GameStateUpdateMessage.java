package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.message.classesDTO.GameStateDTO;

public record GameStateUpdateMessage(GameStateDTO gameState) implements ServerMessage {
    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}