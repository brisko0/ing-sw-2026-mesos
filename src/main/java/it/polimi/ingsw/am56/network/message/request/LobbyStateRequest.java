package it.polimi.ingsw.am56.network.message.request;

import it.polimi.ingsw.am56.controller.GameController;

/**
 * Sent by a client to ask the server for the current lobby state. Routed to
 * {@link GameController#checkLobbyState()}, which re-syncs the lobby (players joined, slots
 * remaining, etc.) to the clients. Carries no payload.
 */
public record LobbyStateRequest() implements ClientRequest {
    @Override
    public void executeOnServer(GameController controller) {
        // Chiama il nuovo metodo che abbiamo appena creato!
        controller.checkLobbyState();
    }
}