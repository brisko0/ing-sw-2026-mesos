package it.polimi.ingsw.am56.controller;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.PersistenceManager;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.board.OfferTile;
import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class GameController {
    private final GameModel model;

    private Set<String> expectedReconnects;
    private final Set<String> reconnectedSoFar = new HashSet<>();
    private Timer reconnectTimer;

    public GameController(GameModel model) {
        this.model = model;
    }

    public synchronized void startReconnectWindow(Set<String> expected, long timeoutMs) {
        this.expectedReconnects = new HashSet<>(expected);
        this.reconnectedSoFar.clear();
        this.reconnectTimer = new Timer("reconnect-window", true);
        this.reconnectTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (model) {
                    if (expectedReconnects != null
                            && !reconnectedSoFar.containsAll(expectedReconnects)) {
                        handleDisconnect();
                    }
                }
            }
        }, timeoutMs);
    }

    public void handleDisconnect() {
        synchronized (model) {
            model.notifyDisconnection();
            PersistenceManager.deleteSave();
        }
        new Thread(() -> {
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            System.exit(0);
        }).start();
    }

    public void checkLobbyState() {
        synchronized (model) {
            model.syncLobbyState();
        }
    }

    public void sendGameState() {
        synchronized (model) {
            model.syncGameState();
        }
    }

    public void createGame(String nickname, String totemColor, int gameSize) throws InvalidActionException {
        synchronized (model) {
            model.initializeGame(gameSize);
            model.addPlayer(new Player(nickname, totemColor));
            System.out.println("[CONTROLLER] Partita creata da " + nickname + " per " + gameSize + " giocatori.");
        }
    }

    public void addPlayer(String nickname, String totemColor) throws InvalidActionException {
        synchronized (model) {
            model.addPlayer(new Player(nickname, totemColor));
            System.out.println("[CONTROLLER] Aggiunto giocatore: " + nickname);
        }
    }

    public void reconnectPlayer(String nickname) throws InvalidActionException {
        synchronized (model) {
            if (!model.hasPlayer(nickname)) {
                throw new InvalidActionException(
                        "Nickname '" + nickname + "' non trovato nella partita in corso. " +
                                "Devi usare lo stesso nickname con cui hai iniziato la partita.");
            }
            System.out.println("[CONTROLLER] Riconnessione di: " + nickname);
            model.syncGameState();

            if (expectedReconnects != null) {
                reconnectedSoFar.add(nickname);
                if (reconnectedSoFar.containsAll(expectedReconnects)) {
                    reconnectTimer.cancel();
                    expectedReconnects = null;
                }
            }
        }
    }

    public void moveTotem(String nickname, String offerTileLetter) throws InvalidActionException {
        synchronized (model) {
            Player player = model.getPlayers().stream()
                    .filter(p -> p.getNickname().equals(nickname))
                    .findFirst()
                    .orElseThrow(() -> new InvalidActionException("Giocatore non trovato"));

            OfferTile tile = model.getBoard().getOfferTiles().stream()
                    .filter(t -> t.getLetter().equals(offerTileLetter))
                    .findFirst()
                    .orElseThrow(() -> new InvalidActionException("Tessera offerta non valida"));

            model.handleTotemPlacement(player, tile);
            System.out.println("[CONTROLLER] Totem mosso per " + nickname + " sulla tile " + offerTileLetter);
        }
    }

    public void cardPick(String nickname, int cardId) throws InvalidActionException {
        synchronized (model) {
            Player player = model.getPlayers().stream()
                    .filter(p -> p.getNickname().equals(nickname))
                    .findFirst()
                    .orElseThrow(() -> new InvalidActionException("Giocatore non trovato"));

            Card card = model.getBoard().getCardById(cardId);
            model.pickUpCard(player, card);
            System.out.println("[CONTROLLER] carta pescata per " + nickname + ", carta con id " + cardId);
        }
    }

    public void skipExtraPick(String nickname) throws InvalidActionException {
        synchronized (model) {
            Player player = model.getPlayers().stream()
                    .filter(p -> p.getNickname().equals(nickname))
                    .findFirst()
                    .orElseThrow(() -> new InvalidActionException("Giocatore non trovato"));

            model.skipExtraUpperCardPick(player);
            System.out.println("[CONTROLLER] " + nickname + " ha rinunciato alla pesca extra.");
        }
    }
}