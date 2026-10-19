package it.polimi.ingsw.am56.GUIClients;

import it.polimi.ingsw.am56.network.NetworkConnectionFactory; // Importa la tua classe concreta
import it.polimi.ingsw.am56.view.GUI.GUIApp;
import javafx.application.Application;

/**
 * Entry point of the GUI client. It builds the concrete {@link NetworkConnectionFactory}, hands it to
 * the {@link GUIApp} and launches the JavaFX application, which then drives the whole client UI.
 */
public class ClientMain {
    /**
     * Starts the GUI client: wires the network connection factory into {@link GUIApp} and launches
     * the JavaFX application.
     *
     * @param args command-line arguments forwarded to {@link Application#launch}
     */
    public static void main(String[] args) {
        System.out.println("Avvio del client in corso...");

        // Usiamo la VERA fabbrica che abbiamo costruito, con dentro il codice corretto
        NetworkConnectionFactory factory = new NetworkConnectionFactory();

        // Passiamo la fabbrica alla GUI e lanciamo
        GUIApp.setConnectionFactory(factory);
        Application.launch(GUIApp.class, args);
    }
}