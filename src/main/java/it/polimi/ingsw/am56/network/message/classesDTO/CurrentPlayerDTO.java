package it.polimi.ingsw.am56.network.message.classesDTO;

import java.io.Serializable;

/**
 * Identifies the player whose turn it currently is, so the clients can highlight the active
 * player. Carries only the data needed to recognise them.
 *
 * @param nickname the current player's nickname
 * @param color    the current player's totem colour
 */
public record CurrentPlayerDTO(String nickname, String color) implements Serializable {}