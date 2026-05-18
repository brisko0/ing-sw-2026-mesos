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
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #3d1f14; -fx-font-weight: bold;");
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

        // Assegnazione delle classi CSS
        if (entry.nickname().equals(localPlayerNickname)) {
            row.getStyleClass().add("ranking-row-local");
        } else {
            row.getStyleClass().add("ranking-row");
        }

        // Posizione
        Label rankLbl = new Label("#" + entry.rank());
        rankLbl.getStyleClass().add("rank-label");
        rankLbl.setMinWidth(40);

        // Nome
        String displayName = entry.nickname();
        if (entry.isWinner() && !isGlobal) {
            displayName += " 👑";
        }
        Label nameLbl = new Label(displayName.toUpperCase());
        nameLbl.getStyleClass().add("name-label");

        // Spaziatore centrale
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Dati a destra
        VBox rightStats = new VBox(-2); // -2 per avvicinare i due testi
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