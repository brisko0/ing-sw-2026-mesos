package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.rmi.client.RmiClient;

public record PlayerAddedMessage() implements ServerMessage {

    @Override
    public void executeOnClient(RmiClient client) {
        System.out.println("un nuovo player si è aggiunto alla partita");
    }

}
