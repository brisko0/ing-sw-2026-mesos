package it.polimi.ingsw.am56.view.TUI.controllers;

import it.polimi.ingsw.am56.network.message.classesDTO.PlayerDTO;
import it.polimi.ingsw.am56.network.message.request.AddPlayerRequest;
import it.polimi.ingsw.am56.network.message.request.CreateGameRequest;
import it.polimi.ingsw.am56.network.message.request.LobbyStateRequest;
import it.polimi.ingsw.am56.network.message.request.ReconnectRequest;
import it.polimi.ingsw.am56.network.message.update.GameSnapshotMessage;
import it.polimi.ingsw.am56.network.message.update.LobbyStateMessage;
import it.polimi.ingsw.am56.network.message.update.PreLobbyStateMessage;

import it.polimi.ingsw.am56.view.TUI.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TUI pre-lobby screen: it queries the server for the current state and then runs one of three flows
 * depending on that state — create a new game, join an open lobby, or <em>reconnect</em> to a game that
 * is already in progress (the persistence/resume case, e.g. after a server restart). It collects the
 * player's nickname (and, when creating/joining, the totem color), validating them against the
 * names/colors already taken, and sends the matching request. It then advances to the
 * {@link LobbyController} when a lobby state arrives, or straight to the {@link GameController} when a
 * game snapshot arrives (the reply to a successful reconnect).
 */
public class PreLobbyController extends BaseTUIController {

    private static final List<String> ALL_COLORS =
            Arrays.asList("BLACK", "CYAN", "RED", "WHITE", "YELLOW");

    private volatile boolean waitingForServer = true;
    private volatile boolean gameExists = false;
    private volatile boolean gameInProgress = false;
    private boolean joinRequestSent = false;
    private volatile List<String> takenNicknames = new ArrayList<>();
    private volatile List<String> takenColors = new ArrayList<>();

    /**
     * Creates the pre-lobby screen bound to the shared context.
     *
     * @param context the shared TUI context
     */
    public PreLobbyController(TUIContext context) {
        super(context);
    }

