package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.message.classesDTO.TotemPositionDTO;
import java.util.List;

public record TotemsStateMessage(List<TotemPositionDTO> totems) implements ServerMessage {
    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}