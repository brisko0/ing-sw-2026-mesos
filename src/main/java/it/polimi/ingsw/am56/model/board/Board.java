package it.polimi.ingsw.am56.model.board;

import it.polimi.ingsw.am56.model.card.BuildingDeck;
import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.card.Deck;
import it.polimi.ingsw.am56.model.card.buildingCard.BuildingCard;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Shared central board holding the structures every player interacts with during a round:
 * <ul>
 *   <li>the pool of {@link OrderTile}s used by players to pick their turn order,</li>
 *   <li>the seven {@link OfferTile}s where totems are placed,</li>
 *   <li>two rows (upper / lower) of {@link BuildingCard}s,</li>
 *   <li>two rows (upper / lower) of character / event {@link Card}s.</li>
 * </ul>
 * The class implements {@link Serializable} so the full board can be snapshotted by the
 * persistence layer.
 */
public class Board implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private List<OrderTile> orderTiles;
    private List<OfferTile> offerTiles;
    private List<BuildingCard> upperRowBuildings;
    private List<Card> upperRowCharEv;
    private List<BuildingCard> lowerRowBuildings;
    private List<Card> lowerRowCharEv;

    /**
     * Creates an empty board. All six lists are initialised to empty {@link ArrayList}s and
     * must be populated by the setup code (offer / order tiles loaded from JSON, card rows
     * filled by the relevant {@code fill*} methods).
     */
    public Board(){
        orderTiles = new ArrayList<>();
        offerTiles = new ArrayList<>();
        upperRowBuildings = new ArrayList<>();
        upperRowCharEv = new ArrayList<>();
        lowerRowBuildings = new ArrayList<>();
        lowerRowCharEv = new ArrayList<>();
    }

    /**
     * Returns the live, mutable list of order tiles. Callers may modify this list — for
     * instance to mark a tile as taken.
     *
     * @return the order-tiles list backing the board
     */
    public List<OrderTile> getOrderTiles() {
        return orderTiles;
    }

    /**
     * Returns the live, mutable list of offer tiles. Callers may modify this list — for
     * instance to flip an {@link OfferTile}'s {@code isFree} flag.
     *
     * @return the offer-tiles list backing the board
     */
    public List<OfferTile> getOfferTiles() {
        return offerTiles;
    }

    /**
     * Returns the live, mutable list of the upper row of building cards.
     *
     * @return the upper-row buildings list backing the board
     */
    public List<BuildingCard> getUpperRowBuildings() {
        return upperRowBuildings;
    }

    /**
     * Returns the live, mutable list of the upper row of character / event cards.
     *
     * @return the upper-row character / event list backing the board
     */
    public List<Card> getUpperRowCharEv() {
        return upperRowCharEv;
    }

    /**
     * Returns the live, mutable list of the lower row of building cards.
     *
     * @return the lower-row buildings list backing the board
     */
    public List<BuildingCard> getLowerRowBuildings() {
        return lowerRowBuildings;
    }

    /**
     * Returns the live, mutable list of the lower row of character / event cards.
     *
     * @return the lower-row character / event list backing the board
     */
    public List<Card> getLowerRowCharEv() {
        return lowerRowCharEv;
    }

    /**
     * Tops up the lower character row to {@code gameSize + 1} cards by drawing from the
     * supplied deck. Used during initial setup.
     *
     * @param gameSize       number of players in the game
     * @param charactersDeck deck to draw character cards from
     */
    public void fillLowerChar(int gameSize, Deck charactersDeck){
        while(lowerRowCharEv.size() < (gameSize + 1)){
            lowerRowCharEv.add(charactersDeck.drawCard());
        }
    }

    /**
     * Tops up the upper character / event row to {@code gameSize + 4} cards, stopping early
     * if the supplied deck is exhausted.
     *
     * @param gameSize   number of players in the game
     * @param charEvDeck deck to draw character / event cards from
     */
    public void fillUpperCharEv(int gameSize, Deck charEvDeck){
        while(upperRowCharEv.size() < (gameSize + 4) && !charEvDeck.getRemainingCards().isEmpty()){
            upperRowCharEv.add(charEvDeck.drawCard());
        }
    }

    /**
     * Draws the era- and player-count–dependent number of building cards into the upper row.
     * The counts are:
     * <ul>
     *   <li>era 1 — 1 card (2 players) or 2 cards (3+),</li>
     *   <li>era 2 — 2 cards (≤3 players) or 3 cards (4+),</li>
     *   <li>era 3 — 3 cards (2 players), 4 cards (3 / 4) or 5 cards (5).</li>
     * </ul>
     *
     * @param gameSize      number of players in the game
     * @param buildingsDeck deck to draw building cards from
     * @param era           current era (1, 2 or 3)
     */
    public void fillUpperBuildings(int gameSize, BuildingDeck buildingsDeck, int era){
        switch(era) {
            case 1:
                if(gameSize == 2) {
                    for(int i = 0; i < 1; i++){
                        upperRowBuildings.add(buildingsDeck.drawCard());
                    }
                } else {
                    for(int i = 0; i < 2; i++){
                        upperRowBuildings.add(buildingsDeck.drawCard());
                    }
                }
                break;
            case 2:
                if(gameSize <= 3) {
                    for(int i = 0; i < 2; i++){
                        upperRowBuildings.add(buildingsDeck.drawCard());
                    }
                } else {
                    for(int i = 0; i < 3; i++){
                        upperRowBuildings.add(buildingsDeck.drawCard());
                    }
                }
                break;
            case 3:
                if(gameSize == 2) {
                    for(int i = 0; i < 3; i++){
                        upperRowBuildings.add(buildingsDeck.drawCard());
                    }
                } else if (gameSize == 5) {
                    for(int i = 0; i < 5; i++){
                        upperRowBuildings.add(buildingsDeck.drawCard());
                    }
                } else {
                    for(int i = 0; i < 4; i++){
                        upperRowBuildings.add(buildingsDeck.drawCard());
                    }
                }
                break;
        }
    }

    /**
     * Removes the given card from whichever of the four card rows currently holds it. The
     * call is a no-op if the card is not on the board.
     *
     * @param c the card to remove
     */
    public void removeCard(Card c) {
        List.of(lowerRowCharEv, upperRowCharEv, upperRowBuildings, lowerRowBuildings)
                .forEach(list -> list.remove(c));
    }

    /**
     * Advances the board at the end of a round: the upper character / event row is moved
     * down to replace the lower row, and the upper row is then refilled from the deck.
     *
     * @param gameSize   number of players in the game
     * @param charEvDeck deck to draw new upper-row character / event cards from
     */
    public void endRound(int gameSize, Deck charEvDeck){
        lowerRowCharEv.clear();
        lowerRowCharEv.addAll(upperRowCharEv);
        upperRowCharEv.clear();
        fillUpperCharEv(gameSize, charEvDeck);
    }

    /**
     * Advances the board at the end of an era: the upper building row is moved down to
     * replace the lower row, then both the upper character / event row and the upper
     * building row are refilled for the next era.
     *
     * @param gameSize      number of players in the game
     * @param charEvDeck    deck to refill the upper character / event row from
     * @param buildingsDeck deck to refill the upper building row from
     * @param era           era to draw buildings for (the era that is about to start)
     */
    public void endEra(int gameSize, Deck charEvDeck, BuildingDeck buildingsDeck, int era){
        lowerRowBuildings.clear();
        lowerRowBuildings.addAll(upperRowBuildings);
        upperRowBuildings.clear();
        fillUpperCharEv(gameSize, charEvDeck);
        fillUpperBuildings(gameSize, buildingsDeck, era);
    }

    /**
     * Looks up a card by id across all four card rows.
     *
     * @param cardId the card id to look for
     * @return the matching card, or {@code null} if no row currently contains it
     */
    public Card getCardById(int cardId) {
        return Stream.of(upperRowCharEv, lowerRowCharEv, upperRowBuildings, lowerRowBuildings)
                .flatMap(Collection::stream)
                .filter(c -> c.getCardId() == cardId)
                .findFirst()
                .orElse(null);
    }
}
