package it.polimi.ingsw.am56.model;

import it.polimi.ingsw.am56.model.card.buildingCard.BuildingCard;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterCard;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterType;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * Mutable bag of per-player gameplay state owned by a {@link Player}. Holds the resources
 * (food, victory points), the running counters fed by buildings and events
 * ({@link #shamanStars}, {@link #feedingSale}, {@link #buildingSale}, {@link #builderPoints}),
 * the set of unique inventions discovered, and the lists of {@link CharacterCard}s and
 * {@link BuildingCard}s the player has accumulated.
 * <p>
 * Mutators are exposed as additive {@code add*} helpers rather than plain setters, since the
 * gameplay rules only ever increment these counters.
 */
public class PlayerStats implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int food;
    private int points;
    private int shamanStars;
    private int feedingSale;
    private int buildingSale;
    private int builderPoints;
    private Set<String>  inventions;
    private List<CharacterCard> characterCards;
    private List<BuildingCard> buildingCards;

    /**
     * Creates a stats object zeroed across all counters, with empty {@code inventions},
     * {@code characterCards} and {@code buildingCards} collections.
     */
    public PlayerStats(){
        this.food = 0;
        this.points = 0;
        this.shamanStars = 0;
        this.feedingSale = 0;
        this.buildingSale = 0;
        this.builderPoints = 0;
        this.inventions = new HashSet<String>();
        this.characterCards = new ArrayList<CharacterCard>();
        this.buildingCards = new ArrayList<BuildingCard>();
    }

    /**
     * Returns the player's current food.
     *
     * @return the food amount
     */
    public int getFood() {
        return food;
    }

    /**
     * Returns the player's current victory points (running total; end-of-game scoring still adds on top).
     *
     * @return the points
     */
    public int getPoints() {
        return points;
    }

    /**
     * Counts how many of the player's character cards belong to the given archetype.
     *
     * @param characterType the character archetype to count
     * @return the number of matching character cards in the player's hand
     */
    public int getCharacterTypeNum(CharacterType characterType) {
        int count = 0;
        for(CharacterCard characterCard : characterCards){
            if(characterCard.getCharacterType() == characterType){
                count++;
            }
        }
        return count;
    }

    /**
     * Returns the running shaman-stars total. Some buildings convert these into end-of-game points.
     *
     * @return the shaman stars
     */
    public int getShamanStars() {
        return shamanStars;
    }

    /**
     * Returns the cumulative discount the player gets when feeding their tribe (granted by
     * some buildings).
     *
     * @return the feeding sale
     */
    public int getFeedingSale() {
        return feedingSale;
    }

    /**
     * Returns the cumulative discount the player gets when buying buildings. The card-picking
     * state subtracts this from the {@code BuildingCard}'s cost when checking affordability.
     *
     * @return the building sale
     */
    public int getBuildingSale() {
        return buildingSale;
    }

    /**
     * Returns the running points bonus granted to the player by builder characters. Read by
     * scoring code that compounds the bonus at end of game.
     *
     * @return the builder points
     */
    public int getBuilderPoints() {
        return builderPoints;
    }

    /**
     * Returns the set of unique inventions the player has discovered. A {@link Set} is used
     * because each invention counts once regardless of how many times it is granted.
     *
     * @return the inventions set
     */
    public Set<String> getInventions() {
        return inventions;
    }

    /**
     * Returns the live, mutable list of character cards in the player's hand.
     *
     * @return the character-cards list
     */
    public List<CharacterCard> getCharacterCards() {
        return characterCards;
    }

    /**
     * Returns the live, mutable list of building cards the player owns.
     *
     * @return the building-cards list
     */
    public List<BuildingCard> getBuildingCards() {
        return buildingCards;
    }

    /**
     * Adds the given amount of victory points to the running total.
     *
     * @param points points to add (may be negative if a card so dictates)
     */
    public void addPoints(int points) {
        this.points += points;
    }

    /**
     * Adds the given amount of food to the running total.
     *
     * @param food food to add (may be negative for losses)
     */
    public void addFood(int food) {
        this.food += food;
    }

    /**
     * Adds the given number of shaman stars.
     *
     * @param shamanStars shaman stars to add
     */
    public void addShamanStars(int shamanStars) {
        this.shamanStars += shamanStars;
    }

    /**
     * Increases the cumulative feeding discount.
     *
     * @param feedingSale feeding discount to add
     */
    public void addFeedingSale(int feedingSale) {
        this.feedingSale += feedingSale;
    }

    /**
     * Increases the cumulative building-purchase discount.
     *
     * @param buildingSale building discount to add
     */
    public void addBuildingSale(int buildingSale) { this.buildingSale += buildingSale; }

    /**
     * Increases the running builder-points bonus.
     *
     * @param builderPoints builder points to add
     */
    public void addBuilderPoints(int builderPoints) {
        this.builderPoints += builderPoints;
    }

    /**
     * Returns how many complete sets of characters (one of every {@link CharacterType}) the
     * player holds. Implemented as the minimum per-type count across the character hand.
     *
     * @return the number of complete character sets
     */
    public int getCompletedSets() {
        Map<CharacterType, Integer> counts = new EnumMap<>(CharacterType.class);
        for (CharacterType type : CharacterType.values()) counts.put(type, 0);

        for (CharacterCard c : characterCards) {
            counts.put(c.getCharacterType(), counts.get(c.getCharacterType()) + 1);
        }

        return Collections.min(counts.values());
    }

}
