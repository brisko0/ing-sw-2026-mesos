package it.polimi.ingsw.am56;

import it.polimi.ingsw.am56.network.NetworkConnectionFactory;
import it.polimi.ingsw.am56.view.TUI.TUIContext;

public class ClientMainTUI2 {
    public static void main(String[] args) {
        NetworkConnectionFactory factory = new NetworkConnectionFactory();
        TUIContext context = new TUIContext(factory);

        context.start();
    }
}