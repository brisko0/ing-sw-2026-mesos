package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.message.classesDTO.PlayerStatsDTO;

public record PlayerStatsUpdateMessage(PlayerStatsDTO stats) implements ServerMessage {
    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}