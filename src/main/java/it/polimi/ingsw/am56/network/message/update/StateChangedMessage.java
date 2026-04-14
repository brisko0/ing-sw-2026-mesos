package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.rmi.client.RmiClient;

public record StateChangedMessage(String stateName) implements ServerMessage {
    @Override
    public void executeOnClient(RmiClient client) {
        System.out.println("Cambio fase, fase attuale: " + stateName);
    }
}
