package it.polimi.ingsw.am56.view.GUI.controllers;

import it.polimi.ingsw.am56.core.ConnectionFactory;
import it.polimi.ingsw.am56.core.ServerAdapter;
import it.polimi.ingsw.am56.network.ConnectionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;

import java.io.IOException;

public class SocketRmiChoiceController {

    @FXML private StackPane rootPane; // Ci serve per ottenere la Scena attuale

    @FXML private TextField rmiIpField;
    @FXML private TextField rmiPortField;
    @FXML private TextField socketIpField;
    @FXML private TextField socketPortField;

    @FXML private Label errorLabel;

    private ConnectionFactory connectionFactory;
    private ServerAdapter serverAdapter;

    public void setConnectionFactory(ConnectionFactory factory) {
        this.connectionFactory = factory;
    }

    @FXML
    public void initialize() {
        String ip   = System.getenv().getOrDefault("MESOS_SERVER_IP",    "127.0.0.1");
        String rPort= System.getenv().getOrDefault("MESOS_RMI_PORT",     "1099");
        String sPort= System.getenv().getOrDefault("MESOS_SOCKET_PORT",  "12345");

        rmiIpField.setText(ip);
        rmiPortField.setText(rPort);
        socketIpField.setText(ip);
        socketPortField.setText(sPort);
    }

    @FXML
    private void handleRmiConnect() {
        String ip = rmiIpField.getText().trim();
        try {
            int port = Integer.parseInt(rmiPortField.getText().trim());
            ServerAdapter raw = connectionFactory.createRmiConnection(ip, port);
            serverAdapter = new ConnectionManager(ip, port, true, connectionFactory, raw);
            errorLabel.setVisible(false);
            goToLobby();
        } catch (Exception e) {
            System.err.println("[GUI] Errore di connessione RMI: " + e.getMessage());
            showError("Nessun server RMI attivo su " + ip + ":" + rmiPortField.getText().trim());
        }
    }

    @FXML
    private void handleSocketConnect() {
        String ip = socketIpField.getText().trim();
        try {
            int port = Integer.parseInt(socketPortField.getText().trim());
            ServerAdapter raw = connectionFactory.createSocketConnection(ip, port);
            serverAdapter = new ConnectionManager(ip, port, false, connectionFactory, raw);
            errorLabel.setVisible(false);
            goToLobby();
        } catch (Exception e) {
            System.err.println("[GUI] Errore di connessione Socket: " + e.getMessage());
            showError("Nessun server Socket attivo su " + ip + ":" + socketPortField.getText().trim());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);

        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {}
            javafx.application.Platform.runLater(() -> errorLabel.setVisible(false));
        }).start();
    }

    // --- Metodo per cambiare scena ---
    private void goToLobby() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/GUI/fxml/PreLobby.fxml"));
            Parent lobbyRoot = loader.load();

            // Otteniamo il controller della nuova schermata e gli passiamo il serverAdapter!
            PreLobbyController preLobbyController = loader.getController();
            preLobbyController.setServerAdapter(serverAdapter);

            // Cambiamo la radice (root) della scena attuale, mantenendo la stessa finestra
            rootPane.getScene().setRoot(lobbyRoot);

        } catch (IOException e) {
            System.err.println("[GUI] Errore nel caricamento della Lobby: " + e.getMessage());
            e.printStackTrace();
        }
    }
}