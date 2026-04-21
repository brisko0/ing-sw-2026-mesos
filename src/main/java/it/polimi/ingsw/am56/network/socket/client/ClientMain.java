package it.polimi.ingsw.am56.network.socket.client;

import it.polimi.ingsw.am56.network.socket.client.SocketClient;
import it.polimi.ingsw.am56.network.message.request.AddPlayerRequest;

public class ClientMain {
    public static void main(String[] args) throws Exception {

        SocketClient client = new SocketClient("127.0.0.1", 12345);

        System.out.println("Connesso! Invio una richiesta...");

        // Usiamo uno dei tuoi DTO!
        AddPlayerRequest request = new AddPlayerRequest("Alice", "RED");
        client.sendRequest(request);

        // Lasciamo il programma acceso per ascoltare eventuali risposte
        Thread.sleep(10000);
    }
}
