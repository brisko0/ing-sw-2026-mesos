package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.message.classesDTO.GameStateDTO;

/**
 * Sent when the game advances to a new phase, so the clients can react to the current
 * {@link it.polimi.ingsw.am56.model.state.State} (e.g. enabling/disabling actions).
 *
 * @param gameState the new game phase
 */
public record GameStateUpdateMessage(GameStateDTO gameState) implements ServerMessage {

    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}