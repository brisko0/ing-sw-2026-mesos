package it.polimi.ingsw.am56.network.rmi.server;

import it.polimi.ingsw.am56.controller.GameController;
import it.polimi.ingsw.am56.controller.PlayerVirtualView;
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
    private final int port;

    // per rendere rmi asincrono, pool di thread che gestisce le richieste in background
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public RmiServer(int port, GameModel model, GameController controller) throws RemoteException {
        super();
        this.port = port;
        this.model = model;
        this.controller = controller;
    }

    @Override
    public void addClient(VirtualViewRmi clientStub) throws RemoteException {
        System.out.println("[SERVER] Nuovo client RMI connesso.");
        RmiClientHandler handler = new RmiClientHandler(clientStub);
        new Thread(handler).start();

        PlayerVirtualView virtualView = new PlayerVirtualView(this.model, handler);
        model.addObserver(virtualView);
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

    public void start() {
        try {
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("MesosServer", this);
            System.out.println("[RMI SERVER] RMI Server pronto sulla porta " + port);
        } catch (Exception e) {
            System.err.println("[RMI SERVER] Errore nell'avvio del server RMI: " + e.getMessage());
        }
    }
}