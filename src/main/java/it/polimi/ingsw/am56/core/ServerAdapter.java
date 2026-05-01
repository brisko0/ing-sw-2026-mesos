package it.polimi.ingsw.am56.core;

import it.polimi.ingsw.am56.network.message.request.ClientRequest;

public interface ServerAdapter {

    // per inviare da client a server
    void sendRequest(ClientRequest request);

    // metodo per registrarsi e ricevere gli aggiornamenti dal server
    void setUpdateHandler(UpdateHandler handler);
}
