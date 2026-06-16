package it.polimi.ingsw.am56.model;

import it.polimi.ingsw.am56.model.board.Board;
import it.polimi.ingsw.am56.model.board.OfferTile;
import it.polimi.ingsw.am56.model.board.OrderTile;
import it.polimi.ingsw.am56.model.card.*;
import it.polimi.ingsw.am56.model.card.buildingCard.BuildingCard;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterType;
import it.polimi.ingsw.am56.model.card.eventCard.EvType;
import it.polimi.ingsw.am56.model.card.eventCard.EventCard;
import it.polimi.ingsw.am56.model.factory.board.BoardFactory;
import it.polimi.ingsw.am56.model.factory.card.EventCardFactory;
import it.polimi.ingsw.am56.model.factory.deck.BuildingDeckFactory;
import it.polimi.ingsw.am56.model.factory.deck.CharacterDeckFactory;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;
import it.polimi.ingsw.am56.model.state.GameEvent;
import it.polimi.ingsw.am56.model.state.GameState;
import it.polimi.ingsw.am56.model.state.PlayersFilling;
import it.polimi.ingsw.am56.model.state.State;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

/**
 * Aggregate root of the game's model layer. Holds the entire mutable game state — the
 * {@link Board}, the per-era {@link Deck}s and {@link BuildingDeck}s, the player list and
 * their associations to {@link OrderTile} / {@link OfferTile}, plus the era / round counters
 * and the active {@link GameState} — and exposes the entry points the controller uses to
 * drive play: {@link #addPlayer}, {@link #handleTotemPlacement}, {@link #pickUpCard}.
 * <p>
 * Each player action is delegated to the current {@link GameState}, which validates it and
 * calls back into the {@code execute*} helpers here to mutate state. After every mutation the
 * model fires a {@link GameEvent} to its registered {@link GameObserver}s and snapshots
 * itself via {@link PersistenceManager}, so the server can rebuild a live game after a crash.
 * <p>
 * Serialisation note: the observer list is {@code transient} — observers are server-side
 * subscriptions that do not survive a restart and are re-registered by the server on load.
 */
