package it.polimi.ingsw.am56.view.GUI.controllers;

import it.polimi.ingsw.am56.core.ServerAdapter;
import it.polimi.ingsw.am56.core.UpdateHandler;
import it.polimi.ingsw.am56.network.message.request.AddPlayerRequest;
import it.polimi.ingsw.am56.network.message.request.CreateGameRequest;
import it.polimi.ingsw.am56.network.message.request.LobbyStateRequest;
import it.polimi.ingsw.am56.network.message.request.ReconnectRequest;
import it.polimi.ingsw.am56.network.message.update.*;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;

public class PreLobbyController implements UpdateHandler, MessageVisitor {

    private ServerAdapter serverAdapter;
    private String selectedColor = null;
    private ImageView lastSelectedTotem = null;
    private String localPlayerNickname;

    private boolean gameInProgress = false;

    @FXML private StackPane rootPane;
    @FXML private GridPane createBox;
    @FXML private GridPane joinBox;
    @FXML private TextField createNicknameField;
    @FXML private TextField gameSizeField;
    @FXML private TextField joinNicknameField;

    @FXML private VBox reconnectingOverlay;
    @FXML private VBox gameAbortedOverlay;

    private volatile boolean gameAborted = false;

    public void setServerAdapter(ServerAdapter serverAdapter) {
        this.serverAdapter = serverAdapter;
        this.serverAdapter.setUpdateHandler(this);
        this.serverAdapter.sendRequest(new LobbyStateRequest());
    }

    @Override
    public void handleUpdate(ServerMessage message) {
        message.accept(this);
    }

    @Override
    public void handleError(String message) {
        GuiErrorToast.show(rootPane, message);
    }

    @Override
    public void visit(ErrorMessage message) {
        handleError(message.details());
    }

    // -------------------------------------------------------------------------
    // Server message handlers
    // -------------------------------------------------------------------------

    @Override
    public void visit(PreLobbyStateMessage message) {
        this.gameInProgress = message.isGameInProgress();

        Platform.runLater(() -> {
            if (message.isGameAlreadyCreated()) {
                // Both lobby-join and reconnect show only the JOIN box.
                // The difference is handled internally in handleJoinGame().
                createBox.setDisable(true);
                createBox.setOpacity(0.5);
                joinBox.setDisable(false);
                joinBox.setOpacity(1.0);
            } else {
                createBox.setDisable(false);
                createBox.setOpacity(1.0);
                joinBox.setDisable(true);
                joinBox.setOpacity(0.5);
            }
        });
    }

    @Override
    public void visit(LobbyStateMessage message) {
        Platform.runLater(() -> goToLobby(message));
    }

    @Override
    public void visit(GameSnapshotMessage message) {
        Platform.runLater(() -> goToGameDirectly());
    }

    // -------------------------------------------------------------------------
    // Button handlers
    // -------------------------------------------------------------------------

    @FXML
    private void handleCreateGame() {
        String nickname = createNicknameField.getText().trim();
        String sizeStr = gameSizeField.getText().trim();

        if (!nickname.isEmpty() && !sizeStr.isEmpty() && selectedColor != null) {
            try {
                int size = Integer.parseInt(sizeStr);
                if (size < 2 || size > 5) {
                    handleError("La partita deve essere da 2 a 5 giocatori.");
                    return;
                }
                this.localPlayerNickname = nickname;
                serverAdapter.sendRequest(new CreateGameRequest(nickname, selectedColor, size));
            } catch (NumberFormatException e) {
                handleError("La dimensione deve essere un numero.");
            }
        } else {
            handleError("Compila tutti i campi e scegli un colore.");
        }
    }

    @FXML
    private void handleJoinGame() {
        String nickname = joinNicknameField.getText().trim();

        if (gameInProgress) {
            // Reconnect after crash: only nickname needed, no color.
            if (!nickname.isEmpty()) {
                this.localPlayerNickname = nickname;
                System.out.println("[LOBBY] Invio ReconnectRequest per: " + nickname);
                serverAdapter.sendRequest(new ReconnectRequest(nickname));
            } else {
                handleError("Inserisci il nickname originale per riconnetterti.");
            }
        } else {
            // Normal join during lobby phase.
            if (!nickname.isEmpty() && selectedColor != null) {
                this.localPlayerNickname = nickname;
                serverAdapter.sendRequest(new AddPlayerRequest(nickname, selectedColor));
            } else {
                handleError("Inserisci il nickname e scegli un colore.");
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
    }

    // -------------------------------------------------------------------------
    // Scene transitions
    // -------------------------------------------------------------------------

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

    private void goToGameDirectly() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/GUI/fxml/ViewGame.fxml"));
            Parent gameRoot = loader.load();

            ViewGameController viewGameController = loader.getController();
            viewGameController.setLocalPlayerNickname(this.localPlayerNickname);
            viewGameController.setServerAdapter(serverAdapter);

            rootPane.getScene().setRoot(gameRoot);
            System.out.println("[GUI] Riconnessione avvenuta, schermata di gioco caricata.");
        } catch (IOException e) {
            System.err.println("[GUI] Errore nel caricamento del gioco: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------------------------
    // persistenza
    // -------------------------------------------------------------------------

    @Override
    public void visit(GameAbortedMessage message) {
        Platform.runLater(() -> {
            gameAborted = true;
            reconnectingOverlay.setVisible(false);
            gameAbortedOverlay.setVisible(true);
        });
    }

    @FXML
    private void handleAbortClose() {
        Stage stage = (Stage) gameAbortedOverlay.getScene().getWindow();
        stage.close();
        Platform.exit();
    }

    @Override
    public void handleDisconnect() {
        if (gameAborted) return;
        Platform.runLater(() -> reconnectingOverlay.setVisible(true));
    }

    @Override
    public void handleReconnected() {
        Platform.runLater(() -> {
            reconnectingOverlay.setVisible(false);
            serverAdapter.sendRequest(new LobbyStateRequest()); // richiede lo stato aggiornato
        });
    }
}