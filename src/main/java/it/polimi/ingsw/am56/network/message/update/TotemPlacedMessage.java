package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.NetworkClient;

public record TotemPlacedMessage(String nickname, String tileLetter) implements ServerMessage {
    @Override
    public void executeOnClient(NetworkClient client) {
        System.out.println("il player " + nickname + " ha spostato il suo totem sulla offer tile " + tileLetter);
    }
}
