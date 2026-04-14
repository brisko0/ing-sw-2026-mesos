package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.rmi.client.RmiClient;

public record CardPickedMessage(String nickname, String phase) implements ServerMessage {
    @Override
    public void executeOnClient(RmiClient client) {
        System.out.println("Il giocatore " + nickname + " ha pescato una carta.");
    }
}