package it.polimi.ingsw.am56;

import it.polimi.ingsw.am56.network.NetworkConnectionFactory;
import it.polimi.ingsw.am56.view.GUI.GUIApp;
import javafx.application.Application;

public class ClientMain {
    public static void main(String[] args) {
        System.out.println("Avvio del client in corso...");

        NetworkConnectionFactory factory = new NetworkConnectionFactory();

        GUIApp.setConnectionFactory(factory);
        Application.launch(GUIApp.class, args);
    }
}