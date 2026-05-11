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

    // ==========================================
    // board
    // ==========================================

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
                    applyHoverEffect(offerStack, "Tessera Offerta " + letter);

                    offerTileSlots.put(letter.toUpperCase(), offerStack);
                    offerTilesWrapper.getChildren().add(offerStack);
                }
            }
            centerTilesBox.getChildren().add(offerTilesWrapper);
        }
        refreshTotems();
    }

    private void handleOfferTileClick(String letter) {
        if (currentPlayerInfo != null && currentPhase != null && localPlayerNickname != null) {
            if (localPlayerNickname.equals(currentPlayerInfo.nickname()) && currentPhase.contains("TotemPlacement")) {
                serverAdapter.sendRequest(new TotemMoveRequest(localPlayerNickname, letter));
                applyGlowsBasedOnState();
            }
        }
    }

    private ImageView createTileImage(String imagePath) {
        try {
            var stream = getClass().getResourceAsStream(imagePath);
            if (stream == null) return null;
            ImageView tileView = new ImageView(new Image(stream));
            tileView.setFitHeight(180); tileView.setPreserveRatio(true);
            return tileView;
        } catch (Exception e) { return null; }
    }

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

    private void handleCardClick(int cardId, Node clickedCard) {
        if (currentPlayerInfo != null && currentPhase != null && localPlayerNickname != null) {
            if (localPlayerNickname.equals(currentPlayerInfo.nickname()) && currentPhase.contains("CardPicking")) {
                pendingCardAnimations.put(cardId, clickedCard.localToScene(clickedCard.getBoundsInLocal()));
                serverAdapter.sendRequest(new CardPickRequest(localPlayerNickname, cardId));
                clickedCard.setDisable(true);
                clickedCard.setEffect(null);
            }
        }
    }

    // ==========================================
    // dashboard giocatori e animazione
    // ==========================================

    private void updateOrAddPlayerDashboard(PlayerStatsDTO stats) {
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

            HBox bottomStats = new HBox(20);
            bottomStats.setAlignment(Pos.CENTER);
            dashboard.getChildren().add(bottomStats);

            playerDashboards.put(stats.nickname(), dashboard);
            playersStatsContainer.getChildren().add(dashboard);
        }

        HBox headerBox = (HBox) dashboard.getChildren().get(0);
        Label nameLabel = (Label) headerBox.getChildren().get(0);
        nameLabel.setText(stats.nickname().toUpperCase());
        nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + stats.color() + ";");

        HBox topStatsBox = (HBox) headerBox.getChildren().get(2);
        topStatsBox.getChildren().clear();
        topStatsBox.getChildren().addAll(
                createStatIcon("food", stats.food()),
                createStatIcon("points", stats.points())
        );

        refreshCategoryRow((HBox) dashboard.getChildren().get(1), "collector", stats.collectorCards());
        refreshCategoryRow((HBox) dashboard.getChildren().get(2), "hunter", stats.hunterCards());
        refreshCategoryRow((HBox) dashboard.getChildren().get(3), "builder", stats.builderCards());
        refreshCategoryRow((HBox) dashboard.getChildren().get(4), "shaman", stats.shamanCards());
        refreshCategoryRow((HBox) dashboard.getChildren().get(5), "artist", stats.artistCards());
        refreshCategoryRow((HBox) dashboard.getChildren().get(6), "inventor", stats.inventorCards());

        HBox bottomStats = (HBox) dashboard.getChildren().get(7);
        bottomStats.getChildren().clear();
        bottomStats.getChildren().addAll(
                createStatIcon("feedingSale", stats.feedingSale()),
                createStatIcon("buildingSale", stats.buildingSale()),
                createStatIcon("shamanStars", stats.shamanStars()),
                createStatIcon("inventions", stats.inventionCount())
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

    private HBox createEmptyCategoryRow(String type, String playerNick) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMinHeight(130);
        row.setUserData("dashboard_row_" + playerNick + "_" + type);
        return row;
    }

    private void refreshCategoryRow(HBox row, String type, List<Integer> cardIds) {
        row.getChildren().clear();

        HBox typeInfo = createStatIcon(type, cardIds.size());
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

    private HBox createStatIcon(String iconName, int value) {
        HBox container = new HBox(5);
        container.setAlignment(Pos.CENTER);

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
        } catch (Exception ignored) {}

        return container;
    }

    private String findCategoryOfCard(int cardId, PlayerStatsDTO stats) {
        if (stats.collectorCards().contains(cardId)) return "collector";
        if (stats.hunterCards().contains(cardId)) return "hunter";
        if (stats.builderCards().contains(cardId)) return "builder";
        if (stats.shamanCards().contains(cardId)) return "shaman";
        if (stats.artistCards().contains(cardId)) return "artist";
        if (stats.inventorCards().contains(cardId)) return "inventor";
        return null;
    }

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

    private void animateCardPickFromBounds(int cardId, javafx.geometry.Bounds startBounds, HBox targetRow) {
        if (startBounds == null || targetRow == null) return;

        String imagePath = "/view/GUI/images/cards/card_" + cardId + ".png";
        ImageView flyer = null;
        try {
            var stream = getClass().getResourceAsStream(imagePath);
            if (stream != null) {
                flyer = new ImageView(new Image(stream));
                flyer.setFitHeight(160); flyer.setPreserveRatio(true);

                DropShadow shadow = new DropShadow()
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
    // gestione totem e animazione
    // ==========================================

    private void animateTotemMove(TotemPositionDTO from, TotemPositionDTO to, List<TotemPositionDTO> finalState) {
        StackPane startSlot = findSlot(from.locationType(), from.locationId());
        StackPane endSlot = findSlot(to.locationType(), to.locationId());

        if (startSlot == null || endSlot == null) {
            this.currentTotems = finalState; refreshTotems(); return;
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
        });

        transition.play();
    }

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
    // animazione fine round
    // ==========================================

    private void animateEndRoundBoardShift(BoardDTO newBoard) {
        isAnimatingBoard = true;
        clearAllGlows();

        ParallelTransition fadeOutLower = new ParallelTransition();

        for (Node slot : lowerTribeBox.getChildren()) {
            FadeTransition ft = new FadeTransition(Duration.millis(400), slot);
            ft.setToValue(0);
            fadeOutLower.getChildren().add(ft);
        }

        if (fadeOutLower.getChildren().isEmpty()) {
            executeDrops(newBoard);
        } else {
            fadeOutLower.setOnFinished(e -> executeDrops(newBoard));
            fadeOutLower.play();
        }
    }

    private void executeDrops(BoardDTO newBoard) {
        lowerTribeBox.getChildren().clear();
        lowerTribeBox.setVisible(true); lowerTribeBox.setManaged(true);

        List<Node> upperTribeNodes = new ArrayList<>(upperTribeBox.getChildren());

        dropCardsSequentially(upperTribeNodes, lowerTribeBox, 0, () -> {

            updateCardRow(lowerBuildingsBox, newBoard.lowerBuildingsIds());
            updateCardRow(lowerTribeBox, newBoard.lowerCharEvIds());
            updateCenterTiles(newBoard.orderTileCount(), newBoard.offerTilesLetters());

            upperTribeBox.getChildren().clear();
            upperTribeBox.setVisible(true); upperTribeBox.setManaged(true);

            dealCardsSequentially(newBoard.upperCharEvIds(), upperTribeBox, 0, () -> {

                updateCardRow(upperBuildingsBox, newBoard.upperBuildingsIds());

                isAnimatingBoard = false;
                applyGlowsBasedOnState();
            });
        });
    }

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

            applyHoverEffect(cardView, "Carta ID: " + cardId);

            StackPane cardSlot = new StackPane(cardView);
            cardSlot.setAlignment(Pos.CENTER);
            cardSlot.setUserData("card_slot_" + cardId);

            cardView.setOnMouseClicked(event -> handleCardClick(cardId, cardView));

            return cardSlot;
        } catch (Exception e) { return null; }
    }

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
    // risoluzione e animazione eventi
    // ==========================================

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
}