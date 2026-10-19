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
import javafx.beans.binding.Bindings;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Controller for the pre-lobby screen, where the player either creates a new game, joins an
 * existing one, or reconnects to a game already in progress. It offers a "create" and a "join"
 * panel, a totem-colour picker, and enables only the relevant panel based on the server's
 * pre-lobby state.
 * <p>
 * As an {@link UpdateHandler}/{@link MessageVisitor} it reacts to server pushes: the pre-lobby
 * state (which panel to enable, which colours are taken), lobby updates (advancing to the lobby
 * once the local player has joined), a full game snapshot (jumping straight into the game on
 * reconnect), errors and game-aborted notifications.
 */
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

    @FXML private HBox createTotemBox;
    @FXML private HBox joinTotemBox;

    @FXML private VBox reconnectingOverlay;
    @FXML private VBox gameAbortedOverlay;

    /** Set once a GameAbortedMessage arrives, so the server-lost overlay is not shown on top of it. */
    private volatile boolean gameAborted = false;

    /**
     * Binds the totem-choice images of both panels so they scale down on small windows. Invoked
     * automatically by JavaFX after the FXML is loaded.
     */
    @FXML
    public void initialize() {
        bindTotemSizes(createTotemBox);
        bindTotemSizes(joinTotemBox);
    }

    /**
     * Keeps the totem-choice images at their natural 70px on normal/large windows, but lets them
     * shrink with the window height once it gets small — so they no longer overflow their row and
     * misalign the panel artwork. The reference is the root pane (sized by the window, not by its
     * content) to avoid a circular binding with the content-sized panels.
     */
    private void bindTotemSizes(HBox totemBox) {
        for (Node totem : totemBox.getChildren()) {
            ((ImageView) totem).fitHeightProperty()
                    .bind(Bindings.min(70.0, rootPane.heightProperty().multiply(0.12)));
        }
    }

    /**
     * Sets the server adapter, registers this controller as its update handler, and immediately
     * asks the server for the current lobby state so the right panel can be enabled.
     *
     * @param serverAdapter the adapter wrapping the server connection
     */
    public void setServerAdapter(ServerAdapter serverAdapter) {
        this.serverAdapter = serverAdapter;
        this.serverAdapter.setUpdateHandler(this);
        this.serverAdapter.sendRequest(new LobbyStateRequest());
    }

    /**
     * Entry point for server pushes: dispatches the message to the matching {@code visit} overload.
     *
     * @param message the message received from the server
     */
    @Override
    public void handleUpdate(ServerMessage message) {
        message.accept(this);
    }

    /**
     * Shows a transient error toast over the pre-lobby.
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

    // -------------------------------------------------------------------------
    // Server message handlers
    // -------------------------------------------------------------------------

    /**
     * Configures the screen from the pre-lobby state: if a game already exists only the join panel
     * is enabled (used for both joining the lobby and reconnecting), otherwise only the create
     * panel is enabled. Also remembers whether the game is already in progress (which switches
     * join into reconnect mode) and hides the totem colours already taken. Runs on the JavaFX
     * thread.
     *
     * @param message the pre-lobby state from the server
     */
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
            updateAvailableTotems(message.takenColors());
        });
    }

    /**
     * Hides (and unmanages) the totem images whose colour is already taken across both the create
     * and join panels, leaving only the colours still available for selection.
     *
     * @param takenColors the colours already chosen by other players (may be {@code null})
     */
    private void updateAvailableTotems(java.util.List<String> takenColors) {
        for (HBox box : java.util.List.of(createTotemBox, joinTotemBox)) {
            for (Node totem : box.getChildren()) {
                String color = (String) totem.getUserData();
                boolean taken = color != null && takenColors != null && takenColors.contains(color);
                totem.setVisible(!taken);
                totem.setManaged(!taken);
            }
        }
    }

    /**
     * Handles a lobby update: if the local player now appears among the joined players, advances
     * to the lobby screen; otherwise keeps the join panel enabled and refreshes the available
     * totem colours. Runs on the JavaFX thread.
     *
     * @param message the lobby state (current players and target size)
     */
    @Override
    public void visit(LobbyStateMessage message) {
        boolean localJoined = localPlayerNickname != null
                && message.players().stream().anyMatch(p -> p.nickname().equals(localPlayerNickname));

        Platform.runLater(() -> {
            if (localJoined) {
                goToLobby(message);
                return;
            }
            createBox.setDisable(true);
            createBox.setOpacity(0.5);
            joinBox.setDisable(false);
            joinBox.setOpacity(1.0);
            updateAvailableTotems(message.players().stream().map(p -> p.color()).toList());
        });
    }

    /**
     * Handles a full game snapshot received in the pre-lobby, which happens on reconnect: jumps
     * straight to the in-game screen. Runs on the JavaFX thread.
     *
     * @param message the game snapshot message
     */
    @Override
    public void visit(GameSnapshotMessage message) {
        Platform.runLater(() -> goToGameDirectly());
    }

    // -------------------------------------------------------------------------
    // Button handlers
    // -------------------------------------------------------------------------

    /**
     * Handles the "create game" button: validates the nickname, the game size (2–5) and the
     * chosen colour, then sends a {@link CreateGameRequest}. Invalid input is reported via an
     * error toast and no request is sent.
     */
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

    /**
     * Handles the "join" button. If the game is already in progress this acts as a reconnect,
     * sending a {@link ReconnectRequest} with just the (original) nickname; otherwise it is a
     * normal lobby join, sending an {@link AddPlayerRequest} with nickname and chosen colour.
     * Missing input is reported via an error toast.
     */
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

    /**
     * Handles clicking a totem image: highlights the clicked totem (clearing the previous
     * selection's highlight) and records its colour as the selected one.
     *
     * @param event the mouse event whose source is the clicked totem image
     */
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

    /**
     * Switches to the lobby screen, reusing the current window: loads {@code Lobby.fxml}, wires the
     * server adapter and local nickname into its controller, replays the current lobby state into
     * it, and swaps the scene root.
     *
     * @param message the lobby state to forward to the lobby controller
     */
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

    /**
     * Switches straight to the in-game screen (used on reconnect): loads {@code ViewGame.fxml},
     * wires the local nickname and server adapter into its controller, and swaps the scene root.
     */
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

    /**
     * Handles a game-aborted notification: marks the game as aborted, hides the reconnecting
     * overlay and shows the game-aborted overlay. Runs on the JavaFX thread.
     *
     * @param message the game-aborted message
     */
    @Override
    public void visit(GameAbortedMessage message) {
        Platform.runLater(() -> {
            gameAborted = true;
            reconnectingOverlay.setVisible(false);
            gameAbortedOverlay.setVisible(true);
        });
    }

    /**
     * Closes the window from the "Partita annullata" overlay, exactly as the window's close
     * button would.
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
            serverAdapter.sendRequest(new LobbyStateRequest()); // richiede lo stato aggiornato
        });
    }
}