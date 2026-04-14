package it.polimi.ingsw.am56.network.message.update;
import it.polimi.ingsw.am56.network.rmi.client.RmiClient;

public record GameStartedMessage(int playersCount) implements ServerMessage {
    @Override
    public void executeOnClient(RmiClient client) {
        System.out.println("La partita è iniziata con " + playersCount + " giocatori!");
    }
}