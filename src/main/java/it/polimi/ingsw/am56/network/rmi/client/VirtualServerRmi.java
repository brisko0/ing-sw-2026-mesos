package it.polimi.ingsw.am56.network.rmi.client;

import it.polimi.ingsw.am56.network.message.request.ClientRequest;
import it.polimi.ingsw.am56.network.rmi.server.VirtualViewRmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

// interfaccia che definisce i metodi offerti dal server RMI
public interface VirtualServerRmi extends Remote {


    void addClient(VirtualViewRmi client) throws RemoteException;

    // unico punto di ingresso per tutte le azioni. request è il DTO contenente l'azione,
    // clientStub il riferimento al client che ha fatot la richiesta
    void sendRequest(ClientRequest request, VirtualViewRmi clientStub) throws RemoteException;
}