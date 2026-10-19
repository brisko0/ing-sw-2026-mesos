package it.polimi.ingsw.am56.network.message.request;

import it.polimi.ingsw.am56.controller.GameController;

/**
 * Application-level heartbeat sent periodically by a socket client to the server. It carries no data
 * and does nothing on the server: its only purpose is to produce regular traffic on the socket so
 * that, when the connection goes half-open (the network drops without a FIN/RST), the server's read
 * times out and the client is considered disconnected — mirroring the RMI heartbeat.
 */
public record PingRequest() implements ClientRequest {
    /**
     * No-op: the request exists only to keep the socket's read alive / detect a half-open connection.
     *
     * @param controller the server-side controller (unused)
     */
    @Override
    public void executeOnServer(GameController controller) {
    }
}
