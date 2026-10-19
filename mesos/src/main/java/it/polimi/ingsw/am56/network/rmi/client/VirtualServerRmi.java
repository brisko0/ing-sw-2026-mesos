package it.polimi.ingsw.am56.network.rmi.client;

import it.polimi.ingsw.am56.network.message.request.ClientRequest;
import it.polimi.ingsw.am56.network.rmi.server.VirtualViewRmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface exposed by the RMI server to its clients. A client looks this stub up in the RMI
 * registry to register itself, send requests and check that the server is alive. The client identifies
 * itself by passing its own {@link VirtualViewRmi} stub, which the server uses to push updates back.
 */
public interface VirtualServerRmi extends Remote {

    /**
     * Registers a client on the server, providing the client's callback stub so the server can later
     * push updates and errors to it.
     *
     * @param client the client's remote view stub
     * @throws RemoteException if the remote call fails
     */
    void addClient(VirtualViewRmi client) throws RemoteException;

    /**
     * Sends a client request to the server for execution; the client's stub is passed so the server
     * can report back any error from processing it.
     *
     * @param request    the request to execute on the server
     * @param clientStub the calling client's remote view stub
     * @throws RemoteException if the remote call fails
     */
    void sendRequest(ClientRequest request, VirtualViewRmi clientStub) throws RemoteException;

    /**
     * No-op liveness check used by the client's heartbeat to detect a dead server.
     *
     * @throws RemoteException if the server is unreachable
     */
    void ping() throws RemoteException;
}