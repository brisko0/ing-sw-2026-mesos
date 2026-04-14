package it.polimi.ingsw.am56.network.rmi.client;

import it.polimi.ingsw.am56.network.message.request.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientMain {
    public static void main(String[] args) {
        try {
            // cerchiamo il registro sulla porta 1099
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);

            // ottenimento del riferimento al server (lo stub)
            VirtualServerRmi serverStub = (VirtualServerRmi) registry.lookup("MesosServer");

            // creazione della view (client remoto)
            RmiClient view = new RmiClient();

            // registrazione sul server
            System.out.println("[CLIENT] Connessione in corso...");
            serverStub.addClient(view);
            System.out.println("[CLIENT] Registrato correttamente.");

            // test
            serverStub.addPlayer("Alice", "RED");

            serverStub.addPlayer("Bob", "BLUE");

            serverStub.startGame();

            serverStub.moveTotem(new TotemMoveRequest("Alice", "C"));

            serverStub.moveTotem(new TotemMoveRequest("Bob", "F"));

            serverStub.cardPick(new CardPickRequest("Alice", 99));

            serverStub.cardPick(new CardPickRequest("Bob", 100));

            serverStub.cardPick(new CardPickRequest("Bob", 101));


        } catch (Exception e) {
            System.err.println("[CLIENT] Errore durante l'esecuzione: " + e.getMessage());
            e.printStackTrace();
        }
    }
}