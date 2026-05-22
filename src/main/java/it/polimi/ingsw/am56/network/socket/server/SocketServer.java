package it.polimi.ingsw.am56.network.socket.server;

import it.polimi.ingsw.am56.controller.EndGameService;
import it.polimi.ingsw.am56.controller.GameController;
import it.polimi.ingsw.am56.controller.PlayerVirtualView;
import it.polimi.ingsw.am56.model.GameModel;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {
    private final ServerSocket serverSocket;
    private final GameController controller;
    private final GameModel model;
    private final EndGameService endGameService;

    public SocketServer(int port, GameModel model, GameController controller, EndGameService endGameService) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.model = model;
        this.controller = controller;
        this.endGameService = endGameService;
    }

    public void start() {
        System.out.println("[SOCKET SERVER] In ascolto sulla porta " + serverSocket.getLocalPort());

        while (true) {
            try {
                // Il server si blocca qui finché non si connette qualcuno
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SOCKET SERVER] Nuovo client connesso!");

                // 1. Creiamo il "Tubo di Rete" specifico per Socket
                SocketClientHandler socketConnection = new SocketClientHandler(clientSocket, controller);

                // 2. Avviamo il thread del tubo affinché inizi ad ascoltare i comandi in entrata
                new Thread(socketConnection).start();

                // 3. Creiamo il "Traduttore" per questo giocatore, collegandolo al Model e al Tubo Socket
                PlayerVirtualView virtualView = new PlayerVirtualView(this.model, socketConnection, endGameService);

                // 4. Registriamo il Traduttore come osservatore del Model
                model.addObserver(virtualView);

            } catch (IOException e) {
                System.err.println("Errore di connessione col client: " + e.getMessage());
            }
        }
    }
}