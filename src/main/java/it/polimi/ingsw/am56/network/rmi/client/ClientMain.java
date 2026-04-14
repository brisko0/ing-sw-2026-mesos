package it.polimi.ingsw.am56.network.rmi.client;

import it.polimi.ingsw.am56.network.message.request.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientMain {
    public static void main(String[] args) {
        try {
            // 1. Cerchiamo il registro sulla porta 1099
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);

            // 2. Otteniamo il riferimento al server (lo stub)
            VirtualServerRmi serverStub = (VirtualServerRmi) registry.lookup("MesosServer");

            // 3. Creiamo la nostra "View" (il client remoto)
            RmiClient view = new RmiClient();

            // 4. Ci registriamo sul server
            System.out.println("[CLIENT] Connessione in corso...");
            serverStub.addClient(view);
            System.out.println("[CLIENT] Registrato correttamente.");

            // 5. TEST FASE INIZIALE
            // Aggiungiamo noi stessi
            serverStub.addPlayer("Alice", "RED");

            // Per testare lo startGame servono almeno 2 giocatori.
            // In un test reale lanceresti due processi ClientMain.
            // Per ora ne aggiungiamo un secondo "fittizio" dalla stessa chiamata per sbloccare il gioco.
            serverStub.addPlayer("Bob", "BLUE");

            // 6. Avviamo la partita
            serverStub.startGame();

            // 7. Totem placement
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