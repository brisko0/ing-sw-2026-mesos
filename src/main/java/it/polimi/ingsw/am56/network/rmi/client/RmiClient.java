package it.polimi.ingsw.am56.network.rmi.client;

import it.polimi.ingsw.am56.network.message.update.ServerMessage;
import it.polimi.ingsw.am56.network.rmi.server.VirtualViewRmi;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RmiClient extends UnicastRemoteObject implements VirtualViewRmi {

    public RmiClient() throws RemoteException {
        super();
    }

    @Override
    public void showUpdate(ServerMessage message) throws RemoteException {
        message.executeOnClient(this);
    }

    @Override
    public void reportError(String details) throws RemoteException {
        System.err.println("[CLIENT] Errore dal server: " + details);
    }
}