package it.polimi.ingsw.am56.view.GUI.controllers;

import it.polimi.ingsw.am56.network.message.classesDTO.EndGameDTO;
import it.polimi.ingsw.am56.network.message.update.GameEndedMessage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class EndGameController {

    @FXML private VBox matchRankingContainer;
    @FXML private VBox globalLeaderboardContainer;

    private String localPlayerNickname;

    public void initData(GameEndedMessage message, String localNickname) {
        this.localPlayerNickname = localNickname;

        Platform.runLater(() -> {
            populateRanking(matchRankingContainer, message.matchRanking(), false);
            populateRanking(globalLeaderboardContainer, message.globalLeaderboard(), true);
        });
    }

    private void populateRanking(VBox container, List<EndGameDTO> ranking, boolean isGlobal) {
        container.getChildren().clear();

        if (ranking == null || ranking.isEmpty()) {
            Label emptyLabel = new Label("Nessun dato disponibile.");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
            container.getChildren().add(emptyLabel);
            return;
        }

        for (EndGameDTO entry : ranking) {
            HBox row = createRankingRow(entry, isGlobal);
            container.getChildren().add(row);
        }
    }

    private HBox createRankingRow(EndGameDTO entry, boolean isGlobal) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 15, 12, 15));

        String defaultStyle = "-fx-background-color: #f8f9fa; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #e0e0e0; -fx-border-width: 1;";

        String localPlayerStyle = "-fx-background-color: #fff9e6; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #f1c40f; -fx-border-width: 2;";

        row.setStyle(entry.nickname().equals(localPlayerNickname) ? localPlayerStyle : defaultStyle);

        Label rankLbl = new Label("#" + entry.rank());
        rankLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        rankLbl.setMinWidth(45);

        String displayName = entry.nickname();
        if (entry.isWinner() && !isGlobal) {
            displayName += " 👑";
        }
        Label nameLbl = new Label(displayName.toUpperCase());
        nameLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #34495e;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox rightStats = new VBox(2);
        rightStats.setAlignment(Pos.CENTER_RIGHT);

        Label scoreLbl = new Label(entry.score() + " PT");
        scoreLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        String subInfoText = isGlobal ? entry.date() : "Cibo: " + entry.food();
        Label subLbl = new Label(subInfoText);
        subLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #95a5a6;");

        rightStats.getChildren().addAll(scoreLbl, subLbl);

        row.getChildren().addAll(rankLbl, nameLbl, spacer, rightStats);

        return row;
    }
}