package it.polimi.ingsw.am56.view.GUI.controllers;

import it.polimi.ingsw.am56.network.message.classesDTO.EndGameDTO;
import it.polimi.ingsw.am56.network.message.update.GameEndedMessage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controller for the end-game screen. It renders two rankings side by side — the just-played
 * match standings and the global leaderboard for games of the same size — from the
 * {@link GameEndedMessage}, and lets the user close the application.
 * <p>
 * The whole UI is scaled to fit the window: the layout is designed against a fixed
 * 1280×720 base and {@link #scaleUI()} rescales it on resize. The local player's row is
 * highlighted, and in the global leaderboard the view shows the top entries plus the local
 * player's row when they fall outside the top five.
 */
public class EndGameController {

    @FXML private StackPane rootPane;
    @FXML private HBox scalableContainer;
    @FXML private VBox matchRankingContainer;
    @FXML private VBox globalLeaderboardContainer;

    @FXML private Label matchGameSizeLabel;
    @FXML private Label globalGameSizeLabel;

    @FXML private Button exitButton;

    private String localPlayerNickname;

    /**
     * Hooks resize listeners on the root pane so the UI is rescaled whenever the window's width
     * or height changes. Invoked automatically by JavaFX after the FXML is loaded.
     */
    @FXML
    public void initialize() {
        rootPane.widthProperty().addListener((obs, oldVal, newVal) -> scaleUI());
        rootPane.heightProperty().addListener((obs, oldVal, newVal) -> scaleUI());
    }

    /**
     * Closes the game window, exactly as clicking the window's close button would. Firing the
     * window's close request lets any {@code onCloseRequest} handler run; with the default
     * implicit-exit behaviour this also shuts the application down once the last window is gone.
     */
    @FXML
    private void handleExit() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
        Platform.exit();
    }

    /**
     * Uniformly scales the content container to fit the current window, preserving aspect ratio.
     * The scale factor is the smaller of the width and height ratios against the 1280×720 base
     * size. No-op until the pane has been laid out (non-zero size).
     */
    private void scaleUI() {
        if (rootPane.getWidth() == 0 || rootPane.getHeight() == 0) return;

        double baseWidth = 1280.0;
        double baseHeight = 720.0;

        double scaleFactor = Math.min(rootPane.getWidth() / baseWidth, rootPane.getHeight() / baseHeight);

        scalableContainer.setScaleX(scaleFactor);
        scalableContainer.setScaleY(scaleFactor);
    }

    /**
     * Populates the screen from the end-game message. Stores the local player's nickname (used to
     * highlight their row and decide what to show in the global leaderboard), sets the
     * game-size labels, and fills both ranking columns. The UI work is posted to the JavaFX
     * thread via {@link Platform#runLater}.
     *
     * @param message       the end-game message carrying the match ranking and global leaderboard
     * @param localNickname the nickname of the player running this client
     */
    public void initData(GameEndedMessage message, String localNickname) {
        this.localPlayerNickname = localNickname;

        Platform.runLater(() -> {
            scaleUI();

            String sizeText = "Partita da " + message.gameSize() + " giocatori";
            matchGameSizeLabel.setText(sizeText);
            globalGameSizeLabel.setText(sizeText);

            populateRanking(matchRankingContainer, message.matchRanking(), false);
            populateRanking(globalLeaderboardContainer, message.globalLeaderboard(), true);
        });
    }

    /**
     * Fills a ranking column with one row per entry. For the match ranking ({@code isGlobal} false)
     * every entry is shown. For the global leaderboard ({@code isGlobal} true) the display is
     * trimmed: if the local player is in the top five, the top five are shown; otherwise the top
     * three are shown followed by a {@code "..."} separator and the local player's own row. Shows
     * a placeholder label when there is no data.
     *
     * @param container the column to populate (its current children are cleared first)
     * @param ranking   the ranking entries to display
     * @param isGlobal  whether this is the global leaderboard (enables the trimming logic)
     */
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

    /**
     * Builds a single ranking row: rank, nickname (with a 👑 next to the match winner), the score
     * in Prestige Points, and a sub-line that shows the match date for the global leaderboard or
     * the leftover food for the match ranking. The local player's row gets a dedicated style class.
     *
     * @param entry    the ranking entry to render
     * @param isGlobal whether the row belongs to the global leaderboard (drives the sub-line and
     *                 suppresses the winner crown)
     * @return the assembled row node
     */
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
        Label nameLbl = new Label(displayName);
        nameLbl.getStyleClass().add("name-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox rightStats = new VBox(-2);
        rightStats.setAlignment(Pos.CENTER_RIGHT);

        Label scoreLbl = new Label(entry.score() + " PP");
        scoreLbl.getStyleClass().add("score-label");

        String subInfoText = isGlobal ? entry.date() : "Cibo: " + entry.food();
        Label subLbl = new Label(subInfoText);
        subLbl.getStyleClass().add("sub-label");

        rightStats.getChildren().addAll(scoreLbl, subLbl);
        row.getChildren().addAll(rankLbl, nameLbl, spacer, rightStats);

        return row;
    }
}