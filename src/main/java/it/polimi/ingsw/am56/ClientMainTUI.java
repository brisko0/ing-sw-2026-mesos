package it.polimi.ingsw.am56;

import it.polimi.ingsw.am56.network.NetworkConnectionFactory;

public class ClientMainTUI {
    public static void main(String[] args) {
        NetworkConnectionFactory factory = new NetworkConnectionFactory();
        TUIController tuiController = new TUIController(factory);

        tuiController.start();
    }
}