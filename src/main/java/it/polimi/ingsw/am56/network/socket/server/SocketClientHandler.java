package it.polimi.ingsw.am56.network.socket.server;

import it.polimi.ingsw.am56.controller.GameController;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;
import it.polimi.ingsw.am56.network.message.update.ServerMessage;
import it.polimi.ingsw.am56.network.message.request.ClientRequest;
import it.polimi.ingsw.am56.network.ClientConnection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketClientHandler implements Runnable, ClientConnection {
    private final Socket socket;
    private final GameController controller;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private boolean active = true;

    public SocketClientHandler(Socket socket, GameController controller) throws IOException {
        this.socket = socket;
        this.controller = controller;

        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        try {
            while (active) {
                ClientRequest request = (ClientRequest) in.readObject();

                try {
                    request.executeOnServer(controller);
                } catch (InvalidActionException e) {
                    this.sendError(e.getMessage());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[SOCKET SERVER] Connessione chiusa col client o classe non trovata.");
            disconnect();
        }
    }


    @Override
    public void sendMessage(ServerMessage message) {
        try {
            synchronized (out) {
                out.writeObject(message);
                out.reset();
                out.flush();
            }
        } catch (IOException e) {
            disconnect();
        }
    }

    @Override
    public void sendError(String errorDetails) {
        System.err.println("[SOCKET SERVER] Inviato errore al client: " + errorDetails);
    }

    @Override
    public void disconnect() {
        this.active = false;
        try {
            socket.close();
        } catch (IOException ignored) {}
    }
}