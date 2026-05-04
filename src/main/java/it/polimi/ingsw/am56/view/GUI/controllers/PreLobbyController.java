package it.polimi.ingsw.am56.view.GUI.controllers;

import it.polimi.ingsw.am56.core.ServerAdapter;
import it.polimi.ingsw.am56.core.UpdateHandler;
import it.polimi.ingsw.am56.network.message.request.AddPlayerRequest;
import it.polimi.ingsw.am56.network.message.request.CreateGameRequest;
import it.polimi.ingsw.am56.network.message.request.LobbyStateRequest;
import it.polimi.ingsw.am56.network.message.update.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;


public class PreLobbyController implements UpdateHandler, MessageVisitor {

    private ServerAdapter serverAdapter;
    private String selectedColor = null;
    private ImageView lastSelectedTotem = null;

    private String localPlayerNickname;

    @FXML private StackPane rootPane;

    @FXML private GridPane createBox;
    @FXML private GridPane joinBox;
    @FXML private TextField createNicknameField;
    @FXML private TextField gameSizeField;
    @FXML private TextField joinNicknameField;

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
                System.out.println("[GUI PRE-LOBBY] Partita esistente: attivo JOIN, disattivo CREATE.");
                createBox.setDisable(true);
                createBox.setOpacity(0.5);
                joinBox.setDisable(false);
                joinBox.setOpacity(1.0);
            } else {
                System.out.println("[GUI PRE-LOBBY] Nessuna partita: attivo CREATE, disattivo JOIN.");
                createBox.setDisable(false);
                createBox.setOpacity(1.0);
                joinBox.setDisable(true);
                joinBox.setOpacity(0.5);
            }
        });
    }

    @FXML
    private void handleCreateGame() {
        String nickname = createNicknameField.getText().trim();
        String sizeStr = gameSizeField.getText().trim();

        if (!nickname.isEmpty() && !sizeStr.isEmpty() && selectedColor != null) {
            try {
                int size = Integer.parseInt(sizeStr);
                if (size < 2 || size > 5) {
                    System.out.println("[LOBBY] errore: la partita deve essere da 2 a 5 giocatori");
                    return;
                }

                this.localPlayerNickname = nickname;

                System.out.println("[LOBBY] invio richiesta creazione: " + nickname + " (" + selectedColor + ")");
                serverAdapter.sendRequest(new CreateGameRequest(nickname, selectedColor, size));
            } catch (NumberFormatException e) {
                System.err.println("[LOBBY] errore: la dimensione deve essere un numero");
            }
        } else {
            System.out.println("[LOBBY] compilare tutti i campi e scegliere un colore");
        }
    }

    @FXML
    private void handleJoinGame() {
        String nickname = joinNicknameField.getText().trim();
        if (!nickname.isEmpty() && selectedColor != null) {

            this.localPlayerNickname = nickname;

            System.out.println("[LOBBY] invio richiesta di partecipazione: " + nickname + " (totem: " + selectedColor + ")");
            serverAdapter.sendRequest(new AddPlayerRequest(nickname, selectedColor));
        } else {
            if (nickname.isEmpty()) {
                System.out.println("[LOBBY] inserire un nickname prima di unirsi");
            } else {
                System.out.println("[LOBBY] scegliere un totem prima di unirsi");
            }
        }
    }

    @FXML
    private void selectColor(MouseEvent event) {
        ImageView clickedTotem = (ImageView) event.getSource();

        if (lastSelectedTotem != null) {
            lastSelectedTotem.getStyleClass().remove("totem-selected");
        }

        clickedTotem.getStyleClass().add("totem-selected");
        lastSelectedTotem = clickedTotem;

        selectedColor = (String) clickedTotem.getUserData();
        System.out.println("[PRE-LOBBY] Colore selezionato: " + selectedColor);
    }

    @Override
    public void visit(LobbyStateMessage message) {
        Platform.runLater(() -> goToLobby(message));
    }

    private void goToLobby(LobbyStateMessage message) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/GUI/fxml/Lobby.fxml"));
            Parent lobbyRoot = loader.load();

            LobbyController lobbyController = loader.getController();
            lobbyController.setServerAdapter(serverAdapter);

            lobbyController.setLocalPlayerNickname(this.localPlayerNickname);

            lobbyController.visit(message);

            rootPane.getScene().setRoot(lobbyRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}