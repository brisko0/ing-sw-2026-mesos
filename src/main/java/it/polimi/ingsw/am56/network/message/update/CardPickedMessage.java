package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.NetworkClient;

public record CardPickedMessage(String nickname, String phase) implements ServerMessage {
    @Override
    public void executeOnClient(NetworkClient client) {
        System.out.println("Il giocatore " + nickname + " ha pescato una carta.");
    }
}