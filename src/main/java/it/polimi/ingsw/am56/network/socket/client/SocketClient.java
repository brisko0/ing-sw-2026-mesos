package it.polimi.ingsw.am56.network.socket.client;

import it.polimi.ingsw.am56.core.ServerAdapter;
import it.polimi.ingsw.am56.core.UpdateHandler;

import it.polimi.ingsw.am56.network.message.request.ClientRequest;
import it.polimi.ingsw.am56.network.message.update.ServerMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

// implementa ServerAdapter invece di NetworkClient
public class SocketClient implements ServerAdapter {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    private UpdateHandler updateHandler;

    public SocketClient(String host, int port) throws IOException {
        this.socket = new Socket(host, port);

        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());

        startListening();
    }

    // metodo per registrare la GUI
    @Override
    public void setUpdateHandler(UpdateHandler handler) {
        this.updateHandler = handler;
    }

    // deve accettare solo ClientRequests non object
    @Override
    public void sendRequest(ClientRequest request) {
        try {
            out.writeObject(request);
            out.reset();
            out.flush();
        } catch (IOException e) {
            System.err.println("Impossibile inviare la richiesta.");
        }
    }

    private void startListening() {
        new Thread(() -> {
            try {
                while (true) {
                    ServerMessage msg = (ServerMessage) in.readObject();
                    if (updateHandler != null) {
                        updateHandler.handleUpdate(msg);
                    } else {
                        System.out.println("[RETE] Messaggio ricevuto, ma nessuna View registrata: " + msg);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("[SOCKET] Connessione persa: " + e.getMessage());
                // Chiude il socket (e a cascata gli stream in/out) per non lasciarlo appeso.
                try {
                    socket.close();
                } catch (IOException ignored) {}
                if (updateHandler != null) {
                    updateHandler.handleDisconnect();
                }
            }
        }).start();
    }
}