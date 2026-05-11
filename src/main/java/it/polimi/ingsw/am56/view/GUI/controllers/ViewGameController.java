package it.polimi.ingsw.am56.view.GUI.controllers;

import it.polimi.ingsw.am56.core.ServerAdapter;
import it.polimi.ingsw.am56.core.UpdateHandler;
import it.polimi.ingsw.am56.network.message.classesDTO.*;
import it.polimi.ingsw.am56.network.message.request.GameSyncRequest;
import it.polimi.ingsw.am56.network.message.request.CardPickRequest;
import it.polimi.ingsw.am56.network.message.request.TotemMoveRequest;
import it.polimi.ingsw.am56.network.message.update.*;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

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

    @FXML private VBox customTooltip;
    @FXML private Label tooltipTextLabel;

    private FadeTransition tooltipFade;

    private final Map<String, VBox> playerDashboards = new HashMap<>();
    private final Map<String, StackPane> orderTileSlots = new HashMap<>();
    private final Map<String, StackPane> offerTileSlots = new HashMap<>();

    private List<TotemPositionDTO> currentTotems = new ArrayList<>();

    private CurrentPlayerDTO currentPlayerInfo = null;
    private String currentPhase = null;
    private int currentEraNum = 1;

    private final List<Timeline> activeGlowTimelines = new ArrayList<>();
    private final List<Node> glowingNodes = new ArrayList<>();

    private final Map<Integer, javafx.geometry.Bounds> pendingCardAnimations = new HashMap<>();
    private final Queue<Integer> eventAnimationQueue = new LinkedList<>();
    private final Queue<Runnable> deferredActionsQueue = new LinkedList<>();

    private boolean isAnimatingEvent = false;
    private boolean isAnimatingBoard = false;

    public void setLocalPlayerNickname(String nickname) {
        this.localPlayerNickname = nickname;
    }

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

    @Override
    public void handleUpdate(ServerMessage message) {
        message.accept(this);
    }

    // ==========================================
    // azioni differite
    // ==========================================

    private void executeOrDefer(Runnable action) {
        if (isAnimatingEvent || !eventAnimationQueue.isEmpty()) {
            deferredActionsQueue.add(action);
        } else {
            action.run();
        }
    }

    // ==========================================
    // effetti hover
    // ==========================================

    private void applyHoverEffect(Node node, String description) {
        node.setOnMouseEntered(e -> {
            node.setViewOrder(-1);
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(150), node);
            scaleIn.setToX(1.15); scaleIn.setToY(1.15); scaleIn.play();

            tooltipTextLabel.setText(description);
            customTooltip.setTranslateX(e.getSceneX() + 15);
            customTooltip.setTranslateY(e.getSceneY() + 15);

            tooltipFade.stop(); tooltipFade.setToValue(1.0); tooltipFade.play();
        });

        node.setOnMouseMoved(e -> {
            customTooltip.setTranslateX(e.getSceneX() + 15);
            customTooltip.setTranslateY(e.getSceneY() + 15);
        });

        node.setOnMouseExited(e -> {
            node.setViewOrder(0);
            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), node);
            scaleOut.setToX(1.0); scaleOut.setToY(1.0); scaleOut.play();

            tooltipFade.stop(); tooltipFade.setToValue(0.0); tooltipFade.play();
        });
    }

    // ==========================================
    // ricezione messagig
    // ==========================================

    @Override
    public void visit(GameSnapshotMessage message) {
        Platform.runLater(() -> {
            executeOrDefer(() -> {
                GameSnapshotDTO snap = message.snapshot();

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

    @Override
    public void visit(EndRoundUpdateMessage message) {
        Platform.runLater(() -> {
            executeOrDefer(() -> {
                EndRoundSnapshotDTO snap = message.snapshot();
                this.currentEraNum = snap.currentEra();

                BoardDTO board = snap.board();
                animateEndRoundBoardShift(board);

                for (PlayerStatsDTO stats : snap.playersStats()) {
                    updateOrAddPlayerDashboard(stats);
                }

                updateGamePhaseUI(snap.currentEra(), snap.currentRound());

                this.currentPlayerInfo = snap.currentPlayer();
                updateCurrentPlayerUI();
            });
        });
    }

    @Override
    public void visit(BoardUpdatedMessage message) {
        Platform.runLater(() -> {
            executeOrDefer(() -> {
                BoardDTO board = message.board();
                updateCardRow(upperBuildingsBox, board.upperBuildingsIds());
                updateCardRow(lowerBuildingsBox, board.lowerBuildingsIds());
                updateCardRow(upperTribeBox, board.upperCharEvIds());
                updateCardRow(lowerTribeBox, board.lowerCharEvIds());
                updateCenterTiles(board.orderTileCount(), board.offerTilesLetters());
                applyGlowsBasedOnState();
            });
        });
    }

    @Override
    public void visit(PlayerStatsUpdateMessage message) {
        Platform.runLater(() -> {
            executeOrDefer(() -> {
                updateOrAddPlayerDashboard(message.stats());
            });
        });
    }

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
                }
            });
        });
    }

    @Override
    public void visit(CurrentPlayerUpdateMessage message) {
        Platform.runLater(() -> {
            executeOrDefer(() -> {
                this.currentPlayerInfo = message.currentPlayer();
                updateCurrentPlayerUI();
            });
        });
    }

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

    @Override
    public void visit(EventResolvedMessage message) {
        Platform.runLater(() -> {
            eventAnimationQueue.add(message.cardId());
            playNextEventAnimation();
        });
    }

    // ==========================================
    // UI current player e glow
    // ==========================================

    private void updateCurrentPlayerUI() {
        if (currentPlayerInfo == null) return;

        gameInfoBox.setVisible(true);
        String nick = currentPlayerInfo.nickname();
        String color = currentPlayerInfo.color();

        currentPlayerLabel.setText(nick.toUpperCase());
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

    public void updateGamePhaseUI(int era, int round) {
        if (currentEraLabel != null && currentRoundLabel != null) {
            currentEraLabel.setText(String.valueOf(era));
            currentRoundLabel.setText(String.valueOf(round));
        }
    }

    private void applyGlowsBasedOnState() {
        clearAllGlows();

        if (isAnimatingBoard) return;

        if (currentPlayerInfo == null || localPlayerNickname == null || currentPhase == null) return;

        if (localPlayerNickname.equals(currentPlayerInfo.nickname())) {
            if (currentPhase.contains("TotemPlacement")) {
                for (StackPane offerStack : offerTileSlots.values()) {
                    for (Node node : offerStack.getChildren()) {
                        if ("offer_tile".equals(node.getUserData())) addPulsingGlow(node);
                    }
                }
            }
            else if (currentPhase.contains("CardPicking")) {
                glowCardsInContainer(upperBuildingsBox);
                glowCardsInContainer(lowerBuildingsBox);
                glowCardsInContainer(upperTribeBox);
                glowCardsInContainer(lowerTribeBox);
            }
        }
    }

    private void glowCardsInContainer(HBox container) {
        List<Node> children = new ArrayList<>(container.getChildren());

        for (Node slotNode : children) {
            String slotData = slotNode.getUserData() != null ? slotNode.getUserData().toString() : "";

            if (slotData.startsWith("card_slot_")) {
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
}