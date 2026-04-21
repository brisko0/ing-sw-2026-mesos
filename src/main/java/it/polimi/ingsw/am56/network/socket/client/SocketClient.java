package it.polimi.ingsw.am56.network.socket.client;

import it.polimi.ingsw.am56.network.NetworkClient;
import it.polimi.ingsw.am56.network.message.update.ServerMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketClient implements NetworkClient {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    public SocketClient(String host, int port) throws IOException {
        this.socket = new Socket(host, port);

        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());

        startListening();
    }

    private void startListening() {
        new Thread(() -> {
            try {
                while (true) {
                    ServerMessage msg = (ServerMessage) in.readObject();

                    msg.executeOnClient(this);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Disconnesso dal server.");
            }
        }).start();
    }

    public void sendRequest(Object request) {
        try {
            out.writeObject(request);
            out.reset();
            out.flush();
        } catch (IOException e) {
            System.err.println("Impossibile inviare la richiesta.");
        }
    }
}