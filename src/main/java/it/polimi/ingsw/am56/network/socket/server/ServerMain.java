package it.polimi.ingsw.am56.network.socket.server;

import it.polimi.ingsw.am56.controller.GameController;
import it.polimi.ingsw.am56.model.GameModel;

public class ServerMain {
    public static void main(String[] args) throws Exception {

        GameModel model = new GameModel();

        GameController controller = new GameController(model);

        SocketServer server = new SocketServer(12345, controller);
        server.start();
    }
}