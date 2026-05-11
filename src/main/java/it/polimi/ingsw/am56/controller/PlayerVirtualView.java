package it.polimi.ingsw.am56.controller;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.GameObserver;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.board.OfferTile;
import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.GameEvent;
import it.polimi.ingsw.am56.network.message.classesDTO.*;
import it.polimi.ingsw.am56.network.message.update.*;
import it.polimi.ingsw.am56.network.ClientConnection;

import java.util.ArrayList;
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
        eventHandlers.put(GameEvent.GAME_STARTED,     this::handleGameStarted);
        eventHandlers.put(GameEvent.TOTEM_PLACED,     this::handleTotemPlaced);
        eventHandlers.put(GameEvent.TURN_CHANGED,     this::handleTurnChanged);
        eventHandlers.put(GameEvent.STATE_CHANGED,    this::handleStateChanged);
        eventHandlers.put(GameEvent.CARD_PICKED,      this::handleCardPicked);
        eventHandlers.put(GameEvent.ROUND_ENDED,      this::handleRoundEnded);
        eventHandlers.put(GameEvent.TOTEM_RESET,      this::handleTotemReset);
        eventHandlers.put(GameEvent.EVENT_RESOLVED,   this::handleEventResolved);
    }

    @Override
    public void onModelChanged(GameEvent event) {
        Runnable handler = eventHandlers.get(event);
        if (handler != null) {
            handler.run();
        } else {
            networkConnection.sendMessage(new TemporaryDefaultMessage());
        }
    }

    // =========================================================================
    // handler
    // =========================================================================

    private void handleLobbyStateSync() {
        List<String> takenNicknames = model.getPlayers().stream().map(Player::getNickname).toList();
        List<String> takenColors = model.getPlayers().stream().map(Player::getTotemColor).toList();
        networkConnection.sendMessage(new PreLobbyStateMessage(model.getGameSize() != -1, takenNicknames, takenColors));
    }

    private void handlePlayerAdded() {
        List<PlayerDTO> currentPlayers = model.getPlayers().stream()
                .map(p -> new PlayerDTO(p.getNickname(), p.getTotemColor()))
                .toList();
        networkConnection.sendMessage(new LobbyStateMessage(currentPlayers, model.getGameSize()));
    }

    private void handleGameStarted() {
        BoardDTO boardDTO = extractBoardDTO();
        List<PlayerStatsDTO> statsDTO = model.getPlayers().stream().map(this::extractPlayerStats).toList();
        List<TotemPositionDTO> totemsDTO = extractAllTotemPositions();

        Player current = model.getCurrentPlayer();
        CurrentPlayerDTO currentDTO = new CurrentPlayerDTO(current.getNickname(), current.getTotemColor());
        GameStateDTO gameStateDTO = new GameStateDTO(model.getCurrentState().toString());

        GameSnapshotDTO snapshot = new GameSnapshotDTO(boardDTO, statsDTO, totemsDTO, currentDTO, gameStateDTO,
                model.getCurrentEra(), model.getCurrentRound());

        networkConnection.sendMessage(new GameSnapshotMessage(snapshot));
    }

    private void handleTotemPlaced() {
        networkConnection.sendMessage(new TotemsStateMessage(extractAllTotemPositions()));
    }

    private void handleTurnChanged() {
        Player current = model.getCurrentPlayer();
        networkConnection.sendMessage(new CurrentPlayerUpdateMessage(
                new CurrentPlayerDTO(current.getNickname(), current.getTotemColor())
        ));
    }

    private void handleStateChanged() {
        networkConnection.sendMessage(new GameStateUpdateMessage(
                new GameStateDTO(model.getCurrentState().toString())
        ));
    }

    private void handleCardPicked() {
        networkConnection.sendMessage(new BoardUpdatedMessage(extractBoardDTO()));
        networkConnection.sendMessage(new PlayerStatsUpdateMessage(extractPlayerStats(model.getCurrentPlayer())));
    }

    private void handleRoundEnded() {
        BoardDTO boardDTO = extractBoardDTO();
        List<PlayerStatsDTO> statsDTO = model.getPlayers().stream().map(this::extractPlayerStats).toList();

        Player current = model.getCurrentPlayer();
        CurrentPlayerDTO currentDTO = new CurrentPlayerDTO(current.getNickname(), current.getTotemColor());

        EndRoundSnapshotDTO endSnap = new EndRoundSnapshotDTO(boardDTO, statsDTO, currentDTO,
                model.getCurrentEra(), model.getCurrentRound());

        networkConnection.sendMessage(new EndRoundUpdateMessage(endSnap));
    }

    private void handleTotemReset() {
        networkConnection.sendMessage(new PlayerStatsUpdateMessage(extractPlayerStats(model.getCurrentPlayer())));
        networkConnection.sendMessage(new TotemsStateMessage(extractAllTotemPositions()));
    }

    private void handleEventResolved() {
        networkConnection.sendMessage(new EventResolvedMessage(
                model.getBoard().getLowerRowCharEv().getFirst().getCardId()
        ));
    }

    // =========================================================================
    // metodi helper per estrarre dati
    // =========================================================================

    private BoardDTO extractBoardDTO() {
        List<Integer> upperBuilds = model.getBoard().getUpperRowBuildings().stream().map(Card::getCardId).toList();
        List<Integer> lowerBuilds = model.getBoard().getLowerRowBuildings().stream().map(Card::getCardId).toList();
        List<Integer> upperTribe = model.getBoard().getUpperRowCharEv().stream().map(Card::getCardId).toList();
        List<Integer> lowerTribe = model.getBoard().getLowerRowCharEv().stream().map(Card::getCardId).toList();
        List<String> offerLetters = model.getBoard().getOfferTiles().stream().map(OfferTile::getLetter).toList();
        int orderCount = model.getBoard().getOrderTiles().size();

        return new BoardDTO(upperBuilds, lowerBuilds, upperTribe, lowerTribe, offerLetters, orderCount);
    }

    private List<TotemPositionDTO> extractAllTotemPositions() {
        List<TotemPositionDTO> totemsDTO = new ArrayList<>();
        model.getPlayerToOrderTile().forEach((player, orderTile) ->
                totemsDTO.add(new TotemPositionDTO(player.getTotemColor(), "ORDER", String.valueOf(orderTile.getOrderTileId())))
        );
        model.getPlayerToOfferTile().forEach((player, offerTile) ->
                totemsDTO.add(new TotemPositionDTO(player.getTotemColor(), "OFFER", offerTile.getLetter()))
        );
        return totemsDTO;
    }

    private PlayerStatsDTO extractPlayerStats(Player p) {
        var stats = p.getStats();

        List<Integer> collectorIds = new ArrayList<>();
        List<Integer> hunterIds = new ArrayList<>();
        List<Integer> builderIds = new ArrayList<>();
        List<Integer> shamanIds = new ArrayList<>();
        List<Integer> artistIds = new ArrayList<>();
        List<Integer> inventorIds = new ArrayList<>();

        for (var card : stats.getCharacterCards()) {
            int id = card.getCardId();
            switch (card.getCharacterType()) {
                case COLLECTOR -> collectorIds.add(id);
                case HUNTER -> hunterIds.add(id);
                case BUILDER -> builderIds.add(id);
                case SHAMAN -> shamanIds.add(id);
                case ARTIST -> artistIds.add(id);
                case INVENTOR -> inventorIds.add(id);
            }
        }

        return new PlayerStatsDTO(
                p.getNickname(), p.getTotemColor(), stats.getFood(), stats.getPoints(),
                collectorIds, hunterIds, builderIds, shamanIds, artistIds, inventorIds,
                stats.getFeedingSale(), stats.getBuildingSale(), stats.getShamanStars(), stats.getInventions().size()
        );
    }
}