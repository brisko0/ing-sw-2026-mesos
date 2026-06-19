package it.polimi.ingsw.am56.view.GUI.controllers;

import it.polimi.ingsw.am56.core.ServerAdapter;
import it.polimi.ingsw.am56.core.UpdateHandler;
import it.polimi.ingsw.am56.network.message.classesDTO.PlayerDTO;
import it.polimi.ingsw.am56.network.message.request.LobbyStateRequest;
import it.polimi.ingsw.am56.network.message.update.ErrorMessage;
import it.polimi.ingsw.am56.network.message.update.GameAbortedMessage;
import it.polimi.ingsw.am56.network.message.update.LobbyStateMessage;
import it.polimi.ingsw.am56.network.message.update.MessageVisitor;
import it.polimi.ingsw.am56.network.message.update.ServerMessage;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.io.IOException;

public class LobbyController implements UpdateHandler, MessageVisitor {

    private ServerAdapter serverAdapter;
    private String localPlayerNickname;

    @FXML private StackPane rootPane;
    @FXML private GridPane lobbyBox;
    @FXML private VBox playerListContainer;
    @FXML private Label statusLabel;

    @FXML private VBox reconnectingOverlay;
    @FXML private VBox gameAbortedOverlay;

    /** Set once a GameAbortedMessage arrives, so the server-lost overlay is not shown on top of it. */
    private volatile boolean gameAborted = false;

    @FXML
    public void initialize() {
        // Lock the lobby frame to the BoxLobby.png aspect ratio (2400x1340) and fill it with the
        // artwork (100% 100% in CSS): this way the player list and the status/countdown label,
        // which are positioned by percentages inside the frame, stay aligned with the artwork at
        // any window size instead of drifting into the empty letterbox area.
        final double ratio = 2400.0 / 1340.0;
        lobbyBox.maxWidthProperty().bind(
                Bindings.min(rootPane.widthProperty().multiply(0.60),
                        rootPane.heightProperty().multiply(0.70).multiply(ratio)));
        lobbyBox.maxHeightProperty().bind(lobbyBox.maxWidthProperty().divide(ratio));
    }

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
    public void handleError(String message) {
        GuiErrorToast.show(rootPane, message);
    }

    @Override
    public void visit(ErrorMessage message) {
        handleError(message.details());
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

    private void addPlayerRow(int index, String nickname, String color) {
        HBox row = new HBox();
        row.getStyleClass().add("player-row");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setSpacing(20);
        row.setPrefHeight(60);

        Label nameLabel = new Label(index + ". " + nickname.toUpperCase());
        nameLabel.getStyleClass().add("player-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String imagePath = "/view/GUI/images/totem2D/" + color.toLowerCase() + "_2D.png";

        try {
            Image img = new Image(getClass().getResourceAsStream(imagePath));
            if (img.isError()) throw new Exception("Immagine non trovata");

            ImageView totemImg = new ImageView(img);
            totemImg.setFitHeight(50);
            totemImg.setPreserveRatio(true);
            row.getChildren().addAll(nameLabel, spacer, totemImg);
        } catch (Exception e) {
            System.err.println("[ERRORE GUI] Impossibile caricare: " + imagePath);
            row.getChildren().addAll(nameLabel, spacer);
        }

        playerListContainer.getChildren().add(row);
    }

    private void startCountdown() {
        final int[] secondsLeft = {5};
        statusLabel.setText("La partita inizierà tra " + secondsLeft[0] + "...");

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            secondsLeft[0]--;
            if (secondsLeft[0] > 0) {
                statusLabel.setText("La partita inizierà tra " + secondsLeft[0] + "...");
            } else {
                goToGame();
            }
        }));

        timeline.setCycleCount(5);
        timeline.play();
    }

    private void goToGame() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/GUI/fxml/ViewGame.fxml"));
            Parent gameRoot = loader.load();

            ViewGameController viewGameController = loader.getController();
            viewGameController.setLocalPlayerNickname(this.localPlayerNickname);
            viewGameController.setServerAdapter(this.serverAdapter);

            rootPane.getScene().setRoot(gameRoot);

            System.out.println("[GUI] Passaggio alla schermata di gioco completato.");
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

    /**
     * Closes the window from the "Game aborted" overlay.
     */
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
            serverAdapter.sendRequest(new LobbyStateRequest());
        });
    }
}