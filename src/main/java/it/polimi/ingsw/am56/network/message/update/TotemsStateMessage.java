package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.message.classesDTO.TotemPositionDTO;
import java.util.List;

/**
 * Sent when the totem positions change (a totem is placed or all totems are reset) to push the
 * current positions of every placed totem to the clients.
 *
 * @param totems the current positions of all placed totems
 */
public record TotemsStateMessage(List<TotemPositionDTO> totems) implements ServerMessage {
    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}