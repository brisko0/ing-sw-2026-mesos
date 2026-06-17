package it.polimi.ingsw.am56.view.TUI.controllers;

import it.polimi.ingsw.am56.network.message.classesDTO.PlayerDTO;
import it.polimi.ingsw.am56.network.message.request.GameSyncRequest;
import it.polimi.ingsw.am56.network.message.request.LobbyStateRequest;
import it.polimi.ingsw.am56.network.message.update.GameSnapshotMessage;
import it.polimi.ingsw.am56.network.message.update.LobbyStateMessage;

import it.polimi.ingsw.am56.view.TUI.*;

import java.util.List;

public class LobbyController extends BaseTUIController {

    private LobbyStateMessage currentLobbyState;
    private boolean syncRequested = false;

    public LobbyController(TUIContext context, LobbyStateMessage initialState) {
        super(context);
        this.currentLobbyState = initialState;
    }

    @Override
    public void onEnter() {
        drawLobby();
        maybeRequestSnapshot();
    }

    @Override
    public void visit(LobbyStateMessage message) {
        this.currentLobbyState = message;
        drawLobby();
        maybeRequestSnapshot();
    }

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

    @Override
    public void handleDisconnect() {
        drawer.printReconnecting();
    }

    @Override
    public void handleReconnected() {
        drawer.printReconnected();
        syncRequested = false;
        context.getServerAdapter().sendRequest(new LobbyStateRequest());
    }

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