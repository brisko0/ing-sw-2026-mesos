package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.message.classesDTO.PlayerStatsDTO;

/**
 * Sent when a single player's in-game state changes (e.g. after picking a card, on turn change
 * or on a totem reset) to push that player's refreshed stats to the clients.
 *
 * @param stats the updated stats of the affected player
 */
public record PlayerStatsUpdateMessage(PlayerStatsDTO stats) implements ServerMessage {
    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}