package it.polimi.ingsw.am56.network.message.request;

import it.polimi.ingsw.am56.controller.GameController;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;

import java.io.Serializable;

/**
 * A command sent from a client to the server. Each concrete request is a {@link Serializable}
 * record carrying the data of one client action; it is shipped over the network (RMI or socket)
 * and, once received, applies itself to the {@link GameController} via {@link #executeOnServer}.
 * <p>
 * This follows the Command pattern: the server does not switch on a request type, it simply
 * calls {@link #executeOnServer} and lets each request route itself to the right controller
 * method.
 */
public interface ClientRequest extends Serializable {
    /**
     * Applies this request to the given controller, invoking the controller method that
     * corresponds to the client action this request represents.
     *
     * @param controller the server-side controller to act upon
     * @throws InvalidActionException if the action is not allowed in the current game state
     */
    void executeOnServer(GameController controller) throws InvalidActionException;
}