package it.polimi.ingsw.am56.network;

import java.rmi.RemoteException;

public interface VirtualView {

    void showUpdate(ServerMessage serverMessage) throws RemoteException;

    void reportError(String details) throws RemoteException;

}
