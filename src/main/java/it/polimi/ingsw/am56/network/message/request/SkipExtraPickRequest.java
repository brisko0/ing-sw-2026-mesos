package it.polimi.ingsw.am56.network.message.request;

import it.polimi.ingsw.am56.controller.GameController;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;

/**
 * Sent by a client to decline the optional extra upper-row pick granted by the relevant
 * building card. Routed to {@link GameController#skipExtraPick(String)}, which advances the
 * game straight to the end-of-round phase without drawing a card.
 */
public record SkipExtraPickRequest(String nickname) implements ClientRequest {
    @Override
    public void executeOnServer(GameController controller) throws InvalidActionException {
        controller.skipExtraPick(nickname);
    }
}
