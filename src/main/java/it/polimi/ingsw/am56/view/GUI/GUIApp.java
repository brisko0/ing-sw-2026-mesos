package it.polimi.ingsw.am56.view.GUI;

import it.polimi.ingsw.am56.core.ConnectionFactory;
import it.polimi.ingsw.am56.network.NetworkConnectionFactory; // Assicurati che l'import sia corretto
import it.polimi.ingsw.am56.view.GUI.controllers.SocketRmiChoiceController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GUIApp extends Application {

    private static ConnectionFactory connectionFactory;

    public static void setConnectionFactory(ConnectionFactory factory) {
        connectionFactory = factory;
    }

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

    @Override
    public void stop() {
        System.exit(0);
    }
}