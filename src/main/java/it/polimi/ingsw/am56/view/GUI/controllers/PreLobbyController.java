package it.polimi.ingsw.am56.view.GUI.controllers;

import it.polimi.ingsw.am56.core.ServerAdapter;
import it.polimi.ingsw.am56.core.UpdateHandler;
import it.polimi.ingsw.am56.network.message.request.LobbyStateRequest;
import it.polimi.ingsw.am56.network.message.update.*;

public class PreLobbyController implements UpdateHandler, MessageVisitor {

    private ServerAdapter serverAdapter;

    public void setServerAdapter(ServerAdapter serverAdapter) {
        this.serverAdapter = serverAdapter;
        this.serverAdapter.setUpdateHandler(this);

        System.out.println("[GUI PRE-LOBBY] Richiesta stato partita inviata...");
        this.serverAdapter.sendRequest(new LobbyStateRequest());
    }


    @Override
    public void handleUpdate(ServerMessage message) {
        message.accept(this);
    }
}