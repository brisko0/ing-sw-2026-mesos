package it.polimi.ingsw.am56.network.rmi.server;

import it.polimi.ingsw.am56.network.message.update.ServerMessage;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VirtualViewRmi extends Remote {
    void showUpdate(ServerMessage serverMessage) throws RemoteException;

    void reportError(String details) throws RemoteException;

    void ping() throws RemoteException;
}
