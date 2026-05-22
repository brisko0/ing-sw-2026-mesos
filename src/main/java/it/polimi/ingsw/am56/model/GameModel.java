package it.polimi.ingsw.am56.model;

import it.polimi.ingsw.am56.database.DatabaseManager;
import it.polimi.ingsw.am56.database.LeaderboardDAO;
import it.polimi.ingsw.am56.model.board.Board;
import it.polimi.ingsw.am56.model.board.OfferTile;
import it.polimi.ingsw.am56.model.board.OrderTile;
import it.polimi.ingsw.am56.model.card.*;
import it.polimi.ingsw.am56.model.card.buildingCard.BuildingCard;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterType;
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
import java.time.LocalDate;
import java.util.*;

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

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Reinitialize the transient field that was not saved
        this.observers = new ArrayList<>();
    }

    // =========================================================================
    // PERSISTENCE
    // =========================================================================

    public void saveGame() {
        PersistenceManager.save(this);
    }

    public boolean hasPlayer(String nickname) {
        return players.stream().anyMatch(p -> p.getNickname().equalsIgnoreCase(nickname));
    }

    // =========================================================================
    // OBSERVER
    // =========================================================================

    public void addObserver(GameObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(GameEvent event) {
        for (GameObserver observer : observers) {
            observer.onModelChanged(event);
        }
    }

    // =========================================================================
    // GETTERS
    // =========================================================================

    public GameState getCurrentState() {
        return state;
    }

    public int getGameSize() {
        return gameSize;
    }

    public Map<Integer, Deck> getEraToCharEvdeck() {
        return eraToCharEvdeck;
    }

    public Map<Integer, BuildingDeck> getEraToBuildingsdeck() {
        return eraToBuildingsdeck;
    }

    public Board getBoard() {
        return board;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Map<Player, OrderTile> getPlayerToOrderTile() {
        return playerToOrderTile;
    }

    public Map<Player, OfferTile> getPlayerToOfferTile() {
        return playerToOfferTile;
    }

    public int getCurrentEra() {
        return currentEra;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    // =========================================================================
    // STATE MACHINE
    // =========================================================================

    public void setState(GameState state) {
        this.state = state;
        notifyObservers(GameEvent.STATE_CHANGED);
        state.onEntry();
        if (state.getState() != State.GAME_ENDING) {
            saveGame();
        }
    }

    public void setCurrentPlayer(Player player) {
        this.currentPlayer = player;
    }

    public void syncLobbyState() {
        notifyObservers(GameEvent.LOBBY_STATE_SYNC);
    }

    public void syncGameState() {
        notifyObservers(GameEvent.GAME_STARTED);
    }

    public void addPlayer(Player p) throws InvalidActionException {
        state.addPlayer(p);
    }

    public void executeAddPlayer(Player p) {
        players.add(p);
        notifyObservers(GameEvent.PLAYER_ADDED);
        saveGame();
    }

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
            eraToCharEvdeck.put(era, charEvDeck);
            eraToBuildingsdeck.put(era, buildingDeckfactory.createDeck(era, gameSize));
        }

        board.fillUpperCharEv(gameSize, eraToCharEvdeck.get(currentEra));
        board.fillUpperBuildings(gameSize, eraToBuildingsdeck.get(currentEra), currentEra);

        saveGame();
        syncLobbyState();
    }

    // =========================================================================
    // TURN MANAGEMENT
    // =========================================================================

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

    public void handleTotemPlacement(Player p, OfferTile t) throws InvalidActionException {
        state.handleTotemPlacement(p, t);
    }

    public void executeTotemPlacement(Player p, OfferTile t) {
        playerToOfferTile.put(p, t);
        t.setIsFree(false);

        OrderTile oldOrderTile = playerToOrderTile.remove(p);
        oldOrderTile.setIsFree(true);

        saveGame();
        notifyObservers(GameEvent.TOTEM_PLACED);
    }

    public boolean checkCanPick() {
        OfferTile ot = playerToOfferTile.get(currentPlayer);

        boolean hasUpperSlot = currentPlayer.getPickedUpper() < ot.getUpperCards();
        boolean canPickUpper = hasUpperSlot && (
                !board.getUpperRowCharEv().isEmpty() ||
                        board.getUpperRowBuildings().stream()
                                .anyMatch(b -> currentPlayer.getStats().getFood() >= (b.getCost() - currentPlayer.getStats().getBuildingSale()))
        );

        boolean hasLowerSlot = currentPlayer.getPickedLower() < ot.getLowerCards();
        boolean canPickLower = hasLowerSlot && (
                !board.getLowerRowCharEv().isEmpty() ||
                        board.getLowerRowBuildings().stream()
                                .anyMatch(b -> currentPlayer.getStats().getFood() >= (b.getCost() - currentPlayer.getStats().getBuildingSale()))
        );

        return canPickUpper || canPickLower;
    }

    public void pickUpCard(Player p, Card c) throws InvalidActionException {
        state.pickUpCard(p, c);
    }

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

    public void resetTotemToOrder() {
        OfferTile oldOffer = playerToOfferTile.remove(currentPlayer);
        oldOffer.setIsFree(true);

        OrderTile firstFreeOrder = board.getOrderTiles().stream()
                .filter(OrderTile::isFree)
                .findFirst()
                .orElse(null);

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

    public void endRound() {
        resolveEvents();
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

    private void resolveEvents() {
        for (Card c : board.getLowerRowCharEv()) {
            if (c.getCardType() == CardType.EVENT) {
                c.resolveEffect(this);
                c.resolveBuildingsEffects(this);
                notifyObservers(GameEvent.EVENT_RESOLVED);
            }
        }
    }

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