package it.polimi.ingsw.am56.view.TUI.controllers;

import it.polimi.ingsw.am56.core.UpdateHandler;
import it.polimi.ingsw.am56.network.message.update.*;
import it.polimi.ingsw.am56.view.TUI.*;

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
}