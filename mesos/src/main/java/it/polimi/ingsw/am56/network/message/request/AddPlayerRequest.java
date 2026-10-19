package it.polimi.ingsw.am56.network.message.request;

import it.polimi.ingsw.am56.controller.GameController;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;

/**
 * Sent by a client to join an already-created game. Routed to
 * {@link GameController#addPlayer(String, String)}, which registers a new player with the chosen
 * nickname and totem colour.
 *
 * @param nickname the nickname the joining player wants to use
 * @param color    the totem colour chosen by the player
 */
public record AddPlayerRequest(String nickname, String color) implements ClientRequest {
    @Override
    public void executeOnServer(GameController controller) throws InvalidActionException {
        // Il record sa che deve chiamare addPlayer!
        controller.addPlayer(nickname, color);
    }
}