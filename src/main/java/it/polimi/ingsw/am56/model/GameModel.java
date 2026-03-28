package it.polimi.ingsw.am56.model;

import it.polimi.ingsw.am56.model.board.Board;
import it.polimi.ingsw.am56.model.board.OfferTile;
import it.polimi.ingsw.am56.model.board.OrderTile;
import it.polimi.ingsw.am56.model.card.*;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;
import it.polimi.ingsw.am56.model.state.GameState;
import it.polimi.ingsw.am56.model.state.PlayersFilling;
import it.polimi.ingsw.am56.model.state.TotemPlacement;

import java.util.*;

public class GameModel {
    private GameState state;
    private int gameSize;
    private Map<Integer, Deck> eraToCharEvdeck;
    private Map<Integer, Deck> eraToBuildingsdeck;
    private Board board;
    private List<Player> players;
    private Map<Player, OrderTile> playerToOrderTile;
    private Map<Player, OfferTile> playerToOfferTile;
    private int currentEra;
    private int currentRound;
    private Player currentPlayer;

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
        this.currentPlayer = null;
    }

    public GameState getState() {
        return state;
    }

    public int getGameSize() {
        return gameSize;
    }

    public Map<Integer, Deck> getEraToCharEvdeck() {
        return eraToCharEvdeck;
    }

    public Map<Integer, Deck> getEraToBuildingsdeck() {
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
    }

    public void executeAddPlayer(Player p){
        players.add(p);
    }

    public void initializeGame(int gameSize){
        Collections.shuffle(players);
        for (int era = 1; era <= 3; era++) {
            eraToCharEvdeck.put(era, Deck.createCharEvDeck(era, gameSize));
            eraToBuildingsdeck.put(era, Deck.createBuildingsDeck(era, gameSize));
        }
        board = Board.createBoard(gameSize);
        board.fillBoard(gameSize, eraToCharEvdeck.get(currentEra), eraToBuildingsdeck.get(currentEra));
        for (int i = 0; i < players.size(); i++) {
            players.get(i).getStats().setInitialFood(i + 1);
            playerToOrderTile.put(players.get(i), board.getOrderTiles().get(i));
            board.getOrderTiles().get(i).setIsFree(false);
        }
        this.setState(new TotemPlacement(this));
        updateCurrentPlayer();
    }

    public void updateCurrentPlayer() {
        // prima si controlla se qualcuno deve ancora piazzare il suo totem
        if (state instanceof TotemPlacement) {
            setCurrentPlayerOrder();
        } else {
            setCurrentPlayerOffer();
        }
    }

    private void setCurrentPlayerOrder(){
        List<Player> needingToPlace = players.stream()
                .filter(p -> !playerToOfferTile.containsKey(p))
                .toList();

        if (!needingToPlace.isEmpty()) {
            this.currentPlayer = needingToPlace.stream()
                    .min(Comparator.comparingInt(p -> playerToOrderTile.get(p).getOrderTileId()))
                    .orElse(null);
        }
    }

    private void setCurrentPlayerOffer(){
        List<Player> needingToAct = players.stream()
                .filter(p -> !playerToOrderTile.containsKey(p))
                .toList();

        if (!needingToAct.isEmpty()) {
            // Ordiniamo i "mancanti" per lettera OfferTile e prendiamo il primo
            this.currentPlayer = needingToAct.stream()
                    .min(Comparator.comparing(p -> playerToOfferTile.get(p).getLetter()))
                    .orElse(null);
        }
    }

    public void handleTotemPlacement (Player p, OfferTile t) throws InvalidActionException {
        state.handleTotemPlacement(p, t);
    }

    public void executeTotemPlacement(Player p, OfferTile t) {
        playerToOfferTile.put(p, t);
        t.setIsFree(false);

        OrderTile oldOrderTile = playerToOrderTile.remove(p);
        oldOrderTile.setIsFree(true);
    }

    public boolean checkCanPick(Player p) {
        OfferTile ot = playerToOfferTile.get(p);
        if (ot == null) return false;

        boolean hasUpperSlot = p.getPickedUpper() < ot.getUpperCards();

        boolean canPickUpper = hasUpperSlot && (
                !board.getUpperRowCharEv().isEmpty() ||
                        board.getUpperRowBuildings().stream()
                                .filter(BuildingCard.class::isInstance) // Filtra solo se è un edificio
                                .map(BuildingCard.class::cast)         // Lo trasforma in BuildingCard
                                .anyMatch(b -> p.getStats().getFood() >= Math.max(0, b.getCost() - p.getStats().getBuildingSale()))
        );

        boolean hasLowerSlot = p.getPickedLower() < ot.getLowerCards();

        boolean canPickLower = hasLowerSlot && (
                !board.getLowerRowCharEv().isEmpty() ||
                        board.getLowerRowBuildings().stream()
                                .filter(BuildingCard.class::isInstance)
                                .map(BuildingCard.class::cast)
                                .anyMatch(b -> p.getStats().getFood() >= Math.max(0, b.getCost() - p.getStats().getBuildingSale()))
        );

        return canPickUpper || canPickLower;
    }

    public void pickUpCharacterCard(Player p, CharacterCard c) throws InvalidActionException {
        state.pickUpCharacterCard(p, c);
    }

    public void pickUpBuildingCard(Player p, BuildingCard c) throws InvalidActionException {
        state.pickUpBuildingCard(p, c);
    }

    public void executeCharacterCardPick(Player p, CharacterCard c, boolean isUpper) {

        p.getStats().getCharacterCards().add(c);
        if(isUpper) {
            p.setPickedUpper(p.getPickedUpper() + 1);
        }
        else {
            p.setPickedLower(p.getPickedLower() + 1);
        }
        board.removeCard(c);
        c.resolveEffect(this);
    }

    public void executeBuildingCardPick(Player p, BuildingCard c, boolean isUpper) {

        // ricordare di aggiungere la sottrazione del cibo nella resolveEffect di BuildingCard

        p.getStats().getBuildingCards().add(c);
        if(isUpper) {
            p.setPickedUpper(p.getPickedUpper() + 1);
        }
        else {
            p.setPickedLower(p.getPickedLower() + 1);
        }
        board.removeCard(c);
        c.resolveEffect(this);
    }

    public void resetTotemToOrder(Player p){
        OfferTile oldOffer = playerToOfferTile.remove(p);
        oldOffer.setIsFree(true);

        OrderTile firstFreeOrder = board.getOrderTiles().stream()
                .filter(OrderTile::isFree)
                .findFirst()
                .orElse(null);

        playerToOrderTile.put(p, firstFreeOrder);
        firstFreeOrder.setIsFree(false);

        p.setPickedUpper(0);
        p.setPickedLower(0);
    }

    public void endRound(){
        resolveEvents();
        board.endRound(gameSize, eraToCharEvdeck.get(currentEra));
        if(currentEra < 3 && board.getUpperRowCharEv().size() < gameSize + 4){
            currentEra ++;
            board.endEra(gameSize, eraToCharEvdeck.get(currentEra), eraToBuildingsdeck.get(currentEra));
        }
        currentRound++;
    }

    private void resolveEvents() {
        for(Card c : board.getLowerRowCharEv()){
            if(c.getCardType() == CardType.EVENT){
                c.resolveEffect(this);
            }
        }
    }
}