public class GameModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private GameState state;
    private int gameSize;
    private Map<Integer, Deck> eraToCharEvdeck;
    private Map<Integer, BuildingDeck> eraToBuildingsdeck;
    private Board board;
    private List<Player> players;
    private Map<Player, OrderTile> playerToOrderTile;
    private Map<Player, OfferTile> playerToOfferTile;
    private int currentEra;
    private int currentRound;
    private Player currentPlayer;

    // Transient: never serialized, re-registered by the server after load
    private transient List<GameObserver> observers;

    /**
     * Creates an empty model in the lobby phase. The board and decks are populated later by
     * {@link #initializeGame(int)} once the player count is known.
     */
    public GameModel() {
        this.gameSize = -1;
        this.eraToCharEvdeck = new HashMap<>();
        this.eraToBuildingsdeck = new HashMap<>();
        this.board = null;
        this.players = new ArrayList<Player>();
        this.playerToOrderTile = new HashMap<>();
        this.playerToOfferTile = new HashMap<>();
        this.currentEra = 1;
        this.currentRound = 1;
        this.currentPlayer = null;
        this.observers = new ArrayList<>();
        this.state = new PlayersFilling(this);
    }

    /**
     * Custom deserialisation hook. Restores the transient {@link #observers} list to an empty
     * collection so the server can re-register its subscribers after loading a saved game.
     */
    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Reinitialize the transient field that was not saved
        this.observers = new ArrayList<>();
    }

    // =========================================================================
    // PERSISTENCE
    // =========================================================================

    /**
     * Snapshots this model to disk via {@link PersistenceManager#save(GameModel)}. Called
     * automatically after most mutations.
     */
    public void saveGame() {
        PersistenceManager.save(this);
    }

    /**
     * Returns whether a player with the given nickname is already in the game (case-insensitive).
     *
     * @param nickname the nickname to look for
     * @return {@code true} if any player matches
     */
    public boolean hasPlayer(String nickname) {
        return players.stream().anyMatch(p -> p.getNickname().equalsIgnoreCase(nickname));
    }

    // =========================================================================
    // OBSERVER
    // =========================================================================

    /**
     * Registers a new observer. No-op if {@code observer} is already subscribed.
     *
     * @param observer the observer to register
     */
    public void addObserver(GameObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Unregisters an observer. No-op if it was not subscribed.
     *
     * @param observer the observer to remove
     */
    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }

    /**
     * Broadcasts a {@link GameEvent#PLAYER_DISCONNECTED} to every observer. Called by the
     * server when a client drops.
     */
    public void notifyDisconnection() {
        notifyObservers(GameEvent.PLAYER_DISCONNECTED);
    }

    private void notifyObservers(GameEvent event) {
        for (GameObserver observer : observers) {
            observer.onModelChanged(event);
        }
    }

    // =========================================================================
    // GETTERS
    // =========================================================================

    /**
     * Returns the active state-machine state.
     *
     * @return the current {@link GameState}
     */
    public GameState getCurrentState() {
        return state;
    }

    /**
     * Returns the configured player count for this game (2–5), or {@code -1} if
     * {@link #initializeGame(int)} has not yet been called.
     *
     * @return the player count
     */
    public int getGameSize() {
        return gameSize;
    }

    /**
     * Returns the era → character/event deck map. Each era keeps its own mixed deck of
     * character and event cards.
     *
     * @return the era-to-deck map
     */
    public Map<Integer, Deck> getEraToCharEvdeck() {
        return eraToCharEvdeck;
    }

    /**
     * Returns the era → building deck map.
     *
     * @return the era-to-building-deck map
     */
    public Map<Integer, BuildingDeck> getEraToBuildingsdeck() {
        return eraToBuildingsdeck;
    }

    /**
     * Returns the central {@link Board}.
     *
     * @return the board, or {@code null} before {@link #initializeGame(int)} has run
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Returns the live, mutable list of players in seat order (join order).
     *
     * @return the players list
     */
    public List<Player> getPlayers() {
        return players;
    }

    /**
     * Returns the live map of players to their current {@link OrderTile}. Entries are removed
     * when a player places their totem on an offer tile and re-added by
     * {@link #resetTotemToOrder()} once card picking is done for that turn.
     *
     * @return the player-to-order-tile map
     */
    public Map<Player, OrderTile> getPlayerToOrderTile() {
        return playerToOrderTile;
    }

    /**
     * Returns the live map of players to the {@link OfferTile} they are currently resolving.
     *
     * @return the player-to-offer-tile map
     */
    public Map<Player, OfferTile> getPlayerToOfferTile() {
        return playerToOfferTile;
    }

    /**
     * Returns the current era (1, 2 or 3).
     *
     * @return the era
     */
    public int getCurrentEra() {
        return currentEra;
    }

    /**
     * Returns the player whose turn it is.
     *
     * @return the current player, or {@code null} in the lobby phase
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Returns the current round number (1-indexed; the game ends after round 11).
     *
     * @return the round
     */
    public int getCurrentRound() {
        return currentRound;
    }

    // =========================================================================
    // STATE MACHINE
    // =========================================================================

    /**
     * Installs a new state. Fires {@link GameEvent#STATE_CHANGED}, runs the new state's
     * {@link GameState#onEntry()} hook, and then snapshots the game — unless the new state is
     * {@link State#GAME_ENDING}, since the post-game save would be deleted by
     * {@link #endGameActions()} anyway.
     *
     * @param state the new state
     */
    public void setState(GameState state) {
        this.state = state;
        notifyObservers(GameEvent.STATE_CHANGED);
        state.onEntry();
        if (state.getState() != State.GAME_ENDING) {
            saveGame();
        }
    }

    /**
     * Sets the active player directly. Most callers should use {@link #setCurrentPlayerOrder()}
     * or {@link #setCurrentPlayerOffer()} instead, which compute the next player from the
     * appropriate ordering.
     *
     * @param player the new active player
     */
    public void setCurrentPlayer(Player player) {
        this.currentPlayer = player;
    }

    /**
     * Broadcasts a {@link GameEvent#LOBBY_STATE_SYNC} so clients can hydrate from the current
     * lobby state.
     */
    public void syncLobbyState() {
        notifyObservers(GameEvent.LOBBY_STATE_SYNC);
    }

    /**
     * Broadcasts a {@link GameEvent#GAME_STARTED} so clients can transition out of the lobby UI.
     */
    public void syncGameState() {
        notifyObservers(GameEvent.GAME_STARTED);
    }

    /**
     * Delegates a join request to the active {@link GameState}. Only legal in
     * {@link PlayersFilling}; rejected in every other phase.
     *
     * @param p the player attempting to join
     * @throws InvalidActionException if joining is not legal in the current phase
     */
    public void addPlayer(Player p) throws InvalidActionException {
        state.addPlayer(p);
    }

    /**
     * Appends a validated player to the lobby. Called by {@link PlayersFilling} after
     * uniqueness checks; fires {@link GameEvent#PLAYER_ADDED} and snapshots.
     *
     * @param p the player to add
     */
    public void executeAddPlayer(Player p) {
        players.add(p);
        notifyObservers(GameEvent.PLAYER_ADDED);
        saveGame();
    }

    /**
     * Builds the board and the three eras' decks for the given player count, fills the lower
     * character row and the upper rows for era 1, then snapshots and broadcasts a lobby sync.
     * Called by the controller once the game-size choice is committed.
     *
     * @param gameSize player count (must be in [2, 5])
     * @throws InvalidActionException if {@code gameSize} is out of range
     */
    public void initializeGame(int gameSize) throws InvalidActionException {
        if (gameSize < 2 || gameSize > 5) {
            throw new InvalidActionException("Numero di giocatori non valido");
        }

        this.gameSize = gameSize;

        board = BoardFactory.createBoard(gameSize);

        EventCardFactory eventCardFactory = new EventCardFactory();
        CharacterDeckFactory characterDeckFactory = new CharacterDeckFactory();
        BuildingDeckFactory buildingDeckfactory = new BuildingDeckFactory();

        Deck era1deck = characterDeckFactory.createDeck(1, gameSize);
        board.fillLowerChar(gameSize, era1deck);
        era1deck.getRemainingCards().addAll(eventCardFactory.createCards(1, gameSize));
        era1deck.shuffle();
        eraToCharEvdeck.put(1, era1deck);
        eraToBuildingsdeck.put(1, buildingDeckfactory.createDeck(1, gameSize));

        for (int era = 2; era <= 3; era++) {
            Deck charEvDeck = characterDeckFactory.createDeck(era, gameSize);
            charEvDeck.getRemainingCards().addAll(eventCardFactory.createCards(era, gameSize));
            charEvDeck.shuffle();
            if (era == 3) {
                moveFinalEventsLast(charEvDeck);
            }
            eraToCharEvdeck.put(era, charEvDeck);
            eraToBuildingsdeck.put(era, buildingDeckfactory.createDeck(era, gameSize));
        }

        board.fillUpperCharEv(gameSize, eraToCharEvdeck.get(currentEra));
        board.fillUpperBuildings(gameSize, eraToBuildingsdeck.get(currentEra), currentEra);

        saveGame();
        syncLobbyState();
    }

    private void moveFinalEventsLast(Deck deck) {
        List<Card> finalEvents = deck.getRemainingCards().stream()
                .filter(card -> card.getCardType() == CardType.EVENT)
                .map(card -> (EventCard) card)
                .filter(EventCard::isFinal)
                .map(eventCard -> (Card) eventCard)
                .toList();

        deck.getRemainingCards().removeIf(card ->
                card.getCardType() == CardType.EVENT
                        && ((EventCard) card).isFinal()
        );

        deck.getRemainingCards().addAll(finalEvents);
    }

    // =========================================================================
    // TURN MANAGEMENT
    // =========================================================================

    /**
     * Selects the next active player by order-tile id, among those who still need to place
     * their totem. Fires {@link GameEvent#TURN_CHANGED} and snapshots.
     */
    public void setCurrentPlayerOrder() {
        List<Player> needingToPlace = players.stream()
                .filter(p -> !playerToOfferTile.containsKey(p))
                .toList();

        this.currentPlayer = needingToPlace.stream()
                .min(Comparator.comparingInt(p -> playerToOrderTile.get(p).getOrderTileId()))
                .orElse(null);

        saveGame();
        notifyObservers(GameEvent.TURN_CHANGED);
    }

    /**
     * Selects the next active player by offer-tile letter, among those who still hold an
     * offer tile. Fires {@link GameEvent#TURN_CHANGED} and snapshots.
     */
    public void setCurrentPlayerOffer() {
        List<Player> needingToAct = players.stream()
                .filter(p -> !playerToOrderTile.containsKey(p))
                .toList();

        this.currentPlayer = needingToAct.stream()
                .min(Comparator.comparing(p -> playerToOfferTile.get(p).getLetter()))
                .orElse(null);

        saveGame();
        notifyObservers(GameEvent.TURN_CHANGED);
    }

    // =========================================================================
    // GAME ACTIONS
    // =========================================================================

    /**
     * Delegates a totem-placement request to the active {@link GameState}.
     *
     * @param p the player placing the totem
     * @param t the offer tile being claimed
     * @throws InvalidActionException if the placement is not legal in the current phase
     */
    public void handleTotemPlacement(Player p, OfferTile t) throws InvalidActionException {
        state.handleTotemPlacement(p, t);
    }

    /**
     * Performs a validated totem placement: binds the player to the offer tile, frees the
     * order tile they were on, then fires {@link GameEvent#TOTEM_PLACED} and snapshots.
     *
     * @param p the player placing the totem
     * @param t the offer tile being claimed
     */
    public void executeTotemPlacement(Player p, OfferTile t) {
        playerToOfferTile.put(p, t);
        t.setIsFree(false);

        OrderTile oldOrderTile = playerToOrderTile.remove(p);
        oldOrderTile.setIsFree(true);

        saveGame();
        notifyObservers(GameEvent.TOTEM_PLACED);
    }

    /**
     * Reports whether the current player can still make a useful pick. A row is usable when
     * the offer tile's quota for that row is not yet exhausted <em>and</em> the row contains
     * at least one card the player can take — i.e. a character card, or a building
     * the player can afford after applying their {@code buildingSale} discount.
     *
     * @return {@code true} if at least one pick remains available
     */
    public boolean checkCanPick() {
        OfferTile ot = playerToOfferTile.get(currentPlayer);

        boolean hasUpperSlot = currentPlayer.getPickedUpper() < ot.getUpperCards();
        boolean canPickUpper = hasUpperSlot && (
                board.getUpperRowCharEv().stream()
                        .anyMatch(c -> c.getCardType() == CardType.CHARACTER) ||
                        board.getUpperRowBuildings().stream()
                                .anyMatch(b -> currentPlayer.getStats().getFood() >= Math.max(0, b.getCost() - currentPlayer.getStats().getBuildingSale()))
        );

        boolean hasLowerSlot = currentPlayer.getPickedLower() < ot.getLowerCards();
        boolean canPickLower = hasLowerSlot && (
                board.getLowerRowCharEv().stream()
                        .anyMatch(c -> c.getCardType() == CardType.CHARACTER) ||
                        board.getLowerRowBuildings().stream()
                                .anyMatch(b -> currentPlayer.getStats().getFood() >= Math.max(0, b.getCost() - currentPlayer.getStats().getBuildingSale()))
        );

        return canPickUpper || canPickLower;
    }

    /**
     * Delegates a card-pick request to the active {@link GameState}.
     *
     * @param p the player picking the card
     * @param c the card being picked
     * @throws InvalidActionException if the pick is not legal in the current phase
     */
    public void pickUpCard(Player p, Card c) throws InvalidActionException {
        state.pickUpCard(p, c);
    }

    /**
     * Performs a validated card pick: hands the card to the player, increments the row pick
     * counter, removes the card from the board, then resolves its primary effect and any
     * building reactions. Fires {@link GameEvent#CARD_PICKED} and snapshots.
     *
     * @param p       the player picking the card
     * @param c       the card being picked
     * @param isUpper {@code true} if the card came from the upper row, {@code false} for the lower
     * @throws InvalidActionException if the card refuses to be added to the player (e.g. an event)
     */
    public void executeCardPick(Player p, Card c, boolean isUpper) throws InvalidActionException {
        c.addCardToPlayer(p);
        if (isUpper) {
            p.setPickedUpper(p.getPickedUpper() + 1);
        } else {
            p.setPickedLower(p.getPickedLower() + 1);
        }
        board.removeCard(c);
        c.resolveEffect(this);
        c.resolveBuildingsEffects(this);

        saveGame();
        notifyObservers(GameEvent.CARD_PICKED);
    }

    /**
     * Closes out the current player's card-picking turn. Releases their offer tile, places
     * them on the first free order tile (which fixes their seat for the next round), applies
     * the offer-tile food bonus and order-tile bonuses, lets the player's buildings react via
     * {@code BuildingCard.onOwnTurnEnding}, and resets the row pick counters. Fires
     * {@link GameEvent#TOTEM_RESET} and snapshots.
     */
    public void resetTotemToOrder() {
        OfferTile oldOffer = playerToOfferTile.remove(currentPlayer);
        oldOffer.setIsFree(true);

        OrderTile firstFreeOrder = board.getOrderTiles().stream()
                .filter(OrderTile::isFree)
                .findFirst()
                .orElse(null);

        currentPlayer.getStats().addFood(oldOffer.getFoodBonus());

        playerToOrderTile.put(currentPlayer, firstFreeOrder);
        firstFreeOrder.setIsFree(false);

        applyOrderBonuses();

        currentPlayer.setPickedUpper(0);
        currentPlayer.setPickedLower(0);

        saveGame();
        notifyObservers(GameEvent.TOTEM_RESET);
    }

    private void applyOrderBonuses() {
        int foodEffect = playerToOrderTile.get(currentPlayer).getFoodEffect();
        int pointsEffect = playerToOrderTile.get(currentPlayer).getPointsEffect();
        if (foodEffect >= 0) {
            currentPlayer.getStats().addFood(foodEffect);
        } else {
            if (currentPlayer.getStats().getFood() > 0) {
                currentPlayer.getStats().addFood(foodEffect);
            } else {
                currentPlayer.getStats().addPoints(pointsEffect);
            }
        }

        for (BuildingCard buildingCard : currentPlayer.getStats().getBuildingCards()) {
            buildingCard.onOwnTurnEnding(this);
        }
    }

    /**
     * Runs end-of-round bookkeeping: resolves the event cards on the board (including the
     * upper row at round 10, the era-change round), slides the board down, advances the era
     * if the upper character/event row has dropped below {@code gameSize + 4}, and bumps the
     * round counter. Fires {@link GameEvent#ERA_CHANGED} (if applicable) and
     * {@link GameEvent#ROUND_ENDED}, then snapshots.
     */
    public void endRound() {
        resolveEvents(currentRound == 10);
        board.endRound(gameSize, eraToCharEvdeck.get(currentEra));
        if (currentEra < 3 && board.getUpperRowCharEv().size() < gameSize + 4) {
            currentEra++;
            board.endEra(gameSize, eraToCharEvdeck.get(currentEra), eraToBuildingsdeck.get(currentEra), currentEra);
            notifyObservers(GameEvent.ERA_CHANGED);
        }
        currentRound++;

        saveGame();

        notifyObservers(GameEvent.ROUND_ENDED);
    }

    private void resolveEvents(boolean includeUpperRow) {
        Stream<Card> stream = board.getLowerRowCharEv().stream();
        if (includeUpperRow) {
            stream = Stream.concat(stream, board.getUpperRowCharEv().stream());
        }

        List<EventCard> events = stream
                .filter(c -> c.getCardType() == CardType.EVENT)
                .map(c -> (EventCard) c)
                .sorted(Comparator.comparing(e -> e.getEventType() == EvType.SUSTENANCE))
                .toList();

        for (EventCard e : events) {
            e.resolveEffect(this);
            e.resolveBuildingsEffects(this);
            notifyObservers(GameEvent.EVENT_RESOLVED);
        }
    }

    /**
     * Final-scoring pass run by {@link it.polimi.ingsw.am56.model.state.GameEnding}. For each
     * player, adds the running {@code builderPoints} bonus, the inventor-pair bonus
     * (inventors × distinct inventions), the artist bonus (5 points per pair of artists), and
     * lets each owned building card contribute via {@code BuildingCard.onGameEnding}. Then
     * deletes the save file (a cleanly finished game should not be restored on the next
     * server start) and fires {@link GameEvent#GAME_ENDED}.
     */
    public void endGameActions() {
        for (Player p : players) {
            p.getStats().addPoints(p.getStats().getBuilderPoints());
            p.getStats().addPoints(p.getStats().getCharacterTypeNum(CharacterType.INVENTOR) * p.getStats().getInventions().size());
            p.getStats().addPoints(10 * p.getStats().getCharacterTypeNum(CharacterType.ARTIST) / 2);

            for (BuildingCard buildingCard : p.getStats().getBuildingCards()) {
                buildingCard.onGameEnding(this, p);
            }
        }

        // Delete the save file: the game ended normally, next server start should be fresh
        PersistenceManager.deleteSave();

        notifyObservers(GameEvent.GAME_ENDED);
    }
}
