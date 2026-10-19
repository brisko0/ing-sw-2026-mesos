package it.polimi.ingsw.am56.view.TUI;

import it.polimi.ingsw.am56.core.ConnectionFactory;
import it.polimi.ingsw.am56.core.ServerAdapter;
import it.polimi.ingsw.am56.view.TUI.controllers.*;

/**
 * Central context of the textual client and holder of its shared, long-lived collaborators: the
 * {@link ConnectionFactory}, the {@link TUIDrawer} used to render to the terminal, the
 * {@link TUIInputReader} used to read user input and, once established, the {@link ServerAdapter} and
 * the local player's nickname.
 * <p>
 * It also drives the screen-state machine: the current {@link BaseTUIController} represents the active
 * screen, and {@link #changeState(BaseTUIController)} switches to a new one — re-pointing the server
 * adapter's update handler at it so incoming server messages are routed to the screen now on display.
 */
public class TUIContext {
    private final ConnectionFactory factory;
    private final TUIDrawer drawer;
    private final TUIInputReader inputReader;

    private ServerAdapter serverAdapter;
    private String localNickname;
    private BaseTUIController currentState;

    /**
     * Creates the context with the given connection factory and initializes the drawer (loading the
     * JSON-backed card catalog once) and the input reader.
     *
     * @param factory the factory used to open the server connection
     */
    public TUIContext(ConnectionFactory factory) {
        this.factory = factory;
        // Load the JSON-backed card lookup once; the drawer uses it to tint and
        // icon-mark each card by class/era at render time.
        this.drawer = new TUIDrawer(new CardCatalog());
        this.inputReader = new TUIInputReader(this.drawer);
    }

    /**
     * Starts the TUI: clears the screen, prints the title and enters the initial network-setup screen.
     */
    public void start() {
        drawer.clearScreen();
        drawer.printTitle();
        changeState(new NetworkSetupController(this));
    }

    /**
     * Switches to a new screen: makes it the current controller, routes the server adapter's updates to
     * it (if a connection is already established) and runs its entry logic.
     *
     * @param newState the controller for the screen to display
     */
    public void changeState(BaseTUIController newState) {
        this.currentState = newState;
        if (this.serverAdapter != null) {
            this.serverAdapter.setUpdateHandler(this.currentState);
        }
        this.currentState.onEnter();
    }

    /**
     * @return the connection factory used to open server connections
     */
    public ConnectionFactory getFactory() {
        return factory;
    }

    /**
     * @return the drawer used to render to the terminal
     */
    public TUIDrawer getDrawer() {
        return drawer;
    }

    /**
     * @return the reader used to read user input
     */
    public TUIInputReader getInputReader() {
        return inputReader;
    }

    /**
     * @return the active server adapter, or {@code null} if not connected yet
     */
    public ServerAdapter getServerAdapter() {
        return serverAdapter;
    }

    /**
     * Sets the active server adapter and routes its updates to the current screen.
     *
     * @param serverAdapter the established server connection
     */
    public void setServerAdapter(ServerAdapter serverAdapter) {
        this.serverAdapter = serverAdapter;
        this.serverAdapter.setUpdateHandler(this.currentState);
    }

    /**
     * @return the local player's nickname, or {@code null} if not chosen yet
     */
    public String getLocalNickname() {
        return localNickname;
    }

    /**
     * Stores the local player's nickname.
     *
     * @param localNickname the chosen nickname
     */
    public void setLocalNickname(String localNickname) {
        this.localNickname = localNickname;
    }
}