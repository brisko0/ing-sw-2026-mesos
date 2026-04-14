package it.polimi.ingsw.am56.network.rmi.server;

import it.polimi.ingsw.am56.controller.GameController;
import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;
import it.polimi.ingsw.am56.network.message.request.*;
import it.polimi.ingsw.am56.network.rmi.client.VirtualServerRmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RmiServer extends UnicastRemoteObject implements VirtualServerRmi {
    private final GameModel model;
    private final GameController controller;

    public RmiServer(GameModel model, GameController controller) throws RemoteException {
        super();
        this.model = model;
        this.controller = controller;
    }

    @Override
    public void addClient(VirtualViewRmi clientStub) throws RemoteException {
        System.out.println("[SERVER] Nuovo client RMI connesso.");

        // creo l'handler asincrono per questo specifico client
        RmiClientHandler handler = new RmiClientHandler(clientStub, this.model);

        // lo si registra come osservatore del modello
        model.addObserver(handler);

        // si fa partire il thread dedicato alla comunicazione con questo client
        new Thread(handler).start();
    }

    @Override
    public void addPlayer(String nickname, String totemColor) throws InvalidActionException, RemoteException {
        controller.addPlayer(nickname, totemColor);
    }

    @Override
    public void startGame() throws InvalidActionException, RemoteException {
        controller.startGame();
    }

    @Override
    public void moveTotem(TotemMoveRequest totemRequest) throws InvalidActionException, RemoteException {
        System.out.println("[SERVER] Richiesta moveTotem ricevuta da: " + totemRequest.nickname());
        controller.moveTotem(totemRequest);
    }

    @Override
    public void cardPick(CardPickRequest pickRequest) throws InvalidActionException, RemoteException {
        System.out.println("[SERVER] Richiesta cardPick ricevuta da: " + pickRequest.nickname());
        controller.cardPick(pickRequest);
    }

    public static void main(String[] args) {
        try {
            GameModel model = new GameModel();
            GameController controller = new GameController(model);

            RmiServer server = new RmiServer(model, controller);

            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("MesosServer", server);

            System.out.println("RMI Server pronto su porta 1099...");
            System.out.println("In attesa di connessioni e comandi addPlayer/startGame...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}