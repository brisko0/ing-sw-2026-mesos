package it.polimi.ingsw.am56.core;

import it.polimi.ingsw.am56.network.message.request.ClientRequest;

/**
 * Client-side abstraction of the connection to the server, independent of the transport (RMI or
 * socket). The client sends requests through it and registers the {@link UpdateHandler} that should
 * receive the updates, errors and connection events coming back from the server.
 */
public interface ServerAdapter {
    /**
     * Sends a request to the server.
     *
     * @param request the request to send
     */
    void sendRequest(ClientRequest request);

    /**
     * Registers the handler that receives the server's updates, errors and connection events.
     *
     * @param handler the update handler
     */
    void setUpdateHandler(UpdateHandler handler);
}