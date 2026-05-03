package it.polimi.ingsw.am56.view.GUI.controllers;

import it.polimi.ingsw.am56.core.ConnectionFactory;
import it.polimi.ingsw.am56.core.ServerAdapter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class SocketRmiChoiceController {

    @FXML private StackPane rootPane;

    @FXML private TextField rmiIpField;
    @FXML private TextField rmiPortField;
    @FXML private TextField socketIpField;
    @FXML private TextField socketPortField;

    private ConnectionFactory connectionFactory;
    private ServerAdapter serverAdapter;

    public void setConnectionFactory(ConnectionFactory factory) {
        this.connectionFactory = factory;
    }

    @FXML
    private void handleRmiConnect() {
        String ip = rmiIpField.getText().trim();
        try {
            int port = Integer.parseInt(rmiPortField.getText().trim());

            serverAdapter = connectionFactory.createRmiConnection(ip, port);
            System.out.println("[GUI] Connessione RMI stabilita con " + ip + ":" + port);

            goToLobby();

        } catch (Exception e) {
            System.err.println("[GUI] Errore di connessione RMI: " + e.getMessage());
        }
    }

    @FXML
    private void handleSocketConnect() {
        String ip = socketIpField.getText().trim();
        try {
            int port = Integer.parseInt(socketPortField.getText().trim());

            serverAdapter = connectionFactory.createSocketConnection(ip, port);
            System.out.println("[GUI] Connessione Socket stabilita con " + ip + ":" + port);

            goToLobby();

        } catch (Exception e) {
            System.err.println("[GUI] Errore di connessione Socket: " + e.getMessage());
        }
    }


    private void goToLobby() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/GUI/fxml/PreLobby.fxml"));
            Parent lobbyRoot = loader.load();

            PreLobbyController preLobbyController = loader.getController();
            preLobbyController.setServerAdapter(serverAdapter);

            rootPane.getScene().setRoot(lobbyRoot);

        } catch (IOException e) {
            System.err.println("[GUI] Errore nel caricamento della Lobby: " + e.getMessage());
            e.printStackTrace();
        }
    }
}