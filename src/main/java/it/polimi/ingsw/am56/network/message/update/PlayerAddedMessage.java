package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.NetworkClient;

public record PlayerAddedMessage() implements ServerMessage {

    @Override
    public void executeOnClient(NetworkClient client) {
        System.out.println("un nuovo player si è aggiunto alla partita");
    }

}
