package it.polimi.ingsw.am56.network.message.classesDTO;

import java.io.Serializable;

/**
 * Per-player result of a single resolved Event card: how much Food and how many Prestige
 * Points the player gained (positive) or lost (negative) from that one event. Used by the
 * clients to show a readable line such as {@code "mario: +1F +2P"} in the events log.
 */
public record EventOutcomeDTO(
        String nickname,
        String color,
        int foodDelta,
        int pointsDelta
) implements Serializable {}
