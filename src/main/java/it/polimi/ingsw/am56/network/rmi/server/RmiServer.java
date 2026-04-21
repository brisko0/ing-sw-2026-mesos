package it.polimi.ingsw.am56.network.rmi.server;

import it.polimi.ingsw.am56.controller.GameController;
import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;
import it.polimi.ingsw.am56.network.message.request.ClientRequest;
import it.polimi.ingsw.am56.network.rmi.client.VirtualServerRmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RmiServer extends UnicastRemoteObject implements VirtualServerRmi {
    private final GameModel model;
    private final GameController controller;

    // per rendere rmi asincrono, pool di thread che gestisce le richieste in background
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public RmiServer(GameModel model, GameController controller) throws RemoteException {
        super();
        this.model = model;
        this.controller = controller;
    }

    @Override
    public void addClient(VirtualViewRmi clientStub) throws RemoteException {
        System.out.println("[SERVER] Nuovo client RMI connesso.");
        RmiClientHandler handler = new RmiClientHandler(clientStub, this.model);
        model.addObserver(handler);
        new Thread(handler).start();
    }

    // nuovo metodo universale
    @Override
    public void sendRequest(ClientRequest request, VirtualViewRmi clientStub) throws RemoteException {

        // server mette la richiesta in coda e il client viene sbloccato immediatamente
        // per evitare di bloccare il client chiamante utilizzo un ExecutorService
        executor.submit(() -> {
            try {
                // polimorfismo nel controller
                request.executeOnServer(controller);

            } catch (InvalidActionException e) {
                // nel caso in cui la mossa genera un eccezione la mandiamo solo al client che l'ha generata
                try {
                    clientStub.reportError(e.getMessage());
                } catch (RemoteException re) {
                    System.err.println("[SERVER] Impossibile notificare l'errore al client.");
                }
            }
        });
    }

    public static void main(String[] args) {
        try {
            GameModel model = new GameModel();
            GameController controller = new GameController(model);
            RmiServer server = new RmiServer(model, controller);

            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("MesosServer", server);

            System.out.println("RMI Server pronto su porta 1099...");
            System.out.println("Architettura Asincrona e Command Pattern ATTIVI.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}