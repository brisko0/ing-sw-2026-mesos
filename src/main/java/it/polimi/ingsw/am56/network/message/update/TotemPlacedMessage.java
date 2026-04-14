package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.rmi.client.RmiClient;

public record TotemPlacedMessage(String nickname, String tileLetter) implements ServerMessage {
    @Override
    public void executeOnClient(RmiClient client) {
        System.out.println("il player " + nickname + " ha spostato il suo totem sulla offer tile " + tileLetter);
    }
}
