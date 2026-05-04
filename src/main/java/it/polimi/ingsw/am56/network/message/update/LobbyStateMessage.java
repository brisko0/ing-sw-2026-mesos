package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.message.classesDTO.PlayerDTO;

public record LobbyStateMessage(java.util.List<PlayerDTO> players, int gameSize) implements ServerMessage {
    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}