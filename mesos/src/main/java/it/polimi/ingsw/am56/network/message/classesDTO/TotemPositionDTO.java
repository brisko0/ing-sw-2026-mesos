package it.polimi.ingsw.am56.network.message.classesDTO;

import java.io.Serializable;

/**
 * Position of one player's totem on the board, identified by the totem's colour. The location is
 * encoded as a {@code (type, id)} pair so the same record can describe a totem sitting on either
 * kind of tile.
 *
 * @param totemColor   the colour of the totem, which identifies its owner
 * @param locationType the kind of tile the totem sits on: {@code "ORDER"} or {@code "OFFER"}
 * @param locationId   the tile identifier within that kind — the order-tile id (e.g. {@code "0"})
 *                     when {@code locationType} is {@code "ORDER"}, or the offer-tile letter
 *                     (e.g. {@code "A"}) when it is {@code "OFFER"}
 */
public record TotemPositionDTO(
        String totemColor,
        String locationType,
        String locationId
) implements Serializable {}