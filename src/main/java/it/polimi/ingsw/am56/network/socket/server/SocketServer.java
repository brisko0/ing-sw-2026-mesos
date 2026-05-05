package it.polimi.ingsw.am56.network.socket.server;

import it.polimi.ingsw.am56.controller.GameController;
import it.polimi.ingsw.am56.controller.PlayerVirtualView;
import it.polimi.ingsw.am56.model.GameModel;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {
    private final ServerSocket serverSocket;
    private final GameModel model;
    private final GameController controller;

    public SocketServer(int port, GameModel model, GameController controller) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.model = model;
        this.controller = controller;
    }

    public void start() {
        System.out.println("[SOCKET SERVER] In ascolto sulla porta " + serverSocket.getLocalPort());

        while (true) {
            try {
                // server in ascolto
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SOCKET SERVER] Nuovo client connesso!");

                // creo l'handler per il client specifico
                SocketClientHandler handler = new SocketClientHandler(clientSocket, controller);

                // creo e avvio un thread dedicato al giocatore
                new Thread(handler).start();

                PlayerVirtualView virtualView = new PlayerVirtualView(this.model, handler);

                model.addObserver(virtualView);

            } catch (IOException e) {
                System.err.println("Errore di connessione col client: " + e.getMessage());
            }
        }
    }
}