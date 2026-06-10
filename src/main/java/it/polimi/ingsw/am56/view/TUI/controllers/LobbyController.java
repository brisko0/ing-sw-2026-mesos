package it.polimi.ingsw.am56.view.TUI.controllers;

import it.polimi.ingsw.am56.network.message.classesDTO.PlayerDTO;
import it.polimi.ingsw.am56.network.message.request.GameSyncRequest;
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

    private void maybeRequestSnapshot() {
        if (syncRequested) return;
        if (currentLobbyState == null) return;
        if (currentLobbyState.players().size() != currentLobbyState.gameSize()) return;
        if (currentLobbyState.players().isEmpty()) return;
        String creator = currentLobbyState.players().get(0).nickname();
        if (!creator.equals(context.getLocalNickname())) {
            syncRequested = true;
            return;
        }
        syncRequested = true;
        context.getServerAdapter().sendRequest(new GameSyncRequest());
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