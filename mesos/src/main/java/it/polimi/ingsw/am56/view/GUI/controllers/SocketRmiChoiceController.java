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

/**
 * Controller for the first GUI screen, where the user chooses how to connect to the server
 * (RMI or socket) and enters the host/port. On a successful connection it builds a
 * {@link ConnectionManager} for the chosen transport and swaps the scene to the pre-lobby.
 */
public class SocketRmiChoiceController {

    @FXML private StackPane rootPane; // Ci serve per ottenere la Scena attuale

    @FXML private TextField rmiIpField;
    @FXML private TextField rmiPortField;
    @FXML private TextField socketIpField;
    @FXML private TextField socketPortField;

    @FXML private Label errorLabel;

    private ConnectionFactory connectionFactory;
    private ServerAdapter serverAdapter;

    /**
     * Injects the factory used to open the actual RMI/socket connections.
     *
     * @param factory the connection factory to use
     */
    public void setConnectionFactory(ConnectionFactory factory) {
        this.connectionFactory = factory;
    }

    /**
     * Pre-fills the host and port fields from the {@code MESOS_SERVER_IP}, {@code MESOS_RMI_PORT}
     * and {@code MESOS_SOCKET_PORT} environment variables, falling back to local defaults
     * ({@code 127.0.0.1}, {@code 1099}, {@code 12345}). Invoked automatically by JavaFX after
     * the FXML is loaded.
     */
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

    /**
     * Handles the "connect via RMI" action: reads the RMI host/port, opens an RMI connection
     * through the factory, wraps it in a {@link ConnectionManager} and proceeds to the lobby.
     * Any failure is reported inline via {@link #showError(String)} without leaving the screen.
     */
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

    /**
     * Handles the "connect via socket" action: reads the socket host/port, opens a socket
     * connection through the factory, wraps it in a {@link ConnectionManager} and proceeds to the
     * lobby. Any failure is reported inline via {@link #showError(String)} without leaving the
     * screen.
     */
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

    /**
     * Shows an error message on the screen and hides it again after about three seconds. The
     * delayed hide runs on a background thread and re-enters the JavaFX thread via
     * {@link javafx.application.Platform#runLater}.
     *
     * @param message the error text to display
     */
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

    /**
     * Switches to the pre-lobby screen, reusing the current window: it loads {@code PreLobby.fxml},
     * passes the freshly built {@link ServerAdapter} to its controller, and replaces the current
     * scene's root with the new view.
     */
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