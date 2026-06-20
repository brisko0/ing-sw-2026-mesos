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

/**
 * Controller for the lobby screen, shown while players gather before a game starts. It lists the
 * players that have joined, and once the lobby is full runs a short countdown before switching to
 * the game screen.
 * <p>
 * As an {@link UpdateHandler} it receives server pushes and, as a {@link MessageVisitor},
 * dispatches each {@link ServerMessage} to the matching {@code visit} overload — here it reacts to
 * lobby-state updates, errors, and game-aborted notifications. It also shows reconnecting / game-
 * aborted overlays when the connection drops or the game can no longer continue.
 */
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

    /** The start countdown; kept so it can be stopped if the game is aborted before it elapses. */
    private Timeline countdownTimeline;

    /**
     * Binds the lobby frame's size to keep it at the lobby artwork's aspect ratio within the
     * window, so the percentage-positioned player list and status label stay aligned with the
     * artwork at any window size. Invoked automatically by JavaFX after the FXML is loaded.
     */
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

    /**
     * Sets the nickname of the player running this client; used when handing control over to the
     * game screen.
     *
     * @param nickname the local player's nickname
     */
    public void setLocalPlayerNickname(String nickname) {
        this.localPlayerNickname = nickname;
    }

    /**
     * Sets the server adapter and registers this controller as its update handler, so server
     * pushes are routed here.
     *
     * @param serverAdapter the adapter wrapping the server connection
     */
    public void setServerAdapter(ServerAdapter serverAdapter) {
        this.serverAdapter = serverAdapter;
        this.serverAdapter.setUpdateHandler(this);
    }

    /**
     * Entry point for server pushes: dispatches the message to the matching {@code visit} overload
     * via the Visitor pattern.
     *
     * @param message the message received from the server
     */
    @Override
    public void handleUpdate(ServerMessage message) {
        message.accept(this);
    }

    /**
     * Shows a transient error toast over the lobby.
     *
     * @param message the human-readable error description
     */
    @Override
    public void handleError(String message) {
        GuiErrorToast.show(rootPane, message);
    }

    /**
     * Handles an {@link ErrorMessage} by showing its detail as an error toast.
     *
     * @param message the error message from the server
     */
    @Override
    public void visit(ErrorMessage message) {
        handleError(message.details());
    }

    /**
     * Rebuilds the player list from the lobby update. When the lobby reaches the target game size
     * it starts the start-of-game countdown; otherwise it shows how many players are still
     * missing. Runs on the JavaFX thread.
     *
     * @param message the lobby state (current players and target size)
     */
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

    /**
     * Appends one row to the player list showing the player's position, nickname and totem image.
     * If the totem image cannot be loaded the row is still added without it.
     *
     * @param index    the 1-based position shown next to the nickname
     * @param nickname the player's nickname
     * @param color    the player's totem colour (selects the totem image)
     */
    private void addPlayerRow(int index, String nickname, String color) {
        HBox row = new HBox();
        row.getStyleClass().add("player-row");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setSpacing(20);
        row.setPrefHeight(60);

        Label nameLabel = new Label(index + ". " + nickname);
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

    /**
     * Starts the 5-second countdown shown when the lobby is full, updating the status label each
     * second and switching to the game screen when it reaches zero. The timeline is kept so it can
     * be stopped if the game is aborted before it elapses.
     */
    private void startCountdown() {
        final int[] secondsLeft = {5};
        statusLabel.setText("La partita inizierà tra " + secondsLeft[0] + "...");

        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            secondsLeft[0]--;
            if (secondsLeft[0] > 0) {
                statusLabel.setText("La partita inizierà tra " + secondsLeft[0] + "...");
            } else {
                goToGame();
            }
        }));

        countdownTimeline.setCycleCount(5);
        countdownTimeline.play();
    }

    /**
     * Switches to the in-game screen, reusing the current window: loads {@code ViewGame.fxml},
     * passes the local nickname and server adapter to its controller, and swaps the scene root.
     * Does nothing if the game was aborted during the countdown, so the empty board is never shown.
     */
    private void goToGame() {
        // If the game was aborted (e.g. a player closed their GUI during the countdown), do not
        // switch to the empty board: stay on the lobby with the "Partita annullata" overlay.
        if (gameAborted) return;
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

    /**
     * Handles a game-aborted notification: marks the game as aborted, stops any running countdown,
     * hides the reconnecting overlay and shows the game-aborted overlay. Runs on the JavaFX thread.
     *
     * @param message the game-aborted message
     */
    @Override
    public void visit(GameAbortedMessage message) {
        Platform.runLater(() -> {
            gameAborted = true;
            if (countdownTimeline != null) countdownTimeline.stop();
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

    /**
     * Shows the reconnecting overlay when the connection to the server drops, unless the game has
     * already been aborted.
     */
    @Override
    public void handleDisconnect() {
        if (gameAborted) return;
        Platform.runLater(() -> reconnectingOverlay.setVisible(true));
    }

    /**
     * Hides the reconnecting overlay once the connection is restored and asks the server for the
     * current lobby state to refresh the view.
     */
    @Override
    public void handleReconnected() {
        Platform.runLater(() -> {
            reconnectingOverlay.setVisible(false);
            serverAdapter.sendRequest(new LobbyStateRequest());
        });
    }
}