package it.polimi.ingsw.am56.view.TUI.controllers;

import it.polimi.ingsw.am56.core.UpdateHandler;
import it.polimi.ingsw.am56.network.message.update.MessageVisitor;
import it.polimi.ingsw.am56.network.message.update.ServerMessage;
import it.polimi.ingsw.am56.view.TUI.TUIContext;
import it.polimi.ingsw.am56.view.TUI.TUIDrawer;
import it.polimi.ingsw.am56.view.TUI.TUIInputReader;

public abstract class BaseTUIController implements UpdateHandler, MessageVisitor {
    protected final TUIContext context;
    protected final TUIDrawer drawer;
    protected final TUIInputReader inputReader;

    public BaseTUIController(TUIContext context) {
        this.context = context;
        this.drawer = context.getDrawer();
        this.inputReader = context.getInputReader();
    }

    public abstract void onEnter();

    @Override
    public void handleUpdate(ServerMessage message) {
        message.accept(this);
    }

    @Override
    public void handleDisconnect() {
        drawer.printServerDown();
        System.exit(1);
    }
}
