package it.polimi.ingsw.am56.view.GUI.controllers;

import it.polimi.ingsw.am56.core.ServerAdapter;
import it.polimi.ingsw.am56.core.UpdateHandler;
import it.polimi.ingsw.am56.network.message.request.LobbyStateRequest;
import it.polimi.ingsw.am56.network.message.update.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;

public class PreLobbyController implements UpdateHandler, MessageVisitor {

    @FXML private GridPane createBox;
    @FXML private GridPane joinBox;

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

    @Override
    public void visit(PreLobbyStateMessage message) {
        Platform.runLater(() -> {
            if (message.isGameAlreadyCreated()) {
                System.out.println("[GUI PRE-LOBBY] partita esistente: attivo JOIN, disattivo CREATE");
                createBox.setDisable(true);
                createBox.setOpacity(0.5);
                joinBox.setDisable(false);
                joinBox.setOpacity(1.0);
            } else {
                System.out.println("[GUI PRE-LOBBY] nessuna partita: attivo CREATE, disattivo JOIN");
                createBox.setDisable(false);
                createBox.setOpacity(1.0);
                joinBox.setDisable(true);
                joinBox.setOpacity(0.5);
            }
        });
    }
}