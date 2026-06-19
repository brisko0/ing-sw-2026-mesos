package it.polimi.ingsw.am56.network.message.request;

import it.polimi.ingsw.am56.controller.GameController;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;

/**
 * Sent by a client to place/move its totem onto an offer tile. Routed to
 * {@link GameController#moveTotem(String, String)}, which resolves the player and the offer tile
 * identified by its letter and performs the totem placement on the model.
 *
 * @param nickname        the nickname of the player moving the totem
 * @param offerTileLetter the letter identifying the target offer tile on the board
 */
public record TotemMoveRequest(String nickname, String offerTileLetter) implements ClientRequest {
    @Override
    public void executeOnServer(GameController controller) throws InvalidActionException {
        // Il record passa se stesso (o i suoi parametri) al controller
        controller.moveTotem(nickname, offerTileLetter);
    }
}