package it.polimi.ingsw.am56.network.rmi.server;

import it.polimi.ingsw.am56.controller.EndGameService;
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
    private final EndGameService endGameService;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    public RmiServer(int port, GameModel model, GameController controller, EndGameService endGameService) throws RemoteException {
        super();
        this.port = port;
        this.model = model;
        this.controller = controller;
        this.endGameService = endGameService;
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

    @Override
    public void addClient(VirtualViewRmi clientStub) throws RemoteException {
        RmiClientHandler rmiConnection = new RmiClientHandler(clientStub, controller);
        new Thread(rmiConnection).start();
        PlayerVirtualView virtualView =
                new PlayerVirtualView(model, rmiConnection, endGameService);
        model.addObserver(virtualView);
    }

    @Override
    public void sendRequest(ClientRequest request, VirtualViewRmi clientStub) throws RemoteException {
        executor.submit(() -> {
            try {
                request.executeOnServer(controller);
            } catch (InvalidActionException e) {
                try {
                    clientStub.reportError(e.getMessage());
                } catch (RemoteException re) {
                    System.err.println("[SERVER] Impossibile notificare l'errore al client RMI.");
                }
            }
        });
    }

    @Override
    public void ping() throws RemoteException {
        // no-op: solo per verificare che il server sia vivo
    }
}