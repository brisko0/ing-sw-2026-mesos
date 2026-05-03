package it.polimi.ingsw.am56.network.message.request;
import it.polimi.ingsw.am56.controller.GameController;

public record LobbyStateRequest() implements ClientRequest {
    @Override
    public void executeOnServer(GameController controller) {
        controller.checkLobbyState();
    }
}