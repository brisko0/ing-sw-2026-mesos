package it.polimi.ingsw.am56.view.TUI.controllers;

import it.polimi.ingsw.am56.network.message.classesDTO.PlayerDTO;
import it.polimi.ingsw.am56.network.message.request.GameSyncRequest;
import it.polimi.ingsw.am56.network.message.request.LobbyStateRequest;
import it.polimi.ingsw.am56.network.message.update.GameSnapshotMessage;
import it.polimi.ingsw.am56.network.message.update.LobbyStateMessage;

import it.polimi.ingsw.am56.view.TUI.*;

import java.util.List;

/**
 * TUI lobby screen: shows the players who have joined and the target game size while waiting for the
 * lobby to fill. It updates the display on each {@link LobbyStateMessage} and, once a
 * {@link GameSnapshotMessage} arrives, transitions to the in-game {@link GameController} screen.
 */
public class LobbyController extends BaseTUIController {

    private LobbyStateMessage currentLobbyState;
    private boolean syncRequested = false;

    /**
     * Creates the lobby screen with the lobby state known at the time of entry.
     *
     * @param context      the shared TUI context
     * @param initialState the lobby state to display first
     */
    public LobbyController(TUIContext context, LobbyStateMessage initialState) {
        super(context);
        this.currentLobbyState = initialState;
    }

    /**
     * Draws the lobby and, if it is already full, requests the initial game snapshot.
     */
    @Override
    public void onEnter() {
        drawLobby();
        maybeRequestSnapshot();
    }

    /**
     * Updates the lobby with new state, redraws it and possibly requests the initial snapshot.
     *
     * @param message the new lobby state
     */
    @Override
    public void visit(LobbyStateMessage message) {
        this.currentLobbyState = message;
        drawLobby();
        maybeRequestSnapshot();
    }

    /**
     * On receiving the initial game snapshot, transitions to the in-game screen.
     *
     * @param message the initial game snapshot
     */
    @Override
    public void visit(GameSnapshotMessage message) {
        context.changeState(new GameController(context, message));
    }

    /**
     * Once the lobby is full, the server auto-starts the game but does NOT spontaneously
     * emit a GameSnapshotMessage (only STATE_CHANGED + TURN_CHANGED). The TUI must explicitly
     * request a snapshot to receive the initial in-game state — same trick the GUI uses
     * in ViewGameController.setServerAdapter.
     *
     * Only the first player in the list (the lobby creator) sends the request: the server
     * fans out the resulting GameSnapshotMessage to every client, so one request is enough
     * for everyone — and avoids each client receiving a duplicate snapshot per request.
     */
    private void maybeRequestSnapshot() {
        if (syncRequested) return;
        if (currentLobbyState == null) return;
        if (currentLobbyState.players().size() != currentLobbyState.gameSize()) return;
        if (currentLobbyState.players().isEmpty()) return;
        String creator = currentLobbyState.players().get(0).nickname();
        if (!creator.equals(context.getLocalNickname())) {
            syncRequested = true; // suppress further attempts on this client
            return;
        }
        syncRequested = true;
        context.getServerAdapter().sendRequest(new GameSyncRequest());
    }

    /**
     * On disconnection, shows the reconnecting indicator (overriding the default exit behaviour).
     */
    @Override
    public void handleDisconnect() {
        drawer.printReconnecting();
    }

    /**
     * On reconnection, shows the reconnected indicator and re-requests the lobby state to refresh the
     * screen.
     */
    @Override
    public void handleReconnected() {
        drawer.printReconnected();
        syncRequested = false;
        context.getServerAdapter().sendRequest(new LobbyStateRequest());
    }

    /**
     * Clears the screen and renders the current lobby (player names, colors and target size).
     */
    private void drawLobby() {
        drawer.clearScreen();

        List<String> names = currentLobbyState.players().stream()
                .map(PlayerDTO::nickname)
                .toList();
        List<String> colors = currentLobbyState.players().stream()
                .map(PlayerDTO::color)
                .toList();

        drawer.printLobbyState(names, colors, currentLobbyState.gameSize());
    }
}