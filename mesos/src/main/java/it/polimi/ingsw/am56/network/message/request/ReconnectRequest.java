package it.polimi.ingsw.am56.network.message.request;

import it.polimi.ingsw.am56.controller.GameController;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;

/**
 * Sent by a client to rejoin a game in progress after a disconnection. Routed to
 * {@link GameController#reconnectPlayer(String)}, which verifies the nickname belongs to the
 * ongoing game, re-syncs the game state to that player, and marks the reconnection within the
 * reconnect window (cancelling the disconnect timeout once everyone expected is back).
 *
 * @param nickname the nickname the player originally used in the game being rejoined
 */
public record ReconnectRequest(String nickname) implements ClientRequest {

    @Override
    public void executeOnServer(GameController controller) throws InvalidActionException {
        controller.reconnectPlayer(nickname);
    }
}