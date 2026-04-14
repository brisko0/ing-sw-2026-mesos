package it.polimi.ingsw.am56.network.message.update;
import it.polimi.ingsw.am56.network.rmi.client.RmiClient;

public record TurnChangedMessage(String activePlayer) implements ServerMessage {
    @Override
    public void executeOnClient(RmiClient client) {
        System.out.println("\n---> È il turno di: " + activePlayer + " <---");
    }
}