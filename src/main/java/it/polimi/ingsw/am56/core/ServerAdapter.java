package it.polimi.ingsw.am56.core;

import it.polimi.ingsw.am56.network.message.request.ClientRequest;

public interface ServerAdapter {

    void sendRequest(ClientRequest request);

    void setUpdateHandler(UpdateHandler handler);
}
