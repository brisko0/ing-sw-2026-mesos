package it.polimi.ingsw.am56.view.GUI.controllers;

import it.polimi.ingsw.am56.core.ServerAdapter;
import it.polimi.ingsw.am56.core.UpdateHandler;
import it.polimi.ingsw.am56.network.message.classesDTO.PlayerDTO;
import it.polimi.ingsw.am56.network.message.update.LobbyStateMessage;
import it.polimi.ingsw.am56.network.message.update.MessageVisitor;
import it.polimi.ingsw.am56.network.message.update.ServerMessage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;

public class LobbyController implements UpdateHandler, MessageVisitor {

    private ServerAdapter serverAdapter;
    private String localPlayerNickname;

    @FXML private StackPane rootPane;
    @FXML private VBox playerListContainer;
    @FXML private Label statusLabel;

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

    @Override
    public void visit(LobbyStateMessage message) {
        Platform.runLater(() -> {
            playerListContainer.getChildren().clear();

            int count = 1;
            for (PlayerDTO p : message.players()) {
                addPlayerRow(count++, p.nickname(), p.color());
            }

            int current = message.players().size();
            int max = message.gameSize();

            if (current == max) {
                startCountdown();
            } else {
                statusLabel.setText("Giocatori pronti nella caverna: " + current + " / " + max);
            }
        });
    }
}