package it.polimi.ingsw.am56.network.message.update;

import java.io.Serializable;

/**
 * An update pushed from the server to a client. Each concrete message is a {@link Serializable}
 * record carrying one piece of game state or notification; it is shipped over the network (RMI or
 * socket) and dispatched on the client side through the Visitor pattern.
 * <p>
 * Rather than having clients switch on the message type, each message calls back into the
 * supplied {@link MessageVisitor} via {@link #accept}, which routes it to the visitor's
 * type-specific {@code visit} overload. This keeps the transport independent of how any
 * particular client (TUI, GUI, …) reacts to the update.
 */
public interface ServerMessage extends Serializable {
    /**
     * Dispatches this message to the visitor's matching {@code visit} overload (double dispatch).
     *
     * @param visitor the visitor that will handle this message
     */
    void accept(MessageVisitor visitor);
}