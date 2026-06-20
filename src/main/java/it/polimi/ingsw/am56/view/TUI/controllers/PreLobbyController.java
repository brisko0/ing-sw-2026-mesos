package it.polimi.ingsw.am56.view.TUI.controllers;

import it.polimi.ingsw.am56.network.message.request.AddPlayerRequest;
import it.polimi.ingsw.am56.network.message.request.CreateGameRequest;
import it.polimi.ingsw.am56.network.message.request.LobbyStateRequest;
import it.polimi.ingsw.am56.network.message.update.LobbyStateMessage;
import it.polimi.ingsw.am56.network.message.update.PreLobbyStateMessage;

import it.polimi.ingsw.am56.view.TUI.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TUI pre-lobby screen: it queries the server for the current state and then either creates a new game
 * or joins the existing one. It collects the player's nickname and totem color (validating them against
 * the names/colors already taken) and sends the matching request, then advances to the
 * {@link LobbyController} when the lobby state arrives.
 */
public class PreLobbyController extends BaseTUIController {

    private static final List<String> ALL_COLORS =
            Arrays.asList("BLACK", "CYAN", "RED", "WHITE", "YELLOW");

    private boolean waitingForServer = true;
    private boolean gameExists = false;
    private List<String> takenNicknames = new ArrayList<>();
    private List<String> takenColors = new ArrayList<>();

    /**
     * Creates the pre-lobby screen bound to the shared context.
     *
     * @param context the shared TUI context
     */
    public PreLobbyController(TUIContext context) {
        super(context);
    }

    /**
     * Requests the lobby state, waits for the server's reply, then runs the create-game or join-game
     * flow depending on whether a game already exists.
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

        if (gameExists) {
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
        this.takenNicknames = message.takenNicknames();
        this.takenColors = message.takenColors();
        this.waitingForServer = false;
    }

    /**
     * Once joined/created and the lobby state arrives, transitions to the lobby screen.
     *
     * @param message the lobby state
     */
    @Override
    public void visit(LobbyStateMessage message) {
        context.changeState(new LobbyController(context, message));
    }

    /**
     * On disconnection, shows the reconnecting indicator (overriding the default exit behaviour).
     */
    @Override
    public void handleDisconnect() {
        drawer.printReconnecting();
    }

    /**
     * On reconnection, shows the reconnected indicator and restarts the pre-lobby flow from scratch.
     */
    @Override
    public void handleReconnected() {
        drawer.printReconnected();
        context.changeState(new PreLobbyController(context));
    }

    /**
     * Create-game flow: collects nickname, player count and color, then sends a create-game request.
     */
    private void handleCreateGame() {
        drawer.printMenuHeader("CREATE GAME — No active game, start a new one");

        String nickname = chooseNickname();
        int size = inputReader.readIntWithRange("  Number of players (2-5):", 2, 5);
        String color = chooseColor();

        context.setLocalNickname(nickname);
        context.getServerAdapter().sendRequest(new CreateGameRequest(nickname, color, size));
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
        context.getServerAdapter().sendRequest(new AddPlayerRequest(nickname, color));
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
