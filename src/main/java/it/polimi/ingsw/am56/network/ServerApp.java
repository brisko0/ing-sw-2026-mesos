package it.polimi.ingsw.am56.network;

import it.polimi.ingsw.am56.controller.GameController;
import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.network.rmi.server.RmiServer;
import it.polimi.ingsw.am56.network.socket.server.SocketServer;

public class ServerApp {
    private static final int RMI_PORT = 1099;
    private static final int SOCKET_PORT = 12345;

    public static void main(String[] args) {
        System.out.println("avvio mesos server (uniti RMI + Socket)");

        try {
            GameModel sharedModel = new GameModel();
            GameController sharedController = new GameController(sharedModel);


            RmiServer rmiServer = new RmiServer(RMI_PORT, sharedModel, sharedController);
            rmiServer.start();


            SocketServer socketServer = new SocketServer(SOCKET_PORT, sharedModel, sharedController);
            socketServer.start();

        } catch (Exception e) {
            System.err.println("Errore critico durante l'avvio del server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}