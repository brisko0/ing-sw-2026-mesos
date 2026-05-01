package it.polimi.ingsw.am56.core;

import it.polimi.ingsw.am56.network.message.update.ServerMessage;

public interface UpdateHandler {
    void handleUpdate(ServerMessage message);
}