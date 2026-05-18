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
    @FXML private HBox scalableContainer; // Il contenitore che ingrandiremo/rim rimpiccioliremo
    @FXML private VBox matchRankingContainer;
    @FXML private VBox globalLeaderboardContainer;

    private String localPlayerNickname;

    /**
     * Metodo chiamato automaticamente da JavaFX quando l'FXML viene caricato
     */
    @FXML
    public void initialize() {
        // Applichiamo un listener: se la finestra cambia dimensione, ricalcola lo zoom
        rootPane.widthProperty().addListener((obs, oldVal, newVal) -> scaleUI());
        rootPane.heightProperty().addListener((obs, oldVal, newVal) -> scaleUI());
    }

    /**
     * Calcola il moltiplicatore per mantenere la UI perfettamente proporzionata
     */
    private void scaleUI() {
        if (rootPane.getWidth() == 0 || rootPane.getHeight() == 0) return;

        // Risoluzione base di riferimento (la grandezza ideale del nostro HBox)
        double baseWidth = 1280.0;
        double baseHeight = 720.0;

        // Troviamo il fattore di scala prendendo il minore tra larghezza e altezza,
        // così siamo certi che l'interfaccia non uscirà mai fuori dallo schermo.
        double scaleFactor = Math.min(rootPane.getWidth() / baseWidth, rootPane.getHeight() / baseHeight);

        // Applichiamo lo zoom (sia per schermi più piccoli che massimizzati)
        scalableContainer.setScaleX(scaleFactor);
        scalableContainer.setScaleY(scaleFactor);
    }

    public void initData(GameEndedMessage message, String localNickname) {
        this.localPlayerNickname = localNickname;

        Platform.runLater(() -> {
            // Un colpo a vuoto per forzare la prima scalatura prima che venga visualizzata la scena
            scaleUI();

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