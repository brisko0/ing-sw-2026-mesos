package it.polimi.ingsw.am56.controller;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.PersistenceManager;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.board.OfferTile;
import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;
import it.polimi.ingsw.am56.model.state.State;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Server-side entry point that turns client actions into operations on the {@link GameModel}.
 * It is the receiver of the command pattern: each {@code ClientRequest} ultimately calls one of the
 * methods below, which validate the request, mutate the model and let the model notify its observers.
 * <p>
 * All model access is guarded by synchronizing on the {@code model} instance (and the public mutators
 * are themselves {@code synchronized}), so concurrent requests coming from different connections are
 * serialized. The controller also owns the reconnection window used to decide, after a disconnection,
 * whether the game can resume or must be aborted.
 */
public class GameController {
    private final GameModel model;

    private Set<String> expectedReconnects;
    private final Set<String> reconnectedSoFar = new HashSet<>();
    private Timer reconnectTimer;

    /**
     * Creates a controller operating on the given game model.
     *
     * @param model the model this controller drives and synchronizes on
     */
    public GameController(GameModel model) {
        this.model = model;
    }

    /**
     * Opens a reconnection window after a disconnection: it records which players are expected to
     * reconnect and schedules a deadline. If, when the timeout elapses, not all expected players have
     * reconnected, the game is aborted via {@link #handleDisconnect()}.
     *
     * @param expected  the nicknames that must reconnect for the game to resume
     * @param timeoutMs how long (in milliseconds) to wait for all reconnections
     */
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

    /**
     * Aborts the current game following an unrecovered disconnection: it notifies the model (so the
     * remaining clients are told the game was aborted), deletes the saved game so it cannot be resumed,
     * and then shuts the server process down shortly afterwards.
     */
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

    /**
     * Tells whether the server may register a brand-new client connection right now. Connections are
     * accepted while the lobby is still filling (so players can join) and while a reconnection window
     * is open (so previously-connected players can come back after a server restart), but are refused
     * once the game is in progress. This stops outsiders from connecting mid-game, which otherwise let
     * them resume under an existing player's nickname, or abort the whole game for everyone else just
     * by closing their terminal even though they were never actually playing.
     *
     * @return {@code true} if a new connection may be registered, {@code false} if it must be refused
     */
    public boolean acceptsNewConnections() {
        synchronized (model) {
            boolean started = model.getGameSize() != -1
                    && model.getCurrentState().getState() != State.PLAYERS_FILLING;
            boolean reconnectWindowOpen = expectedReconnects != null;
            return !started || reconnectWindowOpen;
        }
    }

    /**
     * Requests the model to re-broadcast the current pre-lobby/lobby state, so a freshly connected
     * client can learn whether a game already exists and which nicknames and colors are taken.
     */
    public void checkLobbyState() {
        synchronized (model) {
            model.syncLobbyState();
        }
    }

    /**
     * Requests the model to re-broadcast the full game state (a snapshot), typically to bring a client
     * back in sync after connecting or reconnecting.
     */
    public void sendGameState() {
        synchronized (model) {
            model.syncGameState();
        }
    }

    /**
     * Creates a new game of the given size and adds its first player.
     *
     * @param nickname   the creator's nickname
     * @param totemColor the creator's chosen totem color
     * @param gameSize   the number of players the game will be played with
     * @throws InvalidActionException if the game cannot be initialized or the player cannot be added
     */
    public void createGame(String nickname, String totemColor, int gameSize) throws InvalidActionException {
        synchronized (model) {
            model.initializeGame(gameSize);
            model.addPlayer(new Player(nickname, totemColor));
            System.out.println("[CONTROLLER] Partita creata da " + nickname + " per " + gameSize + " giocatori.");
        }
    }

    /**
     * Adds another player to an already created game (joining its lobby).
     *
     * @param nickname   the joining player's nickname
     * @param totemColor the joining player's chosen totem color
     * @throws InvalidActionException if the player cannot be added (e.g. lobby full or name/color taken)
     */
    public void addPlayer(String nickname, String totemColor) throws InvalidActionException {
        synchronized (model) {
            model.addPlayer(new Player(nickname, totemColor));
            System.out.println("[CONTROLLER] Aggiunto giocatore: " + nickname);
        }
    }

    /**
     * Reconnects a player to a game in progress. The nickname must match one already in the game;
     * on success the player is re-synced with the current game state and, if a reconnection window is
     * open, counted among those who have come back (closing the window once everyone has reconnected).
     *
     * @param nickname the nickname of the reconnecting player
     * @throws InvalidActionException if no player with that nickname exists in the current game
     */
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

    /**
     * Places the given player's totem on the offer tile identified by its letter.
     *
     * @param nickname        the acting player's nickname
     * @param offerTileLetter the letter identifying the target offer tile
     * @throws InvalidActionException if the player is not found, the offer tile letter is invalid, or
     *                                the placement is not allowed by the current game rules/state
     */
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

    /**
     * Makes the given player pick up the card with the given id from the board.
     *
     * @param nickname the acting player's nickname
     * @param cardId   the id of the card to pick from the board
     * @throws InvalidActionException if the player is not found, no card has that id, or the pick is
     *                                not allowed by the current game rules/state
     */
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

    /**
     * Makes the given player decline the optional extra upper-card pick, advancing the game past the
     * {@code EXTRA_UPPER_CARD_PICKING} phase without taking another card.
     *
     * @param nickname the acting player's nickname
     * @throws InvalidActionException if the player is not found or the skip is not allowed in the
     *                                current game state
     */
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