package it.polimi.ingsw.am56.network.rmi.server;

import it.polimi.ingsw.am56.network.message.update.ServerMessage;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote callback interface exposed by an RMI client to the server. The server holds one such stub per
 * connected client and uses it to push updates and errors back, and to check that the client is alive.
 */
public interface VirtualViewRmi extends Remote {
    /**
     * Delivers a server update to the client.
     *
     * @param serverMessage the update to show on the client
     * @throws RemoteException if the remote call fails
     */
    void showUpdate(ServerMessage serverMessage) throws RemoteException;

    /**
     * Delivers an error notification to the client.
     *
     * @param details a human-readable description of the error
     * @throws RemoteException if the remote call fails
     */
    void reportError(String details) throws RemoteException;

    /**
     * No-op liveness check used by the server's heartbeat to detect a dead client.
     *
     * @throws RemoteException if the client is unreachable
     */
    void ping() throws RemoteException;
}
