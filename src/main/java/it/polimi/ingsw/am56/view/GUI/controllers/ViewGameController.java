package it.polimi.ingsw.am56.view.GUI.controllers;

import it.polimi.ingsw.am56.core.ServerAdapter;
import it.polimi.ingsw.am56.core.UpdateHandler;
import it.polimi.ingsw.am56.model.state.State;
import it.polimi.ingsw.am56.view.GUI.CardInfoCatalog;
import it.polimi.ingsw.am56.network.message.classesDTO.*;
import it.polimi.ingsw.am56.network.message.request.GameSyncRequest;
import it.polimi.ingsw.am56.network.message.request.CardPickRequest;
import it.polimi.ingsw.am56.network.message.request.ReconnectRequest;
import it.polimi.ingsw.am56.network.message.request.SkipExtraPickRequest;
import it.polimi.ingsw.am56.network.message.request.TotemMoveRequest;
import it.polimi.ingsw.am56.network.message.update.*;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Controller for the main in-game screen. It renders the whole board — the four card rows, the
 * central deck/order/offer tiles, the totems, and a per-player dashboard — from the snapshots and
 * incremental updates pushed by the server, and turns local clicks into game requests
 * (totem placement, card picks, skipping the bonus pick).
 * <p>
 * As an {@link UpdateHandler}/{@link MessageVisitor} it dispatches each {@link ServerMessage} to
 * the matching {@code visit} overload. Much of the class is presentation logic: hover tooltips and
 * zoom, pulsing glows on the nodes the local player may currently interact with, and animations
 * for card picks, totem moves, end-of-round/era board shifts and event resolution.
 * <p>
 * Because some updates arrive while an animation is playing, board-changing actions are funnelled
 * through {@link #executeOrDefer(Runnable)}: they run immediately when idle, or are queued behind
 * the in-flight event animation otherwise, keeping the visible board consistent.
 */
public class ViewGameController implements UpdateHandler, MessageVisitor {

    private ServerAdapter serverAdapter;
    private String localPlayerNickname;

    @FXML private StackPane rootPane;
    @FXML private VBox mainBoardArea;

    @FXML private HBox upperBuildingsBox;
    @FXML private HBox lowerBuildingsBox;
    @FXML private HBox upperTribeBox;
    @FXML private HBox lowerTribeBox;
    @FXML private HBox centerTilesBox;

    @FXML private HBox playersStatsContainer;

    @FXML private VBox gameInfoBox;
    @FXML private Label currentEraLabel;
    @FXML private Label currentRoundLabel;
    @FXML private Label currentPlayerLabel;

    @FXML private VBox quickStatsBox;

    @FXML private VBox customTooltip;
    @FXML private Label tooltipTextLabel;

    @FXML private VBox reconnectingOverlay;
    @FXML private VBox gameAbortedOverlay;

    /** Set once a GameAbortedMessage arrives, so the server-lost overlay is not shown on top of it. */
    private volatile boolean gameAborted = false;

    private FadeTransition tooltipFade;

    private final Map<String, VBox> playerDashboards = new HashMap<>();
    private final Map<String, StackPane> orderTileSlots = new HashMap<>();
    private final Map<String, StackPane> offerTileSlots = new HashMap<>();

    private List<TotemPositionDTO> currentTotems = new ArrayList<>();
    private List<Integer> currentEventCardIds = new ArrayList<>();

    private CurrentPlayerDTO currentPlayerInfo = null;
    private State currentPhase = null;
    private int currentEraNum = 1;

    /** Lookup of card/tile descriptions used for hover tooltips. */
    private final CardInfoCatalog cardInfo = new CardInfoCatalog();

    private boolean localCanPickUpper = false;
    private boolean localCanPickLower = false;
    private List<Integer> localAffordableBuildingIds = new ArrayList<>();

    private final List<Timeline> activeGlowTimelines = new ArrayList<>();
    private final List<Node> glowingNodes = new ArrayList<>();

    // --- VARIABILI PER L'ANIMAZIONE CARTE E EVENTI ---
    private final Map<Integer, javafx.geometry.Bounds> pendingCardAnimations = new HashMap<>();
    private final Queue<Integer> eventAnimationQueue = new LinkedList<>();
    private final Queue<Runnable> deferredActionsQueue = new LinkedList<>();

    // --- SEMAFORI PER LE ANIMAZIONI ---
    private boolean isAnimatingEvent = false;
    private boolean isAnimatingBoard = false;

    /**
     * Sets the nickname of the player running this client; used to highlight the local player and
     * decide which board elements are interactive.
     *
     * @param nickname the local player's nickname
     */
    public void setLocalPlayerNickname(String nickname) {
        this.localPlayerNickname = nickname;
    }

    /**
     * Sets the server adapter, registers this controller as its update handler, sets up the board
     * sizing and tooltip fade on the JavaFX thread, and immediately requests a full game snapshot
     * to populate the board.
     *
     * @param serverAdapter the adapter wrapping the server connection
     */
    public void setServerAdapter(ServerAdapter serverAdapter) {
        this.serverAdapter = serverAdapter;
        this.serverAdapter.setUpdateHandler(this);

        Platform.runLater(() -> {
            mainBoardArea.minHeightProperty().bind(rootPane.heightProperty().subtract(100));
            tooltipFade = new FadeTransition(Duration.millis(150), customTooltip);
        });

        System.out.println("[GUI GAME] Controller caricato, richiedo la Board al server...");
        this.serverAdapter.sendRequest(new GameSyncRequest());
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
     * Shows a transient error toast over the board.
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

    // ==========================================
    // SISTEMA DI AZIONI DIFFERITE
    // ==========================================

    /**
     * Runs a board-changing action now if no event animation is in progress or queued; otherwise
     * defers it until the current event animation sequence finishes, so updates never alter the
     * board mid-animation.
     *
     * @param action the board update to run or defer
     */
    private void executeOrDefer(Runnable action) {
        if (isAnimatingEvent || !eventAnimationQueue.isEmpty()) {
            deferredActionsQueue.add(action);
        } else {
            action.run();
        }
    }

    // ==========================================
    // EFFETTI HOVER (ZOOM E TOOLTIP SMART)
    // ==========================================

    /**
     * Wires hover behaviour onto a node: on mouse-enter it lifts and zooms the node and shows the
     * custom tooltip with the given description following the cursor; on exit it restores the node
     * and fades the tooltip out.
     *
     * @param node        the node to make hoverable
     * @param description the text shown in the tooltip while hovering
     */
    private void applyHoverEffect(Node node, String description) {
        node.setOnMouseEntered(e -> {
            node.setViewOrder(-1);
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(150), node);
            scaleIn.setToX(1.15); scaleIn.setToY(1.15); scaleIn.play();

            tooltipTextLabel.setText(description);

            customTooltip.applyCss();
            customTooltip.layout();

            positionTooltip(e.getSceneX(), e.getSceneY());

            tooltipFade.stop(); tooltipFade.setToValue(1.0); tooltipFade.play();
        });

        node.setOnMouseMoved(e -> positionTooltip(e.getSceneX(), e.getSceneY()));

        node.setOnMouseExited(e -> {
            node.setViewOrder(0);
            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), node);
            scaleOut.setToX(1.0); scaleOut.setToY(1.0); scaleOut.play();

            tooltipFade.stop(); tooltipFade.setToValue(0.0); tooltipFade.play();
        });
    }

    /**
     * Places the custom tooltip near the cursor, flipping it to the left of the cursor when it
     * would otherwise overflow the right edge of the screen.
     *
     * @param mouseX the cursor x in scene coordinates
     * @param mouseY the cursor y in scene coordinates
     */
    private void positionTooltip(double mouseX, double mouseY) {
        double tooltipWidth = customTooltip.getWidth();
        if (tooltipWidth == 0) tooltipWidth = 150;

        double xOffset = 15;

        if (rootPane != null && (mouseX + xOffset + tooltipWidth > rootPane.getWidth())) {
            xOffset = -tooltipWidth - 15;
        }

        customTooltip.setTranslateX(mouseX + xOffset);
        customTooltip.setTranslateY(mouseY + 15);
    }

    // ==========================================
    // RICEZIONE MESSAGGI
    // ==========================================

    /**
     * Handles a full game snapshot by rebuilding the entire board: the four card rows, the central
     * tiles, every player's dashboard, the totems, and the era/round/current-player and phase
     * indicators. Also hides the reconnecting overlay. Runs on the JavaFX thread and is deferred
     * behind any in-flight animation.
     *
     * @param message the game snapshot message
     */
    @Override
    public void visit(GameSnapshotMessage message) {
        Platform.runLater(() -> {
            reconnectingOverlay.setVisible(false);
            executeOrDefer(() -> {

                GameSnapshotDTO snap = message.snapshot();
                this.currentEventCardIds = snap.board().eventCardIds();

                updateCardRow(upperBuildingsBox, snap.board().upperBuildingsIds());
                updateCardRow(lowerBuildingsBox, snap.board().lowerBuildingsIds());
                updateCardRow(upperTribeBox, snap.board().upperCharEvIds());
                updateCardRow(lowerTribeBox, snap.board().lowerCharEvIds());
                updateCenterTiles(snap.board().orderTileCount(), snap.board().offerTilesLetters());

                this.currentEraNum = snap.currentEra();

                for (PlayerStatsDTO stats : snap.playersStats()) {
                    updateOrAddPlayerDashboard(stats);
                }

                this.currentTotems = snap.totems();
                refreshTotems();

                this.currentPlayerInfo = snap.currentPlayer();
                if (snap.gameStateDTO() != null) this.currentPhase = snap.gameStateDTO().gameState();
                updateGamePhaseUI(snap.currentEra(), snap.currentRound());

                updateCurrentPlayerUI();
            });
        });
    }

    /**
     * Handles the end-of-round snapshot: detects whether the era changed, plays the board-shift
     * animation (lower rows replaced by the upper ones, then refilled), updates every dashboard,
     * and refreshes the era/round and current-player indicators. Runs on the JavaFX thread and is
     * deferred behind any in-flight animation.
     *
     * @param message the end-of-round update message
     */
    @Override
    public void visit(EndRoundUpdateMessage message) {
        Platform.runLater(() -> {
            executeOrDefer(() -> {
                EndRoundSnapshotDTO snap = message.snapshot();

                // Rileviamo se c'è stato un cambio d'era per le animazioni dei building
                boolean isEraChange = snap.currentEra() > this.currentEraNum;

                this.currentEraNum = snap.currentEra();
                this.currentEventCardIds = snap.board().eventCardIds();

                BoardDTO board = snap.board();
                animateEndRoundBoardShift(board, isEraChange);

                for (PlayerStatsDTO stats : snap.playersStats()) {
                    updateOrAddPlayerDashboard(stats);
                }

                updateGamePhaseUI(snap.currentEra(), snap.currentRound());

                this.currentPlayerInfo = snap.currentPlayer();
                updateCurrentPlayerUI();
            });
        });
    }

    /**
     * Handles a board update (e.g. after a card pick): refreshes the four card rows and central
     * tiles, then reapplies the interaction glows. Runs on the JavaFX thread and is deferred
     * behind any in-flight animation.
     *
     * @param message the board update message
     */
    @Override
    public void visit(BoardUpdatedMessage message) {
        Platform.runLater(() -> {
            executeOrDefer(() -> {
                BoardDTO board = message.board();
                this.currentEventCardIds = board.eventCardIds();

                updateCardRow(upperBuildingsBox, board.upperBuildingsIds());
                updateCardRow(lowerBuildingsBox, board.lowerBuildingsIds());
                updateCardRow(upperTribeBox, board.upperCharEvIds());
                updateCardRow(lowerTribeBox, board.lowerCharEvIds());
                updateCenterTiles(board.orderTileCount(), board.offerTilesLetters());
                applyGlowsBasedOnState();
            });
        });
    }

    /**
     * Handles an update to a single player's stats by refreshing (or creating) that player's
     * dashboard. Runs on the JavaFX thread and is deferred behind any in-flight animation.
     *
     * @param message the player-stats update message
     */
    @Override
    public void visit(PlayerStatsUpdateMessage message) {
        Platform.runLater(() -> {
            executeOrDefer(() -> updateOrAddPlayerDashboard(message.stats()));
        });
    }

    /**
     * Handles a totem-positions update: if exactly one totem changed slot, animates its move;
     * otherwise just re-renders all totems. Reapplies the interaction glows afterwards. Runs on the
     * JavaFX thread and is deferred behind any in-flight animation.
     *
     * @param message the totems-state message
     */
    @Override
    public void visit(TotemsStateMessage message) {
        Platform.runLater(() -> {
            executeOrDefer(() -> {
                List<TotemPositionDTO> newPositions = message.totems() != null ? message.totems() : new ArrayList<>();

                TotemPositionDTO movedTotem = null;
                TotemPositionDTO oldPosition = null;

                for (TotemPositionDTO newPos : newPositions) {
                    for (TotemPositionDTO oldPos : currentTotems) {
                        if (oldPos.totemColor().equals(newPos.totemColor()) &&
                                (!oldPos.locationType().equals(newPos.locationType()) || !oldPos.locationId().equals(newPos.locationId()))) {
                            movedTotem = newPos;
                            oldPosition = oldPos;
                            break;
                        }
                    }
                }

                if (movedTotem != null && oldPosition != null) {
                    final TotemPositionDTO finalOld = oldPosition;
                    final TotemPositionDTO finalMoved = movedTotem;
                    Platform.runLater(() -> animateTotemMove(finalOld, finalMoved, newPositions));
                } else {
                    this.currentTotems = newPositions;
                    refreshTotems();
                    applyGlowsBasedOnState();
                }
            });
        });
    }

    /**
     * Handles a change of the current player by updating the stored info and refreshing the
     * current-player UI. Runs on the JavaFX thread and is deferred behind any in-flight animation.
     *
     * @param message the current-player update message
     */
    @Override
    public void visit(CurrentPlayerUpdateMessage message) {
        Platform.runLater(() -> {
            executeOrDefer(() -> {
                this.currentPlayerInfo = message.currentPlayer();
                updateCurrentPlayerUI();
            });
        });
    }

    /**
     * Handles a change of the game phase by storing the new {@link State} and reapplying the
     * interaction glows accordingly. Runs on the JavaFX thread and is deferred behind any in-flight
     * animation.
     *
     * @param message the game-state update message
     */
    @Override
    public void visit(GameStateUpdateMessage message) {
        Platform.runLater(() -> {
            executeOrDefer(() -> {
                if (message.gameState() != null) {
                    this.currentPhase = message.gameState().gameState();
                }
                applyGlowsBasedOnState();
            });
        });
    }

    /**
     * Handles the resolution of an Event card by enqueuing its id for the spotlight animation and
     * kicking off the queue (animations play one at a time). Runs on the JavaFX thread.
     *
     * @param message the event-resolved message
     */
    @Override
    public void visit(EventResolvedMessage message) {
        Platform.runLater(() -> {
            eventAnimationQueue.add(message.cardId());
            playNextEventAnimation();
        });
    }

    /**
     * Handles the end of the game by loading the end-game screen, passing it the final rankings
     * and the local nickname, and replacing the window's scene with it. Runs on the JavaFX thread
     * and is deferred behind any in-flight animation.
     *
     * @param message the game-ended message carrying the final rankings
     */
    @Override
    public void visit(GameEndedMessage message) {
        Platform.runLater(() -> {
            executeOrDefer(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/GUI/fxml/EndGame.fxml"));
                    Parent endGameRoot = loader.load();

                    EndGameController controller = loader.getController();
                    controller.initData(message, localPlayerNickname);

                    Stage stage = (Stage) rootPane.getScene().getWindow();
                    Scene endGameScene = new Scene(endGameRoot);
                    stage.setScene(endGameScene);
                    stage.centerOnScreen();

                } catch (Exception e) {
                    System.err.println("[GUI ERROR] Impossibile caricare la schermata EndGame: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        });
    }

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
     * Closes the game window from the "Game aborted" overlay.
     */
    @FXML
    private void handleAbortClose() {
        Stage stage = (Stage) gameAbortedOverlay.getScene().getWindow();
        stage.close();
        Platform.exit();
    }

    // ==========================================
    // LOGICA DI UI CURRENT PLAYER E GLOW
    // ==========================================

    /**
     * Updates the current-player panel to show the active player's name in their totem colour,
     * plays a short pulse on the panel to draw attention, and reapplies the interaction glows.
     * No-op until the current player is known.
     */
    private void updateCurrentPlayerUI() {
        if (currentPlayerInfo == null) return;

        gameInfoBox.setVisible(true);
        String nick = currentPlayerInfo.nickname();
        String color = currentPlayerInfo.color();

        currentPlayerLabel.setText(nick);
        currentPlayerLabel.setStyle("-fx-text-fill: " + color + ";");
        gameInfoBox.setStyle("-fx-border-color: " + color + ";");

        ScaleTransition pulse = new ScaleTransition(Duration.millis(300), gameInfoBox);
        pulse.setFromX(1.0); pulse.setFromY(1.0);
        pulse.setToX(1.05); pulse.setToY(1.05);
        pulse.setCycleCount(2);
        pulse.setAutoReverse(true);
        pulse.play();

        applyGlowsBasedOnState();
    }

    /**
     * Updates the era and round labels in the game-info panel.
     *
     * @param era   the current era to display
     * @param round the current round to display
     */
    public void updateGamePhaseUI(int era, int round) {
        if (currentEraLabel != null && currentRoundLabel != null) {
            currentEraLabel.setText(String.valueOf(era));
            currentRoundLabel.setText(String.valueOf(round));
        }
    }

    private Button skipExtraPickButton;

    /**
     * Shows a "skip" button only while the local player is the current player during the
     * optional extra upper-row pick phase, letting them decline the bonus card and end the
     * round. The button is created lazily and parked in {@link #gameInfoBox}.
     */
    private void updateSkipExtraPickButton() {
        boolean show = currentPlayerInfo != null && currentPhase != null && localPlayerNickname != null
                && localPlayerNickname.equals(currentPlayerInfo.nickname())
                && currentPhase == State.EXTRA_UPPER_CARD_PICKING;

        if (skipExtraPickButton == null) {
            if (!show) return;
            skipExtraPickButton = new Button("Salta pesca bonus");
            skipExtraPickButton.getStyleClass().add("skip-bonus-button");
            skipExtraPickButton.setMaxWidth(Double.MAX_VALUE);
            skipExtraPickButton.setOnAction(e -> {
                if (serverAdapter != null && localPlayerNickname != null) {
                    serverAdapter.sendRequest(new SkipExtraPickRequest(localPlayerNickname));
                    skipExtraPickButton.setDisable(true);
                }
            });
            if (gameInfoBox != null) {
                VBox.setMargin(skipExtraPickButton, new javafx.geometry.Insets(8, 0, 0, 0));
                gameInfoBox.getChildren().add(skipExtraPickButton);
            }
        }

        skipExtraPickButton.setVisible(show);
        skipExtraPickButton.setManaged(show);
        if (show) {
            skipExtraPickButton.setDisable(false);
        }
    }

    /**
     * Recomputes which board elements pulse with a glow, based on the current phase and whether it
     * is the local player's turn: free offer tiles during totem placement, the upper-row cards
     * during the extra pick, and the affordable/eligible cards in the rows the player may still
     * pick from during card picking. Clears all existing glows first and does nothing while a board
     * animation is running.
     */
    private void applyGlowsBasedOnState() {
        updateSkipExtraPickButton();
        clearAllGlows();

        if (isAnimatingBoard) return;
        if (currentPlayerInfo == null || localPlayerNickname == null || currentPhase == null) return;

        if (localPlayerNickname.equals(currentPlayerInfo.nickname())) {
            if (currentPhase == State.TOTEM_PLACEMENT) {
                Set<String> occupiedOfferTiles = new HashSet<>();
                if (currentTotems != null) {
                    for (TotemPositionDTO totem : currentTotems) {
                        if ("OFFER".equals(totem.locationType())) {
                            occupiedOfferTiles.add(totem.locationId().toUpperCase());
                        }
                    }
                }
                for (Map.Entry<String, StackPane> entry : offerTileSlots.entrySet()) {
                    if (occupiedOfferTiles.contains(entry.getKey())) continue;
                    for (Node node : entry.getValue().getChildren()) {
                        if ("offer_tile".equals(node.getUserData())) addPulsingGlow(node);
                    }
                }
            }
            else if (currentPhase == State.EXTRA_UPPER_CARD_PICKING) {
                glowCardsInContainer(upperTribeBox, false);
                glowCardsInContainer(upperBuildingsBox, true);
            }
            else if (currentPhase == State.CARD_PICKING) {
                if (localCanPickUpper) {
                    glowCardsInContainer(upperTribeBox, false);
                    glowCardsInContainer(upperBuildingsBox, true);
                }
                if (localCanPickLower) {
                    glowCardsInContainer(lowerTribeBox, false);
                    glowCardsInContainer(lowerBuildingsBox, true);
                }
            }
        }
    }

    /**
     * Adds a pulsing glow to the pickable cards in a row, skipping Event cards and — for building
     * rows — buildings the local player cannot currently afford.
     *
     * @param container           the card row to scan
     * @param isBuildingContainer whether this is a building row (enables the affordability filter)
     */
    private void glowCardsInContainer(HBox container, boolean isBuildingContainer) {
        List<Node> children = new ArrayList<>(container.getChildren());

        for (Node slotNode : children) {
            String slotData = slotNode.getUserData() != null ? slotNode.getUserData().toString() : "";

            if (slotData.startsWith("card_slot_")) {
                int cardId = Integer.parseInt(slotData.split("_")[2]);

                if (currentEventCardIds != null && currentEventCardIds.contains(cardId)) {
                    continue;
                }

                if (isBuildingContainer && localAffordableBuildingIds != null && !localAffordableBuildingIds.contains(cardId)) {
                    continue;
                }

                StackPane slot = (StackPane) slotNode;
                if (!slot.getChildren().isEmpty()) {
                    Node cardNode = slot.getChildren().get(0);
                    if ("game_card".equals(cardNode.getUserData())) {
                        addPulsingGlow(cardNode);
                    }
                }
            }
        }
    }

    /**
     * Applies a golden, infinitely pulsing drop-shadow glow to a node, brings it to the front and
     * gives it a hand cursor to signal it is clickable. The glow's timeline and node are tracked so
     * {@link #clearAllGlows()} can later remove them.
     *
     * @param node the node to highlight as interactive
     */
    private void addPulsingGlow(Node node) {
        node.setViewOrder(-1.0);
        node.setCursor(javafx.scene.Cursor.HAND);

        DropShadow glow = new DropShadow();
        glow.setColor(Color.rgb(255, 215, 0, 0.9));
        glow.setRadius(5); glow.setSpread(0.6);
        node.setEffect(glow);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(glow.radiusProperty(), 5)),
                new KeyFrame(Duration.millis(800), new KeyValue(glow.radiusProperty(), 30))
        );
        timeline.setAutoReverse(true);
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        activeGlowTimelines.add(timeline);
        glowingNodes.add(node);
    }

    /**
     * Stops every active glow animation and reverts the highlighted nodes: cards get their default
     * drop shadow back, other nodes have their effect cleared, and the cursor and stacking order
     * are reset.
     */
    private void clearAllGlows() {
        for (Timeline t : activeGlowTimelines) t.stop();
        activeGlowTimelines.clear();

        for (Node n : glowingNodes) {
            if ("game_card".equals(n.getUserData())) {
                DropShadow defaultShadow = new DropShadow();
                defaultShadow.setRadius(8); defaultShadow.setOffsetY(3); defaultShadow.setColor(Color.rgb(0,0,0,0.8));
                n.setEffect(defaultShadow);
            } else {
                n.setEffect(null);
            }
            n.setCursor(javafx.scene.Cursor.DEFAULT);
            n.setViewOrder(0.0);
        }
        glowingNodes.clear();
    }

    // ==========================================
    // GESTIONE BOARD CENTRALE
    // ==========================================

    /**
     * Rebuilds the central area: the era deck image, the order-tile column (with one positioned,
     * percentage-anchored slot per remaining order tile) and the row of offer tiles (each clickable
     * for totem placement and carrying a hover tooltip). Re-renders the totems afterwards.
     *
     * @param orderTileCount the number of order tiles still on the board
     * @param offerLetters   the letters of the offer tiles to display, in order
     */
    private void updateCenterTiles(int orderTileCount, List<String> offerLetters) {
        centerTilesBox.getChildren().clear();
        orderTileSlots.clear(); offerTileSlots.clear();
        centerTilesBox.setSpacing(40);

        String deckPath = "/view/GUI/images/cards_back/era" + this.currentEraNum + "_back.png";
        try {
            var stream = getClass().getResourceAsStream(deckPath);
            if (stream != null) {
                ImageView deckView = new ImageView(new Image(stream));
                deckView.setFitHeight(160);
                deckView.setPreserveRatio(true);

                StackPane deckSlot = new StackPane(deckView);
                deckSlot.setAlignment(Pos.CENTER);
                deckSlot.setUserData("deck_slot");

                DropShadow shadow = new DropShadow();
                shadow.setRadius(8); shadow.setOffsetY(3); shadow.setColor(Color.rgb(0,0,0,0.8));
                deckSlot.setEffect(shadow);

                applyHoverEffect(deckSlot, cardInfo.describeDeck(this.currentEraNum));
                centerTilesBox.getChildren().add(deckSlot);
            }
        } catch (Exception ignored) {}

        String orderPath = "/view/GUI/images/tiles/order_" + orderTileCount + ".png";
        ImageView orderView = createTileImage(orderPath);

        if (orderView != null) {
            StackPane orderStack = new StackPane(orderView);
            Pane coordinateLayer = new Pane();
            final double originalWidth = 204.0; final double originalHeight = 315.0;

            for (int i = 1; i <= orderTileCount; i++) {
                StackPane slot = new StackPane();
                slot.setPrefSize(60, 60); slot.setAlignment(Pos.CENTER);

                double pixelY = 75.0 + ((i - 1) * 52.0);
                final double percX = 102.0 / originalWidth;
                final double percY = pixelY / originalHeight;

                coordinateLayer.widthProperty().addListener((obs, oldW, newW) -> slot.setLayoutX((newW.doubleValue() * percX) - 30));
                coordinateLayer.heightProperty().addListener((obs, oldH, newH) -> slot.setLayoutY((newH.doubleValue() * percY) - 30));

                orderTileSlots.put(String.valueOf(i), slot);
                coordinateLayer.getChildren().add(slot);
            }

            orderStack.getChildren().add(coordinateLayer);
            applyHoverEffect(orderStack, cardInfo.describeOrderTile());
            HBox orderWrapper = new HBox(orderStack);
            orderWrapper.getStyleClass().add("card-row-background");
            orderWrapper.setAlignment(Pos.CENTER);
            centerTilesBox.getChildren().add(orderWrapper);
        }

        if (offerLetters != null && !offerLetters.isEmpty()) {
            HBox offerTilesWrapper = new HBox();
            offerTilesWrapper.setSpacing(0);
            offerTilesWrapper.getStyleClass().add("card-row-background");
            offerTilesWrapper.setAlignment(Pos.CENTER);

            for (String letter : offerLetters) {
                String offerPath = "/view/GUI/images/tiles/offer_" + letter.toUpperCase() + ".png";
                ImageView offerView = createTileImage(offerPath);

                if (offerView != null) {
                    offerView.setUserData("offer_tile");
                    StackPane offerStack = new StackPane(offerView);
                    offerStack.setAlignment(Pos.CENTER);

                    offerStack.setOnMouseClicked(event -> handleOfferTileClick(letter));
                    applyHoverEffect(offerStack, cardInfo.describeOfferTile(letter));

                    offerTileSlots.put(letter.toUpperCase(), offerStack);
                    offerTilesWrapper.getChildren().add(offerStack);
                }
            }
            centerTilesBox.getChildren().add(offerTilesWrapper);
        }
        refreshTotems();
    }

    /**
     * Handles a click on an offer tile: if it is the local player's turn during the totem-placement
     * phase, sends a {@link TotemMoveRequest} for that tile and refreshes the glows. Ignored
     * otherwise.
     *
     * @param letter the letter of the clicked offer tile
     */
    private void handleOfferTileClick(String letter) {
        if (currentPlayerInfo != null && currentPhase != null && localPlayerNickname != null) {
            if (localPlayerNickname.equals(currentPlayerInfo.nickname()) && currentPhase == State.TOTEM_PLACEMENT) {
                serverAdapter.sendRequest(new TotemMoveRequest(localPlayerNickname, letter));
                applyGlowsBasedOnState();
            }
        }
    }

    /**
     * Loads a tile image from the classpath into a fixed-height, ratio-preserving {@link ImageView}.
     *
     * @param imagePath the classpath resource path of the tile image
     * @return the image view, or {@code null} if the resource is missing or cannot be loaded
     */
    private ImageView createTileImage(String imagePath) {
        try {
            var stream = getClass().getResourceAsStream(imagePath);
            if (stream == null) return null;
            ImageView tileView = new ImageView(new Image(stream));
            tileView.setFitHeight(180); tileView.setPreserveRatio(true);
            return tileView;
        } catch (Exception e) { return null; }
    }

    /**
     * Re-renders a card row from the given ids, hiding the row entirely when empty. Cards that were
     * already present and merely moved slide to their new position, while newly added cards fade in,
     * giving a smooth transition between board states.
     *
     * @param container the row to rebuild
     * @param cardIds   the ids of the cards the row should now show (may be {@code null}/empty)
     */
    private void updateCardRow(HBox container, List<Integer> cardIds) {
        if (cardIds == null || cardIds.isEmpty()) {
            container.setVisible(false);
            container.setManaged(false);
            container.getChildren().clear();
            return;
        }

        container.setVisible(true);
        container.setManaged(true);

        Map<Integer, Double> oldXPositions = new HashMap<>();
        for (Node slot : container.getChildren()) {
            if (slot.getUserData() != null && slot.getUserData().toString().startsWith("card_slot_")) {
                int id = Integer.parseInt(slot.getUserData().toString().split("_")[2]);
                oldXPositions.put(id, slot.localToScene(slot.getBoundsInLocal()).getMinX());
            }
        }

        container.getChildren().clear();

        for (Integer cardId : cardIds) {
            StackPane slot = createCardSlotNode(cardId);
            if (slot != null) container.getChildren().add(slot);
        }

        container.applyCss();
        container.layout();

        for (Node slot : container.getChildren()) {
            if (slot.getUserData() != null && slot.getUserData().toString().startsWith("card_slot_")) {
                int id = Integer.parseInt(slot.getUserData().toString().split("_")[2]);

                if (oldXPositions.containsKey(id)) {
                    double oldX = oldXPositions.get(id);
                    double newX = slot.localToScene(slot.getBoundsInLocal()).getMinX();

                    if (Math.abs(oldX - newX) > 1.0) {
                        slot.setTranslateX(oldX - newX);
                        TranslateTransition slide = new TranslateTransition(Duration.millis(500), slot);
                        slide.setToX(0);
                        slide.setInterpolator(Interpolator.EASE_OUT);
                        slide.play();
                    }
                } else {
                    slot.setOpacity(0);
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(600), slot);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                }
            }
        }
    }

    /**
     * Handles a click on a board card: if it is the local player's turn during a card-picking
     * phase, records the card's current on-screen bounds (so the pick can be animated once the
     * stats update arrives) and sends a {@link CardPickRequest}. Ignored otherwise.
     *
     * @param cardId      the id of the clicked card
     * @param clickedCard the clicked card node, used to capture its start position
     */
    private void handleCardClick(int cardId, Node clickedCard) {
        if (currentPlayerInfo != null && currentPhase != null && localPlayerNickname != null) {
            if (localPlayerNickname.equals(currentPlayerInfo.nickname())
                    && (currentPhase == State.CARD_PICKING || currentPhase == State.EXTRA_UPPER_CARD_PICKING)) {
                pendingCardAnimations.put(cardId, clickedCard.localToScene(clickedCard.getBoundsInLocal()));
                serverAdapter.sendRequest(new CardPickRequest(localPlayerNickname, cardId));
            }
        }
    }

    // ==========================================
    // LOGICA DASHBOARD GIOCATORI E PANNELLI
    // ==========================================

    /**
     * Refreshes a player's dashboard from their stats, creating it the first time the player is
     * seen. Updates the header (name, food, points), the per-character-type and owned-buildings
     * rows, and the bottom counters (sales, shaman stars, inventions). For the local player it also
     * caches the pick/affordability flags, updates the quick-stats panel and refreshes the glows,
     * and triggers the fly-to-dashboard animation for any card just picked.
     *
     * @param stats the player's current stats
     */
    private void updateOrAddPlayerDashboard(PlayerStatsDTO stats) {
        if (stats.nickname().equals(localPlayerNickname)) {
            this.localCanPickUpper = stats.canPickUpper();
            this.localCanPickLower = stats.canPickLower();
            this.localAffordableBuildingIds = stats.affordableBuildingIds();

            updateQuickStatsPanel(stats);
            applyGlowsBasedOnState();
        }

        VBox dashboard = playerDashboards.get(stats.nickname());

        if (dashboard == null) {
            dashboard = new VBox(15);
            dashboard.setAlignment(Pos.TOP_CENTER);
            dashboard.getStyleClass().add("player-dashboard");
            dashboard.setStyle("-fx-border-color: " + stats.color() + ";");

            HBox headerBox = new HBox();
            headerBox.setAlignment(Pos.CENTER_LEFT);

            Label nameLabel = new Label();
            nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333333;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox topStatsBox = new HBox(15);
            topStatsBox.setAlignment(Pos.CENTER_RIGHT);

            headerBox.getChildren().addAll(nameLabel, spacer, topStatsBox);
            dashboard.getChildren().add(headerBox);

            dashboard.getChildren().add(createEmptyCategoryRow("collector", stats.nickname()));
            dashboard.getChildren().add(createEmptyCategoryRow("hunter", stats.nickname()));
            dashboard.getChildren().add(createEmptyCategoryRow("builder", stats.nickname()));
            dashboard.getChildren().add(createEmptyCategoryRow("shaman", stats.nickname()));
            dashboard.getChildren().add(createEmptyCategoryRow("artist", stats.nickname()));
            dashboard.getChildren().add(createEmptyCategoryRow("inventor", stats.nickname()));

            dashboard.getChildren().add(createEmptyCategoryRow("buildings", stats.nickname()));

            HBox bottomStats = new HBox(20);
            bottomStats.setAlignment(Pos.CENTER);
            dashboard.getChildren().add(bottomStats);

            playerDashboards.put(stats.nickname(), dashboard);
            playersStatsContainer.getChildren().add(dashboard);
        }

        HBox headerBox = (HBox) dashboard.getChildren().get(0);
        Label nameLabel = (Label) headerBox.getChildren().get(0);
        nameLabel.setText(stats.nickname());
        nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + stats.color() + ";");

        HBox topStatsBox = (HBox) headerBox.getChildren().get(2);
        topStatsBox.getChildren().clear();
        topStatsBox.getChildren().addAll(
                createStatIcon("food", stats.food(), "Cibo posseduto"),
                createStatIcon("points", stats.points(), "Punti Prestigio")
        );

        refreshCategoryRow((HBox) dashboard.getChildren().get(1), "collector", stats.collectorCards());
        refreshCategoryRow((HBox) dashboard.getChildren().get(2), "hunter", stats.hunterCards());
        refreshCategoryRow((HBox) dashboard.getChildren().get(3), "builder", stats.builderCards());
        refreshCategoryRow((HBox) dashboard.getChildren().get(4), "shaman", stats.shamanCards());
        refreshCategoryRow((HBox) dashboard.getChildren().get(5), "artist", stats.artistCards());
        refreshCategoryRow((HBox) dashboard.getChildren().get(6), "inventor", stats.inventorCards());

        refreshCategoryRow((HBox) dashboard.getChildren().get(7), "buildings", stats.ownedBuildingCards());

        HBox bottomStats = (HBox) dashboard.getChildren().get(8);
        bottomStats.getChildren().clear();
        bottomStats.getChildren().addAll(
                createStatIcon("feedingSale", stats.feedingSale(), "Sconto sfamare tribù"),
                createStatIcon("buildingSale", stats.buildingSale(), "Sconto edifici"),
                createStatIcon("shamanStars", stats.shamanStars(), "Stelle sciamano"),
                createStatIcon("inventions", stats.inventionCount(), "Invenzioni")
        );

        if (!pendingCardAnimations.isEmpty() && stats.nickname().equals(localPlayerNickname)) {
            List<Integer> animatedCardsThisTurn = new ArrayList<>();

            for (Integer pendingCardId : pendingCardAnimations.keySet()) {
                String category = findCategoryOfCard(pendingCardId, stats);
                if (category != null) {
                    HBox targetRow = findDashboardRow(localPlayerNickname, category);
                    javafx.geometry.Bounds boundsToAnim = pendingCardAnimations.get(pendingCardId);

                    animatedCardsThisTurn.add(pendingCardId);
                    Platform.runLater(() -> animateCardPickFromBounds(pendingCardId, boundsToAnim, targetRow));
                }
            }

            for (Integer id : animatedCardsThisTurn) {
                pendingCardAnimations.remove(id);
            }
        }
    }

    /**
     * Refreshes the local player's quick-stats side panel: two columns of stat icons covering food,
     * points and the per-type character counts on the left, and the sales, shaman stars and
     * inventions on the right. Preserves the panel's first child (its title) and rebuilds the rest.
     *
     * @param stats the local player's current stats
     */
    private void updateQuickStatsPanel(PlayerStatsDTO stats) {
        if (quickStatsBox == null) return;

        quickStatsBox.setVisible(true);

        if (quickStatsBox.getChildren().size() > 1) {
            quickStatsBox.getChildren().subList(1, quickStatsBox.getChildren().size()).clear();
        }

        VBox leftCol = new VBox(10);
        leftCol.setAlignment(Pos.TOP_LEFT);
        leftCol.getChildren().addAll(
                createStatIcon("food", stats.food(), "Cibo posseduto"),
                createStatIcon("points", stats.points(), "Punti Prestigio"),
                createStatIcon("collector", stats.collectorCards().size(), "Numero Raccoglitori"),
                createStatIcon("hunter", stats.hunterCards().size(), "Numero Cacciatori"),
                createStatIcon("builder", stats.builderCards().size(), "Numero Costruttori"),
                createStatIcon("shaman", stats.shamanCards().size(), "Numero Sciamani"),
                createStatIcon("artist", stats.artistCards().size(), "Numero Artisti"),
                createStatIcon("inventor", stats.inventorCards().size(), "Numero Inventori")
        );

        VBox rightCol = new VBox(10);
        rightCol.setAlignment(Pos.TOP_LEFT);
        rightCol.getChildren().addAll(
                createStatIcon("feedingSale", stats.feedingSale(), "Sconto sfamare tribù"),
                createStatIcon("buildingSale", stats.buildingSale(), "Sconto Costruzione Edifici"),
                createStatIcon("shamanStars", stats.shamanStars(), "Stelle Sciamano accumulate"),
                createStatIcon("inventions", stats.inventionCount(), "Invenzioni completate")
        );

        HBox columnsContainer = new HBox(20);
        columnsContainer.setAlignment(Pos.TOP_CENTER);
        columnsContainer.getChildren().addAll(leftCol, rightCol);

        quickStatsBox.getChildren().add(columnsContainer);
    }

    /**
     * Creates an empty dashboard row for one card category, tagged with a {@code userData} key that
     * encodes the player and category so it can be located later (e.g. for animations).
     *
     * @param type       the category key (e.g. {@code "collector"}, {@code "buildings"})
     * @param playerNick the owning player's nickname
     * @return the empty row node
     */
    private HBox createEmptyCategoryRow(String type, String playerNick) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMinHeight(130);
        row.setUserData("dashboard_row_" + playerNick + "_" + type);
        return row;
    }

    /**
     * Rebuilds one dashboard category row to show a count icon followed by a mini image of each
     * card in the category.
     *
     * @param row     the row to refresh
     * @param type    the category key (drives the icon and tooltip)
     * @param cardIds the ids of the cards to display in this category
     */
    private void refreshCategoryRow(HBox row, String type, List<Integer> cardIds) {
        row.getChildren().clear();

        String tooltipText = type.equals("buildings") ? "Edifici costruiti" : "Carte di tipo " + type;
        HBox typeInfo = createStatIcon(type, cardIds.size(), tooltipText);
        typeInfo.setMinWidth(60);

        HBox cardsList = new HBox(5);
        cardsList.setAlignment(Pos.CENTER_LEFT);

        for (Integer id : cardIds) {
            String path = "/view/GUI/images/cards/card_" + id + ".png";
            try {
                var stream = getClass().getResourceAsStream(path);
                if (stream == null) continue;

                ImageView miniCard = new ImageView(new Image(stream));
                miniCard.setFitHeight(120);
                miniCard.setPreserveRatio(true);
                miniCard.getStyleClass().add("mini-card");

                DropShadow shadow = new DropShadow();
                shadow.setRadius(5); shadow.setOffsetY(2); shadow.setColor(Color.rgb(0,0,0,0.6));
                miniCard.setEffect(shadow);

                cardsList.getChildren().add(miniCard);
            } catch (Exception ignored) {}
        }

        row.getChildren().addAll(typeInfo, cardsList);
    }

    /**
     * Builds a small "icon + value" widget for a stat, attaching a hover tooltip when text is
     * given. Returns an empty container if the icon image cannot be loaded.
     *
     * @param iconName    the stat icon name (selects the image under {@code images/stats})
     * @param value       the numeric value shown next to the icon
     * @param tooltipText the hover tooltip text, or {@code null}/empty for none
     * @return the assembled stat widget
     */
    private HBox createStatIcon(String iconName, int value, String tooltipText) {
        HBox container = new HBox(5);
        container.setAlignment(Pos.CENTER_LEFT);

        String path = "/view/GUI/images/stats/" + iconName + ".png";
        try {
            var stream = getClass().getResourceAsStream(path);
            if (stream == null) return container;
            ImageView icon = new ImageView(new Image(stream));
            icon.setFitHeight(24); icon.setPreserveRatio(true);
            icon.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 5, 0, 0, 2);");

            Label valLabel = new Label(String.valueOf(value));
            valLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");
            container.getChildren().addAll(icon, valLabel);

            if (tooltipText != null && !tooltipText.isEmpty()) {
                applyHoverEffect(container, tooltipText);
            }
        } catch (Exception ignored) {}

        return container;
    }

    /**
     * Determines which dashboard category a card belongs to for the given player, checking each
     * character type and then the owned buildings.
     *
     * @param cardId the card id to classify
     * @param stats  the player's stats to search
     * @return the category key, or {@code null} if the card is not in any of the player's lists
     */
    private String findCategoryOfCard(int cardId, PlayerStatsDTO stats) {
        if (stats.collectorCards().contains(cardId)) return "collector";
        if (stats.hunterCards().contains(cardId)) return "hunter";
        if (stats.builderCards().contains(cardId)) return "builder";
        if (stats.shamanCards().contains(cardId)) return "shaman";
        if (stats.artistCards().contains(cardId)) return "artist";
        if (stats.inventorCards().contains(cardId)) return "inventor";

        if (stats.ownedBuildingCards().contains(cardId)) return "buildings";
        return null;
    }

    /**
     * Finds a player's dashboard row for a given category by matching the row's {@code userData}
     * tag.
     *
     * @param nickname     the owning player's nickname
     * @param categoryType the category key to look for
     * @return the matching row, or {@code null} if the dashboard or row does not exist
     */
    private HBox findDashboardRow(String nickname, String categoryType) {
        VBox dashboard = playerDashboards.get(nickname);
        if (dashboard == null) return null;

        String targetUserData = "dashboard_row_" + nickname + "_" + categoryType;
        for (Node row : dashboard.getChildren()) {
            if (targetUserData.equals(row.getUserData())) {
                return (HBox) row;
            }
        }
        return null;
    }

    /**
     * Animates a picked card flying from its old board position to the player's dashboard row: a
     * temporary card image is added over the root pane and slides/shrinks toward the target row,
     * then removes itself when done. No-op if the start bounds or target row are missing.
     *
     * @param cardId      the id of the picked card (selects the image)
     * @param startBounds the card's on-screen bounds when it was picked
     * @param targetRow   the dashboard row the card flies toward
     */
    private void animateCardPickFromBounds(int cardId, javafx.geometry.Bounds startBounds, HBox targetRow) {
        if (startBounds == null || targetRow == null) return;

        String imagePath = "/view/GUI/images/cards/card_" + cardId + ".png";
        ImageView flyer = null;
        try {
            var stream = getClass().getResourceAsStream(imagePath);
            if (stream != null) {
                flyer = new ImageView(new Image(stream));
                flyer.setFitHeight(160); flyer.setPreserveRatio(true);

                DropShadow shadow = new DropShadow();
                shadow.setRadius(8); shadow.setOffsetY(3); shadow.setColor(Color.rgb(0,0,0,0.8));
                flyer.setEffect(shadow);
            }
        } catch (Exception e) { return; }

        if (flyer == null) return;

        StackPane.setAlignment(flyer, Pos.TOP_LEFT);
        flyer.setMouseTransparent(true);

        var endBounds = targetRow.localToScene(targetRow.getBoundsInLocal());
        var rootBounds = rootPane.localToScene(rootPane.getBoundsInLocal());

        double startX = startBounds.getMinX() - rootBounds.getMinX();
        double startY = startBounds.getMinY() - rootBounds.getMinY();

        double endX = endBounds.getMinX() - rootBounds.getMinX() + 60;
        double endY = endBounds.getMinY() - rootBounds.getMinY() + (endBounds.getHeight() / 2) - 60;

        flyer.setTranslateX(startX);
        flyer.setTranslateY(startY);

        rootPane.getChildren().add(flyer);

        TranslateTransition translate = new TranslateTransition(Duration.millis(700), flyer);
        translate.setToX(endX); translate.setToY(endY);
        translate.setInterpolator(Interpolator.EASE_BOTH);

        ScaleTransition scale = new ScaleTransition(Duration.millis(700), flyer);
        scale.setToX(0.75); scale.setToY(0.75);
        scale.setInterpolator(Interpolator.EASE_BOTH);

        ParallelTransition parallelTransition = new ParallelTransition(translate, scale);

        ImageView finalFlyer = flyer;
        parallelTransition.setOnFinished(event -> rootPane.getChildren().remove(finalFlyer));

        parallelTransition.play();
    }

    // ==========================================
    // ANIMAZIONE E GESTIONE TOTEM
    // ==========================================

    /**
     * Animates a totem flying from its previous slot to its new one: it removes the totem from the
     * start slot, slides a temporary totem image across the root pane, and on completion commits the
     * final positions and re-renders. If either slot can't be found it skips the animation and just
     * renders the final state.
     *
     * @param from       the totem's previous position
     * @param to         the totem's new position
     * @param finalState the full set of totem positions to apply once the move completes
     */
    private void animateTotemMove(TotemPositionDTO from, TotemPositionDTO to, List<TotemPositionDTO> finalState) {
        StackPane startSlot = findSlot(from.locationType(), from.locationId());
        StackPane endSlot = findSlot(to.locationType(), to.locationId());

        if (startSlot == null || endSlot == null) {
            this.currentTotems = finalState; refreshTotems(); applyGlowsBasedOnState(); return;
        }

        ImageView flyer = createTotemImage(to.totemColor());
        if (flyer == null) return;

        flyer.setViewOrder(-10.0);

        StackPane.setAlignment(flyer, Pos.TOP_LEFT);
        flyer.setMouseTransparent(true);

        var startBounds = startSlot.localToScene(startSlot.getBoundsInLocal());
        var endBounds = endSlot.localToScene(endSlot.getBoundsInLocal());
        var rootBounds = rootPane.localToScene(rootPane.getBoundsInLocal());

        double startOffsetY = "OFFER".equals(from.locationType()) ? -55 : 0;
        double endOffsetY = "OFFER".equals(to.locationType()) ? -55 : 0;

        double startX = startBounds.getMinX() - rootBounds.getMinX() + (startBounds.getWidth() / 2) - 30;
        double startY = startBounds.getMinY() - rootBounds.getMinY() + (startBounds.getHeight() / 2) - 30 + startOffsetY;

        double endX = endBounds.getMinX() - rootBounds.getMinX() + (endBounds.getWidth() / 2) - 30;
        double endY = endBounds.getMinY() - rootBounds.getMinY() + (endBounds.getHeight() / 2) - 30 + endOffsetY;

        flyer.setTranslateX(startX); flyer.setTranslateY(startY);
        rootPane.getChildren().add(flyer);
        startSlot.getChildren().removeIf(node -> "totem".equals(node.getUserData()));

        TranslateTransition transition = new TranslateTransition(Duration.millis(600), flyer);
        transition.setToX(endX); transition.setToY(endY);
        transition.setInterpolator(Interpolator.EASE_BOTH);

        transition.setOnFinished(event -> {
            rootPane.getChildren().remove(flyer);
            this.currentTotems = finalState;
            refreshTotems();
            applyGlowsBasedOnState();
        });

        transition.play();
    }

    /**
     * Resolves a tile slot from a location type and id. For {@code "ORDER"} it looks up the order
     * slot by id, retrying with {@code id + 1} to tolerate a 0- vs 1-based mismatch; for
     * {@code "OFFER"} it looks up the offer slot by (upper-cased) letter.
     *
     * @param type the location type ({@code "ORDER"} or {@code "OFFER"})
     * @param id   the slot id (order-tile id or offer-tile letter)
     * @return the matching slot, or {@code null} if none is found
     */
    private StackPane findSlot(String type, String id) {
        if ("ORDER".equals(type)) {
            StackPane s = orderTileSlots.get(id);
            if (s == null) {
                try {
                    int parsedId = Integer.parseInt(id);
                    return orderTileSlots.get(String.valueOf(parsedId + 1));
                } catch (Exception e) { return null; }
            }
            return s;
        } else {
            return offerTileSlots.get(id.toUpperCase());
        }
    }

    /**
     * Re-renders all totems from {@link #currentTotems}: clears any existing totem images from the
     * order and offer slots, then adds a totem image to each slot, raising offer-tile totems so
     * they sit above the tile. No-op when there are no totems to show.
     */
    private void refreshTotems() {
        if (currentTotems == null || currentTotems.isEmpty()) return;

        for (StackPane slot : orderTileSlots.values()) slot.getChildren().removeIf(node -> "totem".equals(node.getUserData()));
        for (StackPane slot : offerTileSlots.values()) slot.getChildren().removeIf(node -> "totem".equals(node.getUserData()));

        for (TotemPositionDTO pos : currentTotems) {
            StackPane targetSlot = null;
            if ("ORDER".equals(pos.locationType())) {
                targetSlot = orderTileSlots.get(pos.locationId());
                if (targetSlot == null) {
                    try {
                        int parsedId = Integer.parseInt(pos.locationId());
                        targetSlot = orderTileSlots.get(String.valueOf(parsedId + 1));
                    } catch (NumberFormatException ignored) {}
                }
            } else if ("OFFER".equals(pos.locationType())) {
                targetSlot = offerTileSlots.get(pos.locationId().toUpperCase());
            }

            if (targetSlot != null) {
                ImageView totemImg = createTotemImage(pos.totemColor());
                if (totemImg != null) {
                    if ("OFFER".equals(pos.locationType())) totemImg.setTranslateY(-55);
                    targetSlot.getChildren().add(totemImg);
                }
            }
        }
    }

    /**
     * Loads the 3D totem image for a colour into an {@link ImageView}, tagged with {@code "totem"}
     * userData and styled with a drop shadow so it can be recognised and layered on the tiles.
     *
     * @param color the totem colour
     * @return the totem image view, or {@code null} if the image is missing or cannot be loaded
     */
    private ImageView createTotemImage(String color) {
        String path = "/view/GUI/images/totem3D/" + color.toLowerCase() + "_3D.png";
        try {
            var stream = getClass().getResourceAsStream(path);
            if (stream == null) return null;
            ImageView totemView = new ImageView(new Image(stream));
            totemView.setFitHeight(60); totemView.setPreserveRatio(true);
            totemView.setUserData("totem");
            totemView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 5, 0, 0, 2);");
            totemView.setViewOrder(-5.0);
            return totemView;
        } catch (Exception e) { return null; }
    }

    // ==========================================
    // LOGICA ANIMAZIONE FINE ROUND E CAMBIO ERA
    // ==========================================

    /**
     * Starts the end-of-round board transition by fading out the lower tribe row (and, on an era
     * change, the lower buildings too), then hands off to {@link #executeDrops(BoardDTO, boolean)}
     * to move and refill the rows. Sets the board-animation flag and clears glows for the duration.
     *
     * @param newBoard    the board state to settle into once the animation completes
     * @param isEraChange whether this round end is also an era change (animates the building rows too)
     */
    private void animateEndRoundBoardShift(BoardDTO newBoard, boolean isEraChange) {
        isAnimatingBoard = true;
        clearAllGlows();

        ParallelTransition fadeOutLower = new ParallelTransition();

        for (Node slot : lowerTribeBox.getChildren()) {
            FadeTransition ft = new FadeTransition(Duration.millis(400), slot);
            ft.setToValue(0);
            fadeOutLower.getChildren().add(ft);
        }

        // Se è fine era, sfumiamo anche i building inferiori
        if (isEraChange) {
            for (Node slot : lowerBuildingsBox.getChildren()) {
                FadeTransition ft = new FadeTransition(Duration.millis(400), slot);
                ft.setToValue(0);
                fadeOutLower.getChildren().add(ft);
            }
        }

        if (fadeOutLower.getChildren().isEmpty()) {
            executeDrops(newBoard, isEraChange);
        } else {
            fadeOutLower.setOnFinished(e -> executeDrops(newBoard, isEraChange));
            fadeOutLower.play();
        }
    }

    /**
     * Runs the staged end-of-round animation: the upper tribe cards drop into the lower row, then
     * (on an era change) the upper buildings drop into the lower building row, then fresh cards are
     * dealt from the deck into the upper row(s). A final step reconciles all rows and central tiles
     * with {@code newBoard} and clears the board-animation flag.
     *
     * @param newBoard    the authoritative board state to settle into
     * @param isEraChange whether the building rows participate in the shift and refill
     */
    private void executeDrops(BoardDTO newBoard, boolean isEraChange) {
        lowerTribeBox.getChildren().clear();
        lowerTribeBox.setVisible(true); lowerTribeBox.setManaged(true);

        if (isEraChange) {
            lowerBuildingsBox.getChildren().clear();
            lowerBuildingsBox.setVisible(true); lowerBuildingsBox.setManaged(true);
        }

        List<Node> upperTribeNodes = new ArrayList<>(upperTribeBox.getChildren());

        // STEP 1: Scendono le carte tribù
        dropCardsSequentially(upperTribeNodes, lowerTribeBox, 0, () -> {

            // Definiamo il blocco che farà scendere le nuove carte dal mazzo
            Runnable step3 = () -> {
                upperTribeBox.getChildren().clear();
                upperTribeBox.setVisible(true); upperTribeBox.setManaged(true);

                if (isEraChange) {
                    upperBuildingsBox.getChildren().clear();
                    upperBuildingsBox.setVisible(true); upperBuildingsBox.setManaged(true);
                }

                // STEP 3: Pescaggio nuove carte tribù
                dealCardsSequentially(newBoard.upperCharEvIds(), upperTribeBox, 0, () -> {

                    // Definiamo l'ultimo step
                    Runnable finishStep = () -> {
                        // Per sicurezza (ed eventuali acquisti avvenuti) aggiorniamo tutto con i dati freschi del DTO
                        updateCardRow(upperBuildingsBox, newBoard.upperBuildingsIds());
                        updateCardRow(lowerBuildingsBox, newBoard.lowerBuildingsIds());
                        updateCardRow(upperTribeBox, newBoard.upperCharEvIds());
                        updateCardRow(lowerTribeBox, newBoard.lowerCharEvIds());
                        updateCenterTiles(newBoard.orderTileCount(), newBoard.offerTilesLetters());

                        isAnimatingBoard = false;
                        applyGlowsBasedOnState();
                    };

                    // STEP 4: Se cambio era, pescaggio nuovi building. Altrimenti finish.
                    if (isEraChange) {
                        dealCardsSequentially(newBoard.upperBuildingsIds(), upperBuildingsBox, 0, finishStep);
                    } else {
                        finishStep.run();
                    }
                });
            };

            // STEP 2: Se cambio era, scendono gli edifici. Altrimenti passiamo allo step 3.
            if (isEraChange) {
                List<Node> upperBuildingNodes = new ArrayList<>(upperBuildingsBox.getChildren());
                dropCardsSequentially(upperBuildingNodes, lowerBuildingsBox, 0, step3);
            } else {
                step3.run();
            }
        });
    }

    /**
     * Recursively animates the upper-row cards dropping one after another into the lower row: for
     * each card it hides the upper slot, adds an invisible placeholder in the lower box, flies a
     * temporary card image from the old to the new position, reveals the placeholder, then proceeds
     * to the next card. Runs {@code onComplete} when all cards have dropped.
     *
     * @param upperSlots the upper-row slot nodes to drop, in order
     * @param lowerBox   the destination row
     * @param index      the index of the card to process in this step
     * @param onComplete callback invoked once every card has dropped (may be {@code null})
     */
    private void dropCardsSequentially(List<Node> upperSlots, HBox lowerBox, int index, Runnable onComplete) {
        if (index >= upperSlots.size()) {
            if (onComplete != null) onComplete.run();
            return;
        }

        Node upperSlot = upperSlots.get(index);

        int cardId = -1;
        if (upperSlot.getUserData() != null && upperSlot.getUserData().toString().startsWith("card_slot_")) {
            cardId = Integer.parseInt(upperSlot.getUserData().toString().split("_")[2]);
        }

        if (cardId == -1) {
            dropCardsSequentially(upperSlots, lowerBox, index + 1, onComplete);
            return;
        }

        upperSlot.setVisible(false);

        StackPane placeholder = createCardSlotNode(cardId);
        placeholder.setOpacity(0);
        lowerBox.getChildren().add(placeholder);

        lowerBox.applyCss();
        lowerBox.layout();

        ImageView flyer = null;
        try {
            var stream = getClass().getResourceAsStream("/view/GUI/images/cards/card_" + cardId + ".png");
            if (stream != null) {
                flyer = new ImageView(new Image(stream));
                flyer.setFitHeight(160); flyer.setPreserveRatio(true);
                DropShadow shadow = new DropShadow();
                shadow.setRadius(8); shadow.setOffsetY(3); shadow.setColor(Color.rgb(0,0,0,0.8));
                flyer.setEffect(shadow);
            }
        } catch (Exception ignored) {}

        if (flyer == null) {
            dropCardsSequentially(upperSlots, lowerBox, index + 1, onComplete); return;
        }

        flyer.setViewOrder(-20.0);
        StackPane.setAlignment(flyer, Pos.TOP_LEFT);
        flyer.setMouseTransparent(true);

        var startBounds = upperSlot.localToScene(upperSlot.getBoundsInLocal());
        var endBounds = placeholder.localToScene(placeholder.getBoundsInLocal());
        var rootBounds = rootPane.localToScene(rootPane.getBoundsInLocal());

        flyer.setTranslateX(startBounds.getMinX() - rootBounds.getMinX());
        flyer.setTranslateY(startBounds.getMinY() - rootBounds.getMinY());

        rootPane.getChildren().add(flyer);

        TranslateTransition drop = new TranslateTransition(Duration.millis(450), flyer);
        drop.setToX(endBounds.getMinX() - rootBounds.getMinX());
        drop.setToY(endBounds.getMinY() - rootBounds.getMinY());
        drop.setInterpolator(Interpolator.EASE_IN);

        final ImageView finalFlyer = flyer;

        drop.setOnFinished(e -> {
            rootPane.getChildren().remove(finalFlyer);
            placeholder.setOpacity(1.0);
            dropCardsSequentially(upperSlots, lowerBox, index + 1, onComplete);
        });

        drop.play();
    }

    /**
     * Builds a board card slot for the given id: a card image with the default drop shadow, a hover
     * tooltip from the card catalog, and a click handler that triggers a pick. The slot is tagged
     * with {@code card_slot_<id>} userData so it can be located on the board.
     *
     * @param cardId the id of the card to render
     * @return the card slot node, or {@code null} if the card image cannot be loaded
     */
    private StackPane createCardSlotNode(int cardId) {
        String imagePath = "/view/GUI/images/cards/card_" + cardId + ".png";
        try {
            var stream = getClass().getResourceAsStream(imagePath);
            if (stream == null) return null;

            ImageView cardView = new ImageView(new Image(stream));
            cardView.setFitHeight(160);
            cardView.setPreserveRatio(true);
            cardView.setUserData("game_card");

            DropShadow defaultShadow = new DropShadow();
            defaultShadow.setRadius(8); defaultShadow.setOffsetY(3); defaultShadow.setColor(Color.rgb(0,0,0,0.8));
            cardView.setEffect(defaultShadow);

            applyHoverEffect(cardView, cardInfo.describeCard(cardId));

            StackPane cardSlot = new StackPane(cardView);
            cardSlot.setAlignment(Pos.CENTER);
            cardSlot.setUserData("card_slot_" + cardId);

            cardView.setOnMouseClicked(event -> handleCardClick(cardId, cardView));

            return cardSlot;
        } catch (Exception e) { return null; }
    }

    /**
     * Recursively animates new cards being dealt one after another from the deck into a row: for
     * each id it adds an invisible placeholder, flies a temporary card image from the deck slot to
     * the placeholder, reveals it, then proceeds to the next id. Runs {@code onComplete} when all
     * cards have been dealt (or immediately if there is no deck slot or no ids).
     *
     * @param newCardIds the ids of the cards to deal, in order
     * @param targetBox  the destination row
     * @param index      the index of the card to process in this step
     * @param onComplete callback invoked once every card has been dealt (may be {@code null})
     */
    private void dealCardsSequentially(List<Integer> newCardIds, HBox targetBox, int index, Runnable onComplete) {
        if (newCardIds == null || index >= newCardIds.size()) {
            if (onComplete != null) onComplete.run();
            return;
        }

        int cardId = newCardIds.get(index);

        Node deckSlot = null;
        for (Node node : centerTilesBox.getChildren()) {
            if ("deck_slot".equals(node.getUserData())) {
                deckSlot = node; break;
            }
        }

        if (deckSlot == null) {
            dealCardsSequentially(newCardIds, targetBox, index + 1, onComplete);
            return;
        }

        StackPane placeholder = createCardSlotNode(cardId);
        placeholder.setOpacity(0);
        targetBox.getChildren().add(placeholder);

        targetBox.applyCss();
        targetBox.layout();

        ImageView flyer = null;
        try {
            var stream = getClass().getResourceAsStream("/view/GUI/images/cards/card_" + cardId + ".png");
            if (stream != null) {
                flyer = new ImageView(new Image(stream));
                flyer.setFitHeight(160); flyer.setPreserveRatio(true);
                DropShadow shadow = new DropShadow();
                shadow.setRadius(8); shadow.setOffsetY(3); shadow.setColor(Color.rgb(0,0,0,0.8));
                flyer.setEffect(shadow);
            }
        } catch (Exception ignored) {}

        if (flyer == null) {
            dealCardsSequentially(newCardIds, targetBox, index + 1, onComplete); return;
        }

        flyer.setViewOrder(-20.0);
        StackPane.setAlignment(flyer, Pos.TOP_LEFT);
        flyer.setMouseTransparent(true);

        var startBounds = deckSlot.localToScene(deckSlot.getBoundsInLocal());
        var endBounds = placeholder.localToScene(placeholder.getBoundsInLocal());
        var rootBounds = rootPane.localToScene(rootPane.getBoundsInLocal());

        flyer.setTranslateX(startBounds.getMinX() - rootBounds.getMinX());
        flyer.setTranslateY(startBounds.getMinY() - rootBounds.getMinY());

        rootPane.getChildren().add(flyer);

        TranslateTransition dealAnim = new TranslateTransition(Duration.millis(500), flyer);
        dealAnim.setToX(endBounds.getMinX() - rootBounds.getMinX());
        dealAnim.setToY(endBounds.getMinY() - rootBounds.getMinY());
        dealAnim.setInterpolator(Interpolator.EASE_OUT);

        final ImageView finalFlyer = flyer;
        dealAnim.setOnFinished(e -> {
            rootPane.getChildren().remove(finalFlyer);
            placeholder.setOpacity(1.0);
            dealCardsSequentially(newCardIds, targetBox, index + 1, onComplete);
        });

        dealAnim.play();
    }

    // ==========================================
    // LOGICA ANIMAZIONE RISOLUZIONE EVENTI
    // ==========================================

    /**
     * Marks the current event animation as finished and either plays the next queued event
     * animation, or — when the queue is empty — flushes all actions that were deferred while
     * events were animating.
     */
    private void finishEventAnimationAndCheckQueue() {
        isAnimatingEvent = false;

        if (!eventAnimationQueue.isEmpty()) {
            playNextEventAnimation();
        } else {
            while (!deferredActionsQueue.isEmpty()) {
                deferredActionsQueue.poll().run();
            }
        }
    }

    /**
     * Searches all four card rows for the slot holding the given card id.
     *
     * @param cardId the card id to locate
     * @return the matching card slot, or {@code null} if it is not on the board
     */
    private StackPane findCardSlotOnBoard(int cardId) {
        String targetUserData = "card_slot_" + cardId;
        HBox[] rows = {upperBuildingsBox, lowerBuildingsBox, upperTribeBox, lowerTribeBox};

        for (HBox row : rows) {
            for (Node slotNode : row.getChildren()) {
                if (targetUserData.equals(slotNode.getUserData())) {
                    return (StackPane) slotNode;
                }
            }
        }
        return null;
    }

    /**
     * Plays the spotlight animation for the next queued event card: the card on the board moves to
     * the screen centre while scaling up and rotating, pauses so it can be read, then returns to its
     * place. On completion it advances the event queue. Does nothing if an event is already
     * animating or the queue is empty, and skips gracefully if the card is no longer on the board.
     */
    private void playNextEventAnimation() {
        if (isAnimatingEvent || eventAnimationQueue.isEmpty()) {
            return;
        }

        isAnimatingEvent = true;
        int cardId = eventAnimationQueue.poll();

        StackPane eventSlot = findCardSlotOnBoard(cardId);

        if (eventSlot == null) {
            finishEventAnimationAndCheckQueue();
            return;
        }

        eventSlot.setViewOrder(-100.0);

        var startBounds = eventSlot.localToScene(eventSlot.getBoundsInLocal());
        var rootBounds = rootPane.localToScene(rootPane.getBoundsInLocal());

        double centerX = (rootBounds.getWidth() / 2) - (startBounds.getMinX() - rootBounds.getMinX() + startBounds.getWidth() / 2);
        double centerY = (rootBounds.getHeight() / 2) - (startBounds.getMinY() - rootBounds.getMinY() + startBounds.getHeight() / 2);

        TranslateTransition translateUp = new TranslateTransition(Duration.millis(700), eventSlot);
        translateUp.setToX(centerX);
        translateUp.setToY(centerY);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(700), eventSlot);
        scaleUp.setToX(2.5);
        scaleUp.setToY(2.5);

        RotateTransition rotateUp = new RotateTransition(Duration.millis(700), eventSlot);
        rotateUp.setByAngle(360);

        ParallelTransition moveUp = new ParallelTransition(translateUp, scaleUp, rotateUp);
        moveUp.setInterpolator(Interpolator.EASE_OUT);

        PauseTransition readPause = new PauseTransition(Duration.millis(1500));

        TranslateTransition translateDown = new TranslateTransition(Duration.millis(500), eventSlot);
        translateDown.setToX(0);
        translateDown.setToY(0);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(500), eventSlot);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        RotateTransition rotateDown = new RotateTransition(Duration.millis(500), eventSlot);
        rotateDown.setByAngle(-360);

        ParallelTransition moveDown = new ParallelTransition(translateDown, scaleDown, rotateDown);
        moveDown.setInterpolator(Interpolator.EASE_IN);

        SequentialTransition fullSequence = new SequentialTransition(moveUp, readPause, moveDown);

        fullSequence.setOnFinished(e -> {
            eventSlot.setViewOrder(0.0);
            finishEventAnimationAndCheckQueue();
        });

        fullSequence.play();
    }

    // ==========================================
    // logica di riconnessione
    // ==========================================

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
     * Re-registers the local player with the server once the connection is restored by sending a
     * {@link ReconnectRequest}; the resulting snapshot then refreshes the board.
     */
    @Override
    public void handleReconnected() {
        serverAdapter.sendRequest(new ReconnectRequest(localPlayerNickname));
    }
}