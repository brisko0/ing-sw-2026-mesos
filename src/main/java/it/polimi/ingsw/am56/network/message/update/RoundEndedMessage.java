package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.rmi.client.RmiClient;

public record RoundEndedMessage(int currentRound) implements ServerMessage {
    @Override
    public void executeOnClient(RmiClient client) {
        System.out.println("il round " + (currentRound - 1) + " è terminato. Inizio round: " + currentRound);
    }
}
