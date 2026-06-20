package it.polimi.ingsw.am56.core;

import it.polimi.ingsw.am56.network.message.update.ServerMessage;

/**
 * Client-side handler for everything the connection delivers from the server: game updates, errors and
 * connection-lifecycle events. A {@link ServerAdapter} pushes these callbacks to the registered
 * handler; the error/disconnect/reconnect methods are defaulted to no-ops so implementations only
 * override what they care about.
 */
public interface UpdateHandler {

    /**
     * Called when a server update arrives, to be applied to the client state/view.
     *
     * @param message the update received from the server
     */
    void handleUpdate(ServerMessage message);

    /**
     * Called when the server rejects an action and sends back an error to display.
     *
     * @param message the human-readable error description
     */
    default void handleError(String message) {}

    /**
     * Called when the connection to the server is lost.
     */
    default void handleDisconnect() {}

    /**
     * Called when the connection to the server has been re-established.
     */
    default void handleReconnected() {}
}