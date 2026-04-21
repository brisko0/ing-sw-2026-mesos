package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.NetworkClient;

public record GameStateMessage() implements ServerMessage {

    @Override
    public void executeOnClient(NetworkClient client) {
        System.out.printf("Messaggio default");
    }
}