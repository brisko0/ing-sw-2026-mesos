package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.rmi.client.RmiClient;

public record TotemResetMessage(String nickname, int tileId) implements ServerMessage {
    @Override
    public void executeOnClient(RmiClient client) {
        System.out.println("il totem del player " + nickname + " è stato riportato sulla offer tile " + tileId);
    }
}
