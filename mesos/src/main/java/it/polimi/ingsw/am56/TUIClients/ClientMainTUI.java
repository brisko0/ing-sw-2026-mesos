package it.polimi.ingsw.am56.TUIClients;

import it.polimi.ingsw.am56.network.NetworkConnectionFactory;
import it.polimi.ingsw.am56.view.TUI.TUIContext;

/**
 * Entry point of the textual (TUI) client. It builds the concrete {@link NetworkConnectionFactory},
 * creates a {@link TUIContext} around it and starts the text-based interaction loop.
 */
public class ClientMainTUI {
    /**
     * Starts the TUI client: creates the network connection factory, wraps it in a {@link TUIContext}
     * and starts it.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        NetworkConnectionFactory factory = new NetworkConnectionFactory();
        TUIContext context = new TUIContext(factory);

        context.start();
    }
}