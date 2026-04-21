package it.polimi.ingsw.am56.network.message.update;
import it.polimi.ingsw.am56.network.NetworkClient;

public record TurnChangedMessage(String activePlayer) implements ServerMessage {
    @Override
    public void executeOnClient(NetworkClient client) {
        System.out.println("\n---> È il turno di: " + activePlayer + " <---");
    }
}