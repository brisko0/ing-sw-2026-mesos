package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.NetworkClient;

public record StateChangedMessage(String stateName) implements ServerMessage {
    @Override
    public void executeOnClient(NetworkClient client) {
        System.out.println("Cambio fase, fase attuale: " + stateName);
    }
}
