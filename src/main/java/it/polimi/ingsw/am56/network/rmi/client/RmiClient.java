package it.polimi.ingsw.am56.network.rmi.client;

// Importa dal package core (o dove hai messo l'interfaccia)
import it.polimi.ingsw.am56.core.ServerAdapter;
import it.polimi.ingsw.am56.core.UpdateHandler;

import it.polimi.ingsw.am56.network.message.update.ServerMessage;
import it.polimi.ingsw.am56.network.message.request.ClientRequest;
import it.polimi.ingsw.am56.network.rmi.server.VirtualViewRmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

// implementa anche ServerAdapter
public class RmiClient extends UnicastRemoteObject implements VirtualViewRmi, ServerAdapter {

    private VirtualServerRmi serverStub;

    private UpdateHandler updateHandler;

    public RmiClient() throws RemoteException {
        super();
    }

    public void setServerStub(VirtualServerRmi serverStub) {
        this.serverStub = serverStub;
    }

    // la GUI potrÃ  registrarsi per ascoltare
    @Override
    public void setUpdateHandler(UpdateHandler handler) {
        this.updateHandler = handler;
    }

    @Override
    public void sendRequest(ClientRequest request) {
        try {
            if (serverStub != null) {
                serverStub.sendRequest(request, this);
            }
        } catch (RemoteException e) {
            System.err.println("[CLIENT] Impossibile inviare la richiesta al server.");
        }
    }

    @Override
    public void showUpdate(ServerMessage message) throws RemoteException {
        if (updateHandler != null) {
            updateHandler.handleUpdate(message);
        } else {
            System.out.println("[RMI CLIENT] Messaggio ricevuto ma nessuna View collegata: " + message);
        }
    }

    @Override
    public void reportError(String details) throws RemoteException {
        System.err.println("[CLIENT] Errore dal server: " + details);
    }
}