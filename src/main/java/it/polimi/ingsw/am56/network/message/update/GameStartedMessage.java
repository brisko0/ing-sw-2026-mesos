package it.polimi.ingsw.am56.network.message.update;
import it.polimi.ingsw.am56.network.NetworkClient;

public record GameStartedMessage(int playersCount) implements ServerMessage {
    @Override
    public void executeOnClient(NetworkClient client) {
        System.out.println("La partita è iniziata con " + playersCount + " giocatori!");
    }
}