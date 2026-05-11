package it.polimi.ingsw.am56.view.TUI;

import it.polimi.ingsw.am56.view.TUI.controllers.*;
import it.polimi.ingsw.am56.core.ConnectionFactory;
import it.polimi.ingsw.am56.core.ServerAdapter;

public class TUIContext {
    private final ConnectionFactory factory;
    private final TUIDrawer drawer;
    private final TUIInputReader inputReader;

    private ServerAdapter serverAdapter;
    private String localNickname;
    private BaseTUIController currentState;

    public TUIContext(ConnectionFactory factory) {
        this.factory = factory;
        this.drawer = new TUIDrawer();
        this.inputReader = new TUIInputReader(this.drawer);
    }

    public void start() {
        changeState(new NetworkSetupController(this));
    }

    public void changeState(BaseTUIController newState) {
        this.currentState = newState;
        if (this.serverAdapter != null) {
            this.serverAdapter.setUpdateHandler(this.currentState);
        }
        this.currentState.onEnter();
    }

    public ConnectionFactory getFactory() {
        return factory;
    }

    public TUIDrawer getDrawer() {
        return drawer;
    }

    public TUIInputReader getInputReader() {
        return inputReader;
    }

    public ServerAdapter getServerAdapter() {
        return serverAdapter;
    }

    public void setServerAdapter(ServerAdapter serverAdapter) {
        this.serverAdapter = serverAdapter;
        this.serverAdapter.setUpdateHandler(this.currentState);
    }

    public String getLocalNickname() {
        return localNickname;
    }

    public void setLocalNickname(String localNickname) {
        this.localNickname = localNickname;
    }
}