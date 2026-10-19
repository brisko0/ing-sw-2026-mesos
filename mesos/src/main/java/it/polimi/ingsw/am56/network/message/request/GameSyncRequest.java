package it.polimi.ingsw.am56.network.message.request;

import it.polimi.ingsw.am56.controller.GameController;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;

/**
 * Sent by a client to ask the server to push the current game state. Routed to
 * {@link GameController#sendGameState()}, which re-syncs the in-game state to the clients.
 * Carries no payload — it is purely a request to receive a fresh snapshot.
 */
public record GameSyncRequest() implements ClientRequest {
    @Override
    public void executeOnServer(GameController controller) throws InvalidActionException {
        controller.sendGameState();
    }
}