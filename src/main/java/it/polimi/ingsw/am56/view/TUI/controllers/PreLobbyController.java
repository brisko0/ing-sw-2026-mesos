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

public class PreLobbyController extends BaseTUIController {

    private static final List<String> ALL_COLORS =
            Arrays.asList("BLACK", "CYAN", "RED", "WHITE", "YELLOW");

    private boolean waitingForServer = true;
    private boolean gameExists = false;
    private List<String> takenNicknames = new ArrayList<>();
    private List<String> takenColors = new ArrayList<>();

    public PreLobbyController(TUIContext context) {
        super(context);
    }

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

    @Override
    public void visit(PreLobbyStateMessage message) {
        this.gameExists = message.isGameAlreadyCreated();
        this.takenNicknames = message.takenNicknames();
        this.takenColors = message.takenColors();
        this.waitingForServer = false;
    }

    @Override
    public void visit(LobbyStateMessage message) {
        context.changeState(new LobbyController(context, message));
    }

    @Override
    public void handleDisconnect() {
        drawer.printReconnecting();
    }

    @Override
    public void handleReconnected() {
        drawer.printReconnected();
        context.changeState(new PreLobbyController(context));
    }

    private void handleCreateGame() {
        drawer.printMenuHeader("CREATE GAME — No active game, start a new one");

        String nickname = chooseNickname();
        int size = inputReader.readIntWithRange("  Number of players (2-5):", 2, 5);
        String color = chooseColor();

        context.setLocalNickname(nickname);
        context.getServerAdapter().sendRequest(new CreateGameRequest(nickname, color, size));
    }

    private void handleJoinGame() {
        drawer.printMenuHeader("JOIN GAME — A lobby is open");

        String nickname = chooseNickname();
        String color = chooseColor();

        context.setLocalNickname(nickname);
        context.getServerAdapter().sendRequest(new AddPlayerRequest(nickname, color));
    }

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
