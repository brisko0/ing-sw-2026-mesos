package it.polimi.ingsw.am56.network.socket.server;

import it.polimi.ingsw.am56.controller.GameController;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;
import it.polimi.ingsw.am56.network.message.update.ServerMessage;
import it.polimi.ingsw.am56.network.message.request.ClientRequest;
import it.polimi.ingsw.am56.network.VirtualView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketClientHandler implements Runnable, VirtualView {
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

                // polimorfismo delle request
                request.executeOnServer(controller);
            }
        } catch (InvalidActionException e) {
            // nel caso in cui il controller lancia l'eccezione, spediamo l'errore solo al client che l'ha generato
            this.reportError(e.getMessage());

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[SERVER] Connessione chiusa col client o classe non trovata.");
            active = false;
        }
    }

    @Override
    public void showUpdate(ServerMessage message) {
        try {
            out.writeObject(message);
            out.reset();
            out.flush();
        } catch (IOException e) {
            active = false;
        }
    }

    @Override
    public void reportError(String details) {
        System.err.println("Inviato errore al client: " + details);
    }
}