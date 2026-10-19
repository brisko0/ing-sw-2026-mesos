package it.polimi.ingsw.am56.network.message.request;

import it.polimi.ingsw.am56.controller.GameController;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;

public record CreateGameRequest(String nickname, String totemColor, int gameSize) implements ClientRequest {
    @Override
    public void executeOnServer(GameController controller) throws InvalidActionException {
        controller.createGame(nickname, totemColor, gameSize);
    }
}
