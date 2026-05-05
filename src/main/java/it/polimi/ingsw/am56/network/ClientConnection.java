package it.polimi.ingsw.am56.network;

import it.polimi.ingsw.am56.network.message.update.ServerMessage;

public interface ClientConnection {

    void sendMessage(ServerMessage message);

    void sendError(String errorDetails);

    void disconnect();
}