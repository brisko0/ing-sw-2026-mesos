package it.polimi.ingsw.am56.network.rmi.server;

import it.polimi.ingsw.am56.network.VirtualView;
import it.polimi.ingsw.am56.network.message.ServerMessage;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VirtualViewRmi extends Remote, VirtualView {
    @Override
    void showUpdate(ServerMessage serverMessage) throws RemoteException;

    @Override
    void reportError(String details) throws RemoteException;
}
