package it.polimi.ingsw.am56.model;

import it.polimi.ingsw.am56.model.board.Board;
import it.polimi.ingsw.am56.model.board.OfferTile;
import it.polimi.ingsw.am56.model.board.OrderTile;
import it.polimi.ingsw.am56.model.card.*;
import it.polimi.ingsw.am56.model.card.buildingCard.BuildingCard;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterType;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;
import it.polimi.ingsw.am56.model.factory.board.BoardFactory;
import it.polimi.ingsw.am56.model.factory.card.EventCardFactory;
import it.polimi.ingsw.am56.model.factory.deck.BuildingDeckFactory;
import it.polimi.ingsw.am56.model.factory.deck.CharacterDeckFactory;
import it.polimi.ingsw.am56.model.state.GameState;
import it.polimi.ingsw.am56.model.state.PlayersFilling;

import java.util.*;

public class GameModel {
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

    private final transient List<GameObserver> observers;

    public GameModel() {
        this.state = new PlayersFilling(this);
        this.gameSize = -1;
        this.eraToCharEvdeck = new HashMap<>();
        this.eraToBuildingsdeck = new HashMap<>();
        this.board = null;
        this.players = new ArrayList<Player>();
        this.playerToOrderTile = new HashMap<>();
        this.playerToOfferTile = new HashMap<>();
        this.currentEra = 1;
        this.currentRound = 1;
        this.observers = new ArrayList<>();
        this.currentPlayer = null;
    }

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

    public void setState(GameState state) {
        this.state = state;
        notifyObservers(GameEvent.STATE_CHANGED);
        state.onEntry();
    }

    public void setGameSize(int gameSize) throws InvalidActionException {
        state.setGameSize(gameSize);
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public void setCurrentPlayer(Player player) {
        this.currentPlayer = player;
    }

    public void addPlayer(Player p) throws InvalidActionException{
        state.addPlayer(p);
        notifyObservers(GameEvent.PLAYER_ADDED);
    }

    public void executeAddPlayer(Player p){
        players.add(p);
    }

    public void initializeGame(int gameSize) throws InvalidActionException {
        Collections.shuffle(players);

        if(gameSize < 2 || gameSize > 5){
            throw new InvalidActionException("Numero di giocatori non valido");
        }

        this.gameSize = gameSize;

        board = BoardFactory.createBoard(gameSize);

        EventCardFactory eventCardFactory = new EventCardFactory();

        CharacterDeckFactory characterDeckFactory = new CharacterDeckFactory();
        BuildingDeckFactory buildingDeckFactory = new BuildingDeckFactory();

        Deck era1deck = characterDeckFactory.createDeck(1, gameSize);

        board.fillLowerChar(gameSize, era1deck);

        era1deck.getRemainingCards().addAll(eventCardFactory.createCards(1, gameSize));
        era1deck.shuffle();

        getEraToCharEvdeck().put(1, era1deck);

        eraToBuildingsdeck.put(1, buildingDeckFactory.createDeck(1, gameSize));

        for (int era = 2; era <= 3; era++) {
            Deck charEvDeck = characterDeckFactory.createDeck(era, gameSize);
            charEvDeck.getRemainingCards().addAll(eventCardFactory.createCards(era, gameSize));
            charEvDeck.shuffle();
            eraToCharEvdeck.put(era, charEvDeck);
            eraToBuildingsdeck.put(era, buildingDeckFactory.createDeck(era, gameSize));
        }

        board.fillUpperCharEv(gameSize, eraToCharEvdeck.get(currentEra));
        board.fillUpperBuildings(gameSize, eraToBuildingsdeck.get(currentEra));
    }

    public void setCurrentPlayerOrder(){
        List<Player> needingToPlace = players.stream()
                .filter(p -> !playerToOfferTile.containsKey(p))
                .toList();

        if (!needingToPlace.isEmpty()) {
            this.currentPlayer = needingToPlace.stream()
                    .min(Comparator.comparingInt(p -> playerToOrderTile.get(p).getOrderTileId()))
                    .orElse(null);
        }
        notifyObservers(GameEvent.TURN_CHANGED);
    }

    public void setCurrentPlayerOffer(){
        List<Player> needingToAct = players.stream()
                .filter(p -> !playerToOrderTile.containsKey(p))
                .toList();

        if (!needingToAct.isEmpty()) {
            this.currentPlayer = needingToAct.stream()
                    .min(Comparator.comparing(p -> playerToOfferTile.get(p).getLetter()))
                    .orElse(null);
        }
        notifyObservers(GameEvent.TURN_CHANGED);
    }

    public void handleTotemPlacement (Player p, OfferTile t) throws InvalidActionException {
        state.handleTotemPlacement(p, t);
    }

    public void executeTotemPlacement(Player p, OfferTile t) {
        playerToOfferTile.put(p, t);
        t.setIsFree(false);

        OrderTile oldOrderTile = playerToOrderTile.remove(p);
        oldOrderTile.setIsFree(true);

        notifyObservers(GameEvent.TOTEM_PLACED);
    }

    public boolean checkCanPick() {
        OfferTile ot = playerToOfferTile.get(currentPlayer);
        if (ot == null) return false;

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

    public void executeCardPick(Player p, Card c, boolean isUpper) {

        c.addCardToPlayer(p);
        if(isUpper) {
            p.setPickedUpper(p.getPickedUpper() + 1);
        }
        else {
            p.setPickedLower(p.getPickedLower() + 1);
        }
        board.removeCard(c);
        c.resolveEffect(this);

        c.resolveBuildingsEffects(this);

        notifyObservers(GameEvent.CARD_PICKED);
    }

    public void resetTotemToOrder(){
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

        notifyObservers(GameEvent.TOTEM_RESET);
    }

    private void applyOrderBonuses(){
        int foodEffect = playerToOrderTile.get(currentPlayer).getFoodEffect();
        int pointsEffect = playerToOrderTile.get(currentPlayer).getPointsEffect();

        if(foodEffect >= 0){
            currentPlayer.getStats().addFood(foodEffect);
        }
        else {
            if(currentPlayer.getStats().getFood() > 0) {
                currentPlayer.getStats().addFood(foodEffect);
            }
            else {
                currentPlayer.getStats().addPoints(pointsEffect);
            }
        }
    }

    public void endRound(){
        resolveEvents();
        board.endRound(gameSize, eraToCharEvdeck.get(currentEra));
        if(currentEra < 3 && board.getUpperRowCharEv().size() < gameSize + 4){
            currentEra ++;
            board.endEra(gameSize, eraToCharEvdeck.get(currentEra), eraToBuildingsdeck.get(currentEra));
            notifyObservers(GameEvent.ERA_CHANGED);
        }
        currentRound++;
        notifyObservers(GameEvent.ROUND_ENDED);
    }

    private void resolveEvents() {
        for(Card c : board.getLowerRowCharEv()){
            if(c.getCardType() == CardType.EVENT){
                c.resolveEffect(this);
                c.resolveBuildingsEffects(this);
            }
        }
    }

    public void endGameActions() {
        for(Player p : players){
            p.getStats().addPoints(p.getStats().getBuilderPoints());
            p.getStats().addPoints(p.getStats().getCharacterTypeNum(CharacterType.INVENTOR) * p.getStats().getInventions().size());
            p.getStats().addPoints(p.getStats().getCharacterTypeNum(CharacterType.ARTIST) / 2);

            for(BuildingCard buildingCard : p.getStats().getBuildingCards()){
                buildingCard.onGameEnding(this, p);
            }

        }
        notifyObservers(GameEvent.GAME_ENDED);
    }
}