    /**
     * Requests the lobby state, waits for the server's reply, then runs the reconnect, join or create
     * flow: reconnect if a game is already in progress, join if a lobby is open, otherwise create.
     */
    @Override
    public void onEnter() {
        drawer.printMessage("Requesting game state from server...");
        context.getServerAdapter().sendRequest(new LobbyStateRequest());

        while (waitingForServer) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}
        }

        if (gameInProgress) {
            handleReconnect();
        } else if (gameExists) {
            handleJoinGame();
        } else {
            handleCreateGame();
        }
    }

    /**
     * Stores the pre-lobby state received from the server (whether a game exists and the taken
     * nicknames/colors) and releases the wait in {@link #onEnter()}.
     *
     * @param message the pre-lobby state
     */
    @Override
    public void visit(PreLobbyStateMessage message) {
        this.gameExists = message.isGameAlreadyCreated();
        this.gameInProgress = message.isGameInProgress();
        this.takenNicknames = message.takenNicknames();
        this.takenColors = message.takenColors();
        this.waitingForServer = false;
    }

    /**
     * Transitions to the lobby screen — but only once <em>this</em> client has actually joined.
     * {@link LobbyStateMessage} is broadcast to every connected client whenever <em>any</em> player
     * joins or creates the game, so a client still sitting in the pre-lobby (no nickname chosen yet,
     * not logged in) would otherwise be pulled into the game without having joined. We therefore
     * advance only after this client has actually sent its own join/create request and its nickname
     * appears among the lobby's players; otherwise the broadcast concerns other players and is ignored.
     *
     * @param message the lobby state
     */
    @Override
    public void visit(LobbyStateMessage message) {
        String localNickname = context.getLocalNickname();
        boolean localPlayerJoined = localNickname != null && message.players().stream()
                .anyMatch(p -> p.nickname().equalsIgnoreCase(localNickname));

        if (joinRequestSent && localPlayerJoined) {
            context.changeState(new LobbyController(context, message));
            return;
        }

        // Una partita esiste ora (creata da un altro client) ma questo client non vi ha ancora
        // aderito: aggiorna nickname/colori occupati e, se eravamo nel flusso di creazione,
        // segnala una sola volta che ormai si può solo unirsi (JOIN).
        this.takenNicknames = message.players().stream()
                .map(PlayerDTO::nickname).collect(Collectors.toList());
        this.takenColors = message.players().stream()
                .map(PlayerDTO::color).collect(Collectors.toList());

        if (!gameExists) {
            this.gameExists = true;
            drawer.printMessage("\n⚠  Una partita è appena stata creata da un altro giocatore: "
                    + "non puoi più crearne una. Completa il campo in corso per passare al JOIN.");
        }
    }

    /**
     * Reply to a successful {@link ReconnectRequest}: the server re-syncs the in-progress game by
     * broadcasting a snapshot, so we jump straight into the in-game screen.
     *
     * @param message the game snapshot
     */
    @Override
    public void visit(GameSnapshotMessage message) {
        context.changeState(new GameController(context, message));
    }

    /**
     * On disconnection, shows the reconnecting indicator (overriding the default exit behaviour).
     */
    @Override
    public void handleDisconnect() {
        drawer.printReconnecting();
    }

    /**
     * On reconnection, refreshes the pre-lobby state on <em>this same</em> controller — it must not
     * create a new {@code PreLobbyController}. The pre-lobby flow reads input with a blocking scanner
     * on the main thread: if a player is mid-prompt (e.g. choosing a totem color) when the server
     * drops, that flow is still parked on {@code System.in}. Re-entering a fresh controller here (on
     * the reconnect thread) would start a <em>second</em> flow reading the same scanner, so the two
     * would fight over the input and the original one would be orphaned, leaving the player stuck.
     * Instead we just re-request the state: {@link #visit(PreLobbyStateMessage)} updates the fields
     * (releasing {@link #onEnter()} if it is still waiting, and refreshing {@link #gameExists} /
     * {@link #gameInProgress}) and the already-running flow proceeds against the reconnected server.
     */
    @Override
    public void handleReconnected() {
        drawer.printReconnected();
        context.getServerAdapter().sendRequest(new LobbyStateRequest());
    }

    /**
     * Create-game flow: collects nickname, player count and color, then sends a create-game request.
     * <p>
     * Because another client can create the game while we sit at one of these (blocking) prompts,
     * after every input step we re-check whether a game now exists ({@link #gameExists}, raised by
     * {@link #visit(LobbyStateMessage)}). If so the create option is no longer valid, so we abandon
     * the create flow and switch to {@link #redirectToJoinGame()} — the user can only join now,
     * mirroring the GUI greying out "create game".
     */
    private void handleCreateGame() {
        drawer.printMenuHeader("CREATE GAME — No active game, start a new one");

        String nickname = chooseNickname();
        if (gameExists) { redirectToJoinGame(); return; }

        int size = inputReader.readIntWithRange("  Number of players (2-5):", 2, 5);
        if (gameExists) { redirectToJoinGame(); return; }

        String color = chooseColor();
        if (gameExists) { redirectToJoinGame(); return; }

        context.setLocalNickname(nickname);
        joinRequestSent = true;
        context.getServerAdapter().sendRequest(new CreateGameRequest(nickname, color, size));
    }

    /**
     * Abandons the create-game flow and runs the join-game flow instead, used when a game gets
     * created by another client while this client was still creating one.
     */
    private void redirectToJoinGame() {
        drawer.printMessage("Una partita è già attiva: passaggio a JOIN GAME.");
        handleJoinGame();
    }

    /**
     * Join-game flow: collects nickname and color for an existing lobby, then sends an add-player
     * request.
     */
    private void handleJoinGame() {
        drawer.printMenuHeader("JOIN GAME — A lobby is open");

        String nickname = chooseNickname();
        String color = chooseColor();

        context.setLocalNickname(nickname);
        joinRequestSent = true;
        context.getServerAdapter().sendRequest(new AddPlayerRequest(nickname, color));
    }

    /**
     * Reconnect flow (persistence / resume): a game is already in progress, so the player cannot create
     * or join — they can only rejoin with the <em>same nickname</em> they originally played with. It
     * collects that nickname and sends a {@link ReconnectRequest}; on success the server replies with a
     * game snapshot, handled by {@link #visit(GameSnapshotMessage)}. If the nickname does not belong to
     * the running game the server replies with an error (shown by the base controller) and the player
     * can relaunch to try again.
     */
    private void handleReconnect() {
        drawer.printMenuHeader("RECONNECT — A game is already in progress");
        drawer.printMessage("  Usa lo stesso nickname con cui avevi iniziato la partita.");

        String nickname;
        while (true) {
            nickname = inputReader.readString("  Enter your nickname:");
            if (!nickname.isEmpty()) break;
            drawer.printError("Nickname cannot be empty.");
        }

        context.setLocalNickname(nickname);
        context.getServerAdapter().sendRequest(new ReconnectRequest(nickname));
    }

    /**
     * Prompts repeatedly until the user enters a non-empty nickname that is not already taken.
     *
     * @return the chosen nickname
     */
    private String chooseNickname() {
        while (true) {
            String nickname = inputReader.readString("  Enter your nickname:");
            if (nickname.isEmpty()) {
                drawer.printError("Nickname cannot be empty.");
                continue;
            }
            if (takenNicknames.contains(nickname)) {
                drawer.printError("Nickname already taken. Pick another one.");
            } else {
                return nickname;
            }
        }
    }

    /**
     * Shows the palette and prompts repeatedly until the user picks a valid, not-yet-taken color.
     *
     * @return the chosen color (uppercase)
     */
    private String chooseColor() {
        printColorPalette();
        while (true) {
            String color = inputReader.readString("  Pick a color:").toUpperCase();
            if (!ALL_COLORS.contains(color)) {
                drawer.printError("Invalid color. Allowed: " + String.join(", ", ALL_COLORS));
                continue;
            }
            if (takenColors.contains(color)) {
                drawer.printError("Color already taken. Pick another one.");
            } else {
                return color;
            }
        }
    }

    /**
     * Render the available palette with each color word in its own ANSI shade;
     * colors already taken are shown dimmed.
     */
    private void printColorPalette() {
        StringBuilder line = new StringBuilder("  Available totem colors:  ");
        boolean first = true;
        for (String c : ALL_COLORS) {
            if (!first) line.append("   ");
            first = false;
            boolean taken = takenColors.contains(c);
            String sgr = taken ? "\033[2m" : drawer.colorSgrFor(c);
            line.append(sgr).append(c).append("\033[0m");
            if (taken) line.append(" (taken)");
        }
        drawer.printMessage(line.toString());
    }
}
