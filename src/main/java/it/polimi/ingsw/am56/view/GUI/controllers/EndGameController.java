package it.polimi.ingsw.am56.view.GUI.controllers;

import it.polimi.ingsw.am56.network.message.classesDTO.EndGameDTO;
import it.polimi.ingsw.am56.network.message.update.GameEndedMessage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class EndGameController {

    @FXML private StackPane rootPane;
    @FXML private HBox scalableContainer;
    @FXML private VBox matchRankingContainer;
    @FXML private VBox globalLeaderboardContainer;

    @FXML private Label matchGameSizeLabel;
    @FXML private Label globalGameSizeLabel;

    private String localPlayerNickname;

    @FXML
    public void initialize() {
        rootPane.widthProperty().addListener((obs, oldVal, newVal) -> scaleUI());
        rootPane.heightProperty().addListener((obs, oldVal, newVal) -> scaleUI());
    }

    private void scaleUI() {
        if (rootPane.getWidth() == 0 || rootPane.getHeight() == 0) return;

        double baseWidth = 1280.0;
        double baseHeight = 720.0;

        double scaleFactor = Math.min(rootPane.getWidth() / baseWidth, rootPane.getHeight() / baseHeight);

        scalableContainer.setScaleX(scaleFactor);
        scalableContainer.setScaleY(scaleFactor);
    }

    public void initData(GameEndedMessage message, String localNickname) {
        this.localPlayerNickname = localNickname;

        Platform.runLater(() -> {
            scaleUI();

            String sizeText = "Game Size: " + message.gameSize();
            matchGameSizeLabel.setText(sizeText);
            globalGameSizeLabel.setText(sizeText);

            populateRanking(matchRankingContainer, message.matchRanking(), false);
            populateRanking(globalLeaderboardContainer, message.globalLeaderboard(), true);
        });
    }

    private void populateRanking(VBox container, List<EndGameDTO> ranking, boolean isGlobal) {
        container.getChildren().clear();

        if (ranking == null || ranking.isEmpty()) {
            Label emptyLabel = new Label("Nessun dato disponibile.");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #3d1f14; -fx-font-weight: bold;");
            container.getChildren().add(emptyLabel);
            return;
        }

        if (!isGlobal) {
            for (EndGameDTO entry : ranking) {
                container.getChildren().add(createRankingRow(entry, false));
            }
            return;
        }

        EndGameDTO localEntry = ranking.stream()
                .filter(e -> e.nickname().equals(localPlayerNickname))
                .findFirst()
                .orElse(null);

        boolean localInTopFive = localEntry != null && localEntry.rank() <= 5;

        if (localInTopFive) {
            int showCount = Math.min(5, ranking.size());
            for (int i = 0; i < showCount; i++) {
                container.getChildren().add(createRankingRow(ranking.get(i), true));
            }
        } else {
            int showCount = Math.min(3, ranking.size());
            for (int i = 0; i < showCount; i++) {
                container.getChildren().add(createRankingRow(ranking.get(i), true));
            }
            if (localEntry != null) {
                Label dots = new Label("...");
                dots.setStyle("-fx-font-size: 18px; -fx-text-fill: #888888; -fx-padding: 2 0 2 20;");
                container.getChildren().add(dots);
                container.getChildren().add(createRankingRow(localEntry, true));
            }
        }
    }

    private HBox createRankingRow(EndGameDTO entry, boolean isGlobal) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);

        if (entry.nickname().equals(localPlayerNickname)) {
            row.getStyleClass().add("ranking-row-local");
        } else {
            row.getStyleClass().add("ranking-row");
        }

        Label rankLbl = new Label("#" + entry.rank());
        rankLbl.getStyleClass().add("rank-label");
        rankLbl.setMinWidth(40);

        String displayName = entry.nickname();
        if (entry.isWinner() && !isGlobal) {
            displayName += " 👑";
        }
        Label nameLbl = new Label(displayName.toUpperCase());
        nameLbl.getStyleClass().add("name-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox rightStats = new VBox(-2);
        rightStats.setAlignment(Pos.CENTER_RIGHT);

        Label scoreLbl = new Label(entry.score() + " PT");
        scoreLbl.getStyleClass().add("score-label");

        String subInfoText = isGlobal ? entry.date() : "Cibo: " + entry.food();
        Label subLbl = new Label(subInfoText);
        subLbl.getStyleClass().add("sub-label");

        rightStats.getChildren().addAll(scoreLbl, subLbl);
        row.getChildren().addAll(rankLbl, nameLbl, spacer, rightStats);

        return row;
    }
}