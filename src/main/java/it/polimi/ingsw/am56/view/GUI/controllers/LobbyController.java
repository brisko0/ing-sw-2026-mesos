package it.polimi.ingsw.am56.view.GUI.controllers;

import it.polimi.ingsw.am56.core.ServerAdapter;
import it.polimi.ingsw.am56.core.UpdateHandler;
import it.polimi.ingsw.am56.network.message.update.MessageVisitor;
import it.polimi.ingsw.am56.network.message.update.ServerMessage;

public class LobbyController implements UpdateHandler, MessageVisitor {

    private ServerAdapter serverAdapter;
    private String localPlayerNickname;

    public void setLocalPlayerNickname(String nickname) {
        this.localPlayerNickname = nickname;
    }

    public void setServerAdapter(ServerAdapter serverAdapter) {
        this.serverAdapter = serverAdapter;
        this.serverAdapter.setUpdateHandler(this);
    }

    @Override
    public void handleUpdate(ServerMessage message) {
        message.accept(this);
    }
}