package it.polimi.ingsw.am56.network.message.classesDTO;

import it.polimi.ingsw.am56.model.state.State;

import java.io.Serializable;

/**
 * Carries the current game phase to the clients as a {@link State} enum value, so the views can
 * compare it for equality instead of matching on the (unstable) state-class name.
 *
 * @param gameState the current phase of the game
 */
public record GameStateDTO(State gameState) implements Serializable {}
