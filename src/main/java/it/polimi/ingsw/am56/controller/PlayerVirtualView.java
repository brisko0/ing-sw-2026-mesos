package it.polimi.ingsw.am56.controller;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.GameObserver;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.board.OfferTile;
import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.card.CardType;
import it.polimi.ingsw.am56.model.state.GameEvent;
import it.polimi.ingsw.am56.model.state.State;
import it.polimi.ingsw.am56.network.message.classesDTO.*;
import it.polimi.ingsw.am56.network.message.update.*;
import it.polimi.ingsw.am56.network.ClientConnection;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Server-side {@link GameObserver} that translates model events into network messages sent to a
 * client. It sits between the {@link GameModel} and a single {@link ClientConnection}: whenever the
 * model changes it fires a {@link GameEvent}, and this class reacts by building the right
 * {@code ServerMessage} (carrying the relevant DTO snapshot) and pushing it over the connection.
 * <p>
 * Events are routed through a dispatch map from {@link GameEvent} to a handler {@link Runnable}, so
 * {@link #onModelChanged(GameEvent)} simply looks the event up and runs its handler. Events without a
 * registered handler (e.g. {@code ERA_CHANGED}) intentionally produce no message, because their
 * information already travels inside another update such as a round-ended snapshot.
 * <p>
 * The helper methods extract the parts of the model that the client needs as serializable DTOs
 * (board rows as card-id lists, per-player stats, totem positions), keeping full model objects on the
 * server side.
 */
public class PlayerVirtualView implements GameObserver {
    private final GameModel model;
    private final ClientConnection networkConnection;

    // IL DISPATCHER: Associa ogni evento alla sua funzione di gestione
    private final Map<GameEvent, Runnable> eventHandlers = new EnumMap<>(GameEvent.class);

    private final EndGameService endGameService;

    /**
     * Creates a virtual view bound to one client connection.
     *
     * @param model             the game model to observe
     * @param networkConnection the connection to the client this view serves
     * @param endGameService    the service used to obtain the end-of-game message when the game ends
     */
    public PlayerVirtualView(GameModel model, ClientConnection networkConnection, EndGameService endGameService) {
        this.model = model;
        this.networkConnection = networkConnection;
        this.endGameService = endGameService;
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
        eventHandlers.put(GameEvent.EVENT_RESOLVED,      this::handleEventResolved);
        eventHandlers.put(GameEvent.GAME_ENDED,          this::handleGameEnded);
        eventHandlers.put(GameEvent.PLAYER_DISCONNECTED, this::handlePlayerDisconnected);
    }

    /**
     * Reacts to a model change by dispatching the event to its registered handler, which sends the
     * appropriate update message to the client. Events with no registered handler are ignored.
     *
     * @param event the event signalled by the model
     */
    @Override
    public void onModelChanged(GameEvent event) {
        Runnable handler = eventHandlers.get(event);
        if (handler != null) {
            handler.run();
        }
        // Gli eventi senza handler dedicato (es. ERA_CHANGED) non producono un messaggio:
        // l'informazione viaggia già dentro altri update (es. lo snapshot di ROUND_ENDED).
    }

    // =========================================================================
    // METODI HANDLER
    // =========================================================================

    /**
     * Sends the pre-lobby state: whether a game has been created and is already in progress, plus the
     * nicknames and totem colors already taken, so a connecting client knows what it may choose.
     */
    private void handleLobbyStateSync() {
        List<String> takenNicknames = model.getPlayers().stream().map(Player::getNickname).toList();
        List<String> takenColors    = model.getPlayers().stream().map(Player::getTotemColor).toList();

        boolean isGameCreated    = model.getGameSize() != -1;
        boolean isGameInProgress = isGameCreated
                && model.getCurrentState().getState() != State.PLAYERS_FILLING;

        networkConnection.sendMessage(
                new PreLobbyStateMessage(isGameCreated, isGameInProgress, takenNicknames, takenColors)
        );
    }

    /**
     * Sends the updated lobby state (current players and target game size) after a player joins.
     */
    private void handlePlayerAdded() {
        List<PlayerDTO> currentPlayers = model.getPlayers().stream()
                .map(p -> new PlayerDTO(p.getNickname(), p.getTotemColor()))
                .toList();
        networkConnection.sendMessage(new LobbyStateMessage(currentPlayers, model.getGameSize()));
    }

    /**
     * Sends a full game snapshot when the game starts: board, every player's stats, all totem
     * positions, the current player, the current state, era and round.
     */
    private void handleGameStarted() {
        BoardDTO boardDTO = extractBoardDTO();
        List<PlayerStatsDTO> statsDTO = model.getPlayers().stream().map(this::extractPlayerStats).toList();
        List<TotemPositionDTO> totemsDTO = extractAllTotemPositions();

        Player current = model.getCurrentPlayer();
        CurrentPlayerDTO currentDTO = new CurrentPlayerDTO(current.getNickname(), current.getTotemColor());
        GameStateDTO gameStateDTO = new GameStateDTO(model.getCurrentState().getState());

        GameSnapshotDTO snapshot = new GameSnapshotDTO(boardDTO, statsDTO, totemsDTO, currentDTO, gameStateDTO,
                model.getCurrentEra(), model.getCurrentRound());

        networkConnection.sendMessage(new GameSnapshotMessage(snapshot));
    }

    /**
     * Sends the updated positions of all totems after one has been placed.
     */
    private void handleTotemPlaced() {
        networkConnection.sendMessage(new TotemsStateMessage(extractAllTotemPositions()));
    }

    /**
     * On a turn change, sends the new current player's stats and then the new current player itself.
     */
    private void handleTurnChanged() {
        Player current = model.getCurrentPlayer();
        networkConnection.sendMessage(new PlayerStatsUpdateMessage(extractPlayerStats(current)));
        networkConnection.sendMessage(new CurrentPlayerUpdateMessage(
                new CurrentPlayerDTO(current.getNickname(), current.getTotemColor())
        ));
    }

    /**
     * Sends the new game state (turn phase) after the model's state changes.
     */
    private void handleStateChanged() {
        networkConnection.sendMessage(new GameStateUpdateMessage(
                new GameStateDTO(model.getCurrentState().getState())
        ));
    }

    /**
     * After a card is picked, sends the updated board (the rows changed) and the current player's
     * updated stats.
     */
    private void handleCardPicked() {
        networkConnection.sendMessage(new BoardUpdatedMessage(extractBoardDTO()));
        networkConnection.sendMessage(new PlayerStatsUpdateMessage(extractPlayerStats(model.getCurrentPlayer())));
    }

    /**
     * At the end of a round, sends an end-round snapshot (board, all players' stats, current player,
     * era and round) summarizing the situation after the round resolved.
     */
    private void handleRoundEnded() {
        BoardDTO boardDTO = extractBoardDTO();
        List<PlayerStatsDTO> statsDTO = model.getPlayers().stream().map(this::extractPlayerStats).toList();

        Player current = model.getCurrentPlayer();
        CurrentPlayerDTO currentDTO = new CurrentPlayerDTO(current.getNickname(), current.getTotemColor());

        EndRoundSnapshotDTO endSnap = new EndRoundSnapshotDTO(boardDTO, statsDTO, currentDTO,
                model.getCurrentEra(), model.getCurrentRound());

        networkConnection.sendMessage(new EndRoundUpdateMessage(endSnap));
    }

    /**
     * When totems are reset, sends the current player's updated stats and the refreshed totem
     * positions.
     */
    private void handleTotemReset() {
        networkConnection.sendMessage(new PlayerStatsUpdateMessage(extractPlayerStats(model.getCurrentPlayer())));
        networkConnection.sendMessage(new TotemsStateMessage(extractAllTotemPositions()));
    }

    /**
     * After an event card resolves, sends the id of the resolved event together with its per-player
     * outcomes (food and points deltas), built from the deltas recorded by the model.
     */
    private void handleEventResolved() {
        Map<String, int[]> deltas = model.getLastEventDeltas();
        List<EventOutcomeDTO> outcomes = new ArrayList<>();
        for (Player p : model.getPlayers()) {
            int[] d = deltas.get(p.getNickname());
            if (d != null) {
                outcomes.add(new EventOutcomeDTO(p.getNickname(), p.getTotemColor(), d[0], d[1]));
            }
        }
        networkConnection.sendMessage(new EventResolvedMessage(model.getLastResolvedEventId(), outcomes));
    }

    /**
     * Sends the end-of-game message obtained from the {@link EndGameService}; if it is not yet
     * available, logs an error and sends nothing.
     */
    private void handleGameEnded() {
        GameEndedMessage msg = endGameService.getEndGameMessage();
        if (msg != null) {
            networkConnection.sendMessage(msg);
        } else {
            System.err.println("[VIRTUAL VIEW] GameEndedMessage non disponibile al momento della notifica.");
        }
    }

    /**
     * Notifies the client that the game was aborted because a player disconnected.
     */
    private void handlePlayerDisconnected() {
        networkConnection.sendMessage(new GameAbortedMessage());
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    /**
     * Builds a {@link BoardDTO} from the current board, representing each row and the offer tiles by
     * id/letter rather than by full card objects. The event-card id list is the subset of the tribe
     * rows whose card type is {@link CardType#EVENT}, and the order-tile count is how many order tiles
     * remain.
     *
     * @return the board snapshot to send to the client
     */
    private BoardDTO extractBoardDTO() {
        List<Integer> upperBuilds = model.getBoard().getUpperRowBuildings().stream().map(Card::getCardId).toList();
        List<Integer> lowerBuilds = model.getBoard().getLowerRowBuildings().stream().map(Card::getCardId).toList();
        List<Integer> upperTribe = model.getBoard().getUpperRowCharEv().stream().map(Card::getCardId).toList();
        List<Integer> lowerTribe = model.getBoard().getLowerRowCharEv().stream().map(Card::getCardId).toList();
        List<String> offerLetters = model.getBoard().getOfferTiles().stream().map(OfferTile::getLetter).toList();
        int orderCount = model.getBoard().getOrderTiles().size();

        List<Integer> eventIds = new ArrayList<>();
        model.getBoard().getUpperRowCharEv().forEach(c -> {
            if (c.getCardType() == CardType.EVENT) eventIds.add(c.getCardId());
        });
        model.getBoard().getLowerRowCharEv().forEach(c -> {
            if (c.getCardType() == CardType.EVENT) eventIds.add(c.getCardId());
        });

        return new BoardDTO(upperBuilds, lowerBuilds, upperTribe, lowerTribe, offerLetters, orderCount, eventIds);
    }

    /**
     * Collects the positions of every placed totem: one entry per player on an order tile (located by
     * its order-tile id) and one per player on an offer tile (located by its letter).
     *
     * @return the totem positions to send to the client
     */
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

    /**
     * Builds the {@link PlayerStatsDTO} for the given player: food, points, each character type's card
     * ids, the sale/discount and shaman-star bonuses and the invention count. It also computes, from
     * the current game situation, whether the player may pick an upper/lower card (from the offer tile
     * they occupy, or — for upper only — during {@code EXTRA_UPPER_CARD_PICKING} when it is their
     * turn) and which board buildings they can currently afford (food at least the cost minus their
     * building discount), plus the ids of the buildings they already own.
     *
     * @param p the player whose stats to extract
     * @return the stats snapshot to send to the client
     */
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

        boolean canPickUpper = false;
        boolean canPickLower = false;
        OfferTile ot = model.getPlayerToOfferTile().get(p);

        if (ot != null) {
            canPickUpper = p.getPickedUpper() < ot.getUpperCards();
            canPickLower = p.getPickedLower() < ot.getLowerCards();
        } else if (model.getCurrentState().getState() == State.EXTRA_UPPER_CARD_PICKING
                && p.equals(model.getCurrentPlayer())) {
            canPickUpper = true;
        }

        List<Integer> affordableBuildings = new ArrayList<>();
        int availableFood = stats.getFood();
        int discount = stats.getBuildingSale();

        model.getBoard().getUpperRowBuildings().forEach(b -> {
            if (availableFood >= (b.getCost() - discount)) {
                affordableBuildings.add(b.getCardId());
            }
        });

        model.getBoard().getLowerRowBuildings().forEach(b -> {
            if (availableFood >= (b.getCost() - discount)) {
                affordableBuildings.add(b.getCardId());
            }
        });

        List<Integer> ownedBuildingCards = stats.getBuildingCards().stream()
                .map(Card::getCardId)
                .toList();

        return new PlayerStatsDTO(
                p.getNickname(), p.getTotemColor(), stats.getFood(), stats.getPoints(),
                collectorIds, hunterIds, builderIds, shamanIds, artistIds, inventorIds,
                stats.getFeedingSale(), stats.getBuildingSale(), stats.getShamanStars(), stats.getInventions().size(),
                canPickUpper, canPickLower,
                affordableBuildings, ownedBuildingCards
        );
    }
}