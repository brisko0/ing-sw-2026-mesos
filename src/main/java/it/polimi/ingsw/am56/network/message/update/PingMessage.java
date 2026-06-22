package it.polimi.ingsw.am56.network.message.update;

/**
 * Application-level heartbeat pushed periodically by the server to a socket client. It carries no
 * data and triggers no UI reaction: its only purpose is to produce regular traffic on the socket so
 * that, when the connection goes half-open (the network drops without a FIN/RST), the client's read
 * times out and the server is considered unreachable — mirroring the RMI heartbeat.
 */
public record PingMessage() implements ServerMessage {
    /**
     * Dispatches to the visitor's (empty by default) {@code visit(PingMessage)} overload; no client
     * reacts to it.
     *
     * @param visitor the visitor
     */
    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}
