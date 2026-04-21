package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.NetworkClient;

public record RoundEndedMessage(int currentRound) implements ServerMessage {
    @Override
    public void executeOnClient(NetworkClient client) {
        System.out.println("il round " + (currentRound - 1) + " è terminato. Inizio round: " + currentRound);
    }
}
