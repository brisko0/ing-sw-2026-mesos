package it.polimi.ingsw.am56.network.message.request;

import it.polimi.ingsw.am56.controller.GameController;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;

/**
 * Sent by a client to pick a card from the board. Routed to
 * {@link GameController#cardPick(String, int)}, which resolves the player and the board card
 * identified by {@code cardId} and performs the pick on the model.
 *
 * @param nickname the nickname of the player picking the card
 * @param cardId   the identifier of the card to pick from the board
 */
public record CardPickRequest(String nickname, int cardId) implements ClientRequest {
    @Override
    public void executeOnServer(GameController controller) throws InvalidActionException {
        controller.cardPick(nickname, cardId);
    }
}