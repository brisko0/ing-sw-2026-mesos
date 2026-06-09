package it.polimi.ingsw.am56.network;

import it.polimi.ingsw.am56.controller.EndGameService;
import it.polimi.ingsw.am56.controller.GameController;
import it.polimi.ingsw.am56.database.DatabaseManager;
import it.polimi.ingsw.am56.database.LeaderboardDAO;
import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.PersistenceManager;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.state.State;
import it.polimi.ingsw.am56.network.rmi.server.RmiServer;
import it.polimi.ingsw.am56.network.socket.server.SocketServer;

import java.util.Set;
import java.util.stream.Collectors;

public class ServerApp {
    private static final int RMI_PORT = 1099;
    private static final int SOCKET_PORT = 12345;

    public static void main(String[] args) {
        System.out.println("avvio mesos server (uniti RMI + Socket)");
        try {
            GameModel sharedModel = PersistenceManager.hasSave()
                    ? PersistenceManager.load() : null;
            if (sharedModel == null) {
                sharedModel = new GameModel();
                System.out.println("[SERVER] Nessun salvataggio trovato. Nuova partita.");
            }

            LeaderboardDAO leaderboardDao = new DatabaseManager();

            EndGameService endGameService = new EndGameService(sharedModel, leaderboardDao);
            sharedModel.addObserver(endGameService);

            GameController sharedController = new GameController(sharedModel);

            if (sharedModel.getPlayers() != null
                    && !sharedModel.getPlayers().isEmpty()
                    && sharedModel.getCurrentState().getState() != State.GAME_ENDING) {
                Set<String> expected = sharedModel.getPlayers().stream()
                        .map(Player::getNickname)
                        .collect(Collectors.toSet());
                sharedController.startReconnectWindow(expected, 10_000L);
                System.out.println("[SERVER] Riconnessione: attesa di " + expected
                        + " entro 10s, altrimenti partita abortita.");
            }

            RmiServer rmiServer = new RmiServer(
                    RMI_PORT, sharedModel, sharedController, endGameService);
            rmiServer.start();

            SocketServer socketServer = new SocketServer(
                    SOCKET_PORT, sharedModel, sharedController, endGameService);
            socketServer.start();

        } catch (Exception e) {
            System.err.println("Errore critico durante l'avvio del server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}