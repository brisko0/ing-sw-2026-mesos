package it.polimi.ingsw.am56.network.rmi.client;

import it.polimi.ingsw.am56.network.message.update.ServerMessage;
import it.polimi.ingsw.am56.network.message.request.ClientRequest;
import it.polimi.ingsw.am56.network.rmi.server.VirtualViewRmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RmiClient extends UnicastRemoteObject implements VirtualViewRmi {

    private VirtualServerRmi serverStub;

    public RmiClient() throws RemoteException {
        super();
    }

    public void setServerStub(VirtualServerRmi serverStub) {
        this.serverStub = serverStub;
    }

    // metodo chiamata dalla UI
    public void sendRequest(ClientRequest request) {
        try {
            if (serverStub != null) {
                // inviamo la richiesta passando this in modo da saper chi ha mandato la richiesta in caso di errore
                serverStub.sendRequest(request, this);
            }
        } catch (RemoteException e) {
            System.err.println("[CLIENT] Impossibile inviare la richiesta al server.");
        }
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