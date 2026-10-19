package it.polimi.ingsw.am56.network;

import it.polimi.ingsw.am56.network.message.update.ServerMessage;

/**
 * Server-side view of a single connected client, regardless of the underlying transport (RMI or
 * socket). It is the channel through which the server pushes game updates and errors to one client and
 * tears the connection down. A {@code PlayerVirtualView} talks to its client only through this
 * interface, so it stays independent of the transport implementation.
 */
public interface ClientConnection {

    /**
     * Pushes a server update to the client.
     *
     * @param message the update to deliver
     */
    void sendMessage(ServerMessage message);

    /**
     * Sends an error notification to the client.
     *
     * @param errorDetails a human-readable description of the error
     */
    void sendError(String errorDetails);

    /**
     * Closes the connection to the client.
     */
    void disconnect();
}