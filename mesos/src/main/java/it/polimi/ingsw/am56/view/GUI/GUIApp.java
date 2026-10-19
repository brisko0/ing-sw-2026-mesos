package it.polimi.ingsw.am56.view.GUI;

import it.polimi.ingsw.am56.core.ConnectionFactory;
import it.polimi.ingsw.am56.network.NetworkConnectionFactory; // Assicurati che l'import sia corretto
import it.polimi.ingsw.am56.view.GUI.controllers.SocketRmiChoiceController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX {@link Application} entry point for the GUI client. It opens the first window — the
 * server-connection choice screen ({@code SocketRmiChoice.fxml}) — and hands the configured
 * {@link ConnectionFactory} to its controller.
 * <p>
 * The factory is injected through the static {@link #setConnectionFactory(ConnectionFactory)}
 * before {@code launch} is called, because JavaFX instantiates the {@code Application} itself and
 * does not allow passing constructor arguments.
 */
public class GUIApp extends Application {

    private static ConnectionFactory connectionFactory;

    /**
     * Sets the connection factory the GUI will use to open server connections. Must be called
     * before the application is launched, since JavaFX creates the instance reflectively.
     *
     * @param factory the connection factory to inject
     */
    public static void setConnectionFactory(ConnectionFactory factory) {
        connectionFactory = factory;
    }

    /**
     * Loads the connection-choice screen, injects the {@link ConnectionFactory} into its
     * controller, and shows the primary window. The window is resizable with a minimum size, and
     * closing it terminates the JVM.
     *
     * @param primaryStage the primary stage supplied by JavaFX
     * @throws Exception if the FXML resource cannot be loaded
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/GUI/fxml/SocketRmiChoice.fxml"));
        Parent root = loader.load();

        SocketRmiChoiceController controller = loader.getController();
        controller.setConnectionFactory(connectionFactory);

        // dimensione iniziale non fissa
        Scene scene = new Scene(root, 1024, 576);
        primaryStage.setTitle("AM56 - Mesos - Connessione Server");
        primaryStage.setScene(scene);

        // ridimensionamento abilitato
        primaryStage.setResizable(true);

        // dimensione minima per non far sparire la UI
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(500);

        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.show();
    }

    /**
     * Called by JavaFX when the application shuts down; forces the JVM to exit so no lingering
     * non-daemon (e.g. network) threads keep the process alive.
     */
    @Override
    public void stop() {
        System.exit(0);
    }
}