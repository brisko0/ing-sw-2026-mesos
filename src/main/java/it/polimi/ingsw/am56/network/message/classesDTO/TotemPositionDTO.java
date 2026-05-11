package it.polimi.ingsw.am56.network.message.classesDTO;

import java.io.Serializable;

public record TotemPositionDTO(
        String totemColor,
        String locationType, // ORDER o OFFER
        String locationId    // numeri per ORDER, lettere per OFFER
) implements Serializable {}