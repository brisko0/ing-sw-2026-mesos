package it.polimi.ingsw.am56.network.rmi.client;

import it.polimi.ingsw.am56.network.message.request.ClientRequest;
import it.polimi.ingsw.am56.network.rmi.server.VirtualViewRmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VirtualServerRmi extends Remote {

    void addClient(VirtualViewRmi client) throws RemoteException;

    void sendRequest(ClientRequest request, VirtualViewRmi clientStub) throws RemoteException;

    void ping() throws RemoteException;
}