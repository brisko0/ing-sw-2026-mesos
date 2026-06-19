package it.polimi.ingsw.am56.network.message.classesDTO;

/**
 * Lightweight representation of a player used in the lobby, where only their identity matters and
 * no gameplay stats exist yet. For in-game state the richer {@link PlayerStatsDTO} is used instead.
 *
 * @param nickname the player's nickname
 * @param color    the player's chosen totem colour
 */
public record PlayerDTO(String nickname, String color) implements java.io.Serializable {}
