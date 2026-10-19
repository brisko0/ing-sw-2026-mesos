package it.polimi.ingsw.am56.view.TUI.controllers;

import it.polimi.ingsw.am56.core.UpdateHandler;
import it.polimi.ingsw.am56.network.message.update.ErrorMessage;
import it.polimi.ingsw.am56.network.message.update.MessageVisitor;
import it.polimi.ingsw.am56.network.message.update.ServerMessage;
import it.polimi.ingsw.am56.view.TUI.TUIContext;
import it.polimi.ingsw.am56.view.TUI.TUIDrawer;
import it.polimi.ingsw.am56.view.TUI.TUIInputReader;

/**
 * Base class for every TUI screen. Each screen is both an {@link UpdateHandler} (so the active screen
 * receives server messages, errors and disconnections from the connection) and a {@link MessageVisitor}
 * (so those messages are dispatched to a typed {@code visit} method via double dispatch). Subclasses
 * implement {@link #onEnter()} to render and drive their screen, and override the {@code visit} methods
 * for the message types they care about.
 * <p>
 * It provides shared access to the {@link TUIContext} and its drawer/input reader, plus default
 * handling for errors (printed) and disconnections (print and exit).
 */
public abstract class BaseTUIController implements UpdateHandler, MessageVisitor {
    protected final TUIContext context;
    protected final TUIDrawer drawer;
    protected final TUIInputReader inputReader;

    /**
     * Creates a screen controller bound to the shared context, caching its drawer and input reader.
     *
     * @param context the shared TUI context
     */
    public BaseTUIController(TUIContext context) {
        this.context = context;
        this.drawer = context.getDrawer();
        this.inputReader = context.getInputReader();
    }

    /**
     * Renders this screen and runs its interaction logic when it becomes the active screen.
     */
    public abstract void onEnter();

    /**
     * Dispatches an incoming server message to the matching {@code visit} method via the visitor
     * pattern.
     *
     * @param message the message received from the server
     */
    @Override
    public void handleUpdate(ServerMessage message) {
        message.accept(this);
    }

    /**
     * Default error handling: prints the error to the terminal.
     *
     * @param message the error message
     */
    @Override
    public void handleError(String message) {
        drawer.printError(message);
    }

    /**
     * Handles an {@link ErrorMessage} update by routing it through {@link #handleError(String)}.
     *
     * @param message the error message
     */
    @Override
    public void visit(ErrorMessage message) {
        handleError(message.details());
    }

    /**
     * Default disconnection handling: notifies the user the server is down and terminates the client.
     */
    @Override
    public void handleDisconnect() {
        drawer.printServerDown();
        System.exit(1);
    }
}
