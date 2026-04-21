package it.polimi.ingsw.am56.network.rmi.client;

// Assicurati di avere tutti questi import corretti in base ai tuoi package
import it.polimi.ingsw.am56.network.message.request.AddPlayerRequest;
import it.polimi.ingsw.am56.network.message.request.CardPickRequest;
import it.polimi.ingsw.am56.network.message.request.CreateGameRequest;
import it.polimi.ingsw.am56.network.message.request.TotemMoveRequest;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientMain {
    public static void main(String[] args) {
        try {
            // cerco il registro sulla porta 1099
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);

            //  ottengo riferimento al server (stub)
            VirtualServerRmi serverStub = (VirtualServerRmi) registry.lookup("MesosServer");

            // creo la view
            RmiClient view = new RmiClient();

            // dico alla view chi è il server
            view.setServerStub(serverStub);

            // registrazione sul server
            System.out.println("[CLIENT] Connessione in corso...");
            serverStub.addClient(view);
            System.out.println("[CLIENT] Registrato correttamente.");

            // test attraverso DTO request (test hardcodato)

            // createGame + addPlayer

            view.sendRequest(new CreateGameRequest("Alice", "RED", 2));
            Thread.sleep(200);

            view.sendRequest(new AddPlayerRequest("Bob", "BLUE"));
            Thread.sleep(200);

            // totem placement

            view.sendRequest(new TotemMoveRequest("Alice", "C"));
            Thread.sleep(200);

            view.sendRequest(new TotemMoveRequest("Bob", "F"));
            Thread.sleep(200);

            // pesca carte
            view.sendRequest(new CardPickRequest("Alice", 99));
            Thread.sleep(200);

            view.sendRequest(new CardPickRequest("Bob", 100));
            Thread.sleep(200);

            view.sendRequest(new CardPickRequest("Bob", 101));
            Thread.sleep(200);

        } catch (Exception e) {
            System.err.println("[CLIENT] Errore durante l'esecuzione: " + e.getMessage());
            e.printStackTrace();
        }
    }
}