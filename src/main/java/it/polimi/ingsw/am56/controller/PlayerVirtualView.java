package it.polimi.ingsw.am56.controller;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.GameObserver;
import it.polimi.ingsw.am56.model.GameEvent;
import it.polimi.ingsw.am56.network.message.classesDTO.*;
import it.polimi.ingsw.am56.network.message.update.*;
import it.polimi.ingsw.am56.network.ClientConnection;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PlayerVirtualView implements GameObserver {
    private final GameModel model;
    private final ClientConnection networkConnection;

    private final Map<GameEvent, Runnable> eventHandlers = new EnumMap<>(GameEvent.class);

    public PlayerVirtualView(GameModel model, ClientConnection networkConnection) {
        this.model = model;
        this.networkConnection = networkConnection;
        registerHandlers();
    }

    private void registerHandlers() {
        eventHandlers.put(GameEvent.LOBBY_STATE_SYNC, this::handleLobbyStateSync);
        eventHandlers.put(GameEvent.PLAYER_ADDED,     this::handlePlayerAdded);
    }

    @Override
    public void onModelChanged(GameEvent event) {
        // Magia: niente più switch! Cerchiamo l'azione nella mappa e la eseguiamo.
        Runnable handler = eventHandlers.get(event);
        if (handler != null) {
            handler.run();
        } else {
            // Evento non mappato o ignorato di proposito
            networkConnection.sendMessage(new TemporaryDefaultMessage());
        }
    }

    // =========================================================================
    // METODI HANDLER (Un metodo per ogni evento, pulito e incapsulato)
    // =========================================================================

    private void handleLobbyStateSync() {
        networkConnection.sendMessage(new PreLobbyStateMessage(model.getGameSize() != -1));
    }

    private void handlePlayerAdded() {
        List<PlayerDTO> currentPlayers = model.getPlayers().stream()
                .map(p -> new PlayerDTO(p.getNickname(), p.getTotemColor()))
                .toList();
        networkConnection.sendMessage(new LobbyStateMessage(currentPlayers, model.getGameSize()));
    }
}