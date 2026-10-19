package it.polimi.ingsw.am56.view.GUI.controllers;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Small UI helper that overlays a transient red error message on top of a
 * {@link StackPane} root for a few seconds, then removes it. Used to surface
 * server-side rejections (e.g. a duplicate nickname) to the player.
 */
final class GuiErrorToast {

    private static final int DURATION_SECONDS = 3;

    private GuiErrorToast() {}

    /**
     * Shows {@code message} as a red banner near the top of {@code root} for
     * {@value #DURATION_SECONDS} seconds. Safe to call from any thread.
     *
     * @param root    the StackPane to overlay the message on
     * @param message the error text to display
     */
    static void show(StackPane root, String message) {
        if (root == null || message == null) return;

        Platform.runLater(() -> {
            Label label = new Label(message);
            label.setWrapText(true);
            label.setMouseTransparent(true);
            label.setStyle(
                    "-fx-background-color: rgba(0, 0, 0, 0.78);"
                            + " -fx-text-fill: #ff4d4d;"
                            + " -fx-font-weight: bold;"
                            + " -fx-font-size: 16px;"
                            + " -fx-padding: 12 22 12 22;"
                            + " -fx-background-radius: 8;");

            StackPane.setAlignment(label, Pos.TOP_CENTER);
            label.setTranslateY(40);
            root.getChildren().add(label);

            PauseTransition delay = new PauseTransition(Duration.seconds(DURATION_SECONDS));
            delay.setOnFinished(e -> root.getChildren().remove(label));
            delay.play();
        });
    }
}
