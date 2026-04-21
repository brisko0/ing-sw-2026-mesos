package it.polimi.ingsw.am56.network.socket.server;

import it.polimi.ingsw.am56.controller.GameController;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {
    private final ServerSocket serverSocket;
    private final GameController controller;

    public SocketServer(int port, GameController controller) throws IOException {
        this.serverSocket = new ServerSocket(port);
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

            } catch (IOException e) {
                System.err.println("Errore di connessione col client: " + e.getMessage());
            }
        }
    }
}