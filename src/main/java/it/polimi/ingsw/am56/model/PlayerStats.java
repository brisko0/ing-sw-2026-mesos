package it.polimi.ingsw.am56.model;

import it.polimi.ingsw.am56.model.card.buildingCard.BuildingCard;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterCard;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterType;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

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

    public int getFood() {
        return food;
    }

    public int getPoints() {
        return points;
    }

    public int getCharacterTypeNum(CharacterType characterType) {
        int count = 0;
        for(CharacterCard characterCard : characterCards){
            if(characterCard.getCharacterType() == characterType){
                count++;
            }
        }
        return count;
    }

    public int getShamanStars() {
        return shamanStars;
    }

    public int getFeedingSale() {
        return feedingSale;
    }

    public int getBuildingSale() {
        return buildingSale;
    }

    public int getBuilderPoints() {
        return builderPoints;
    }

    public Set<String> getInventions() {
        return inventions;
    }

    public List<CharacterCard> getCharacterCards() {
        return characterCards;
    }

    public List<BuildingCard> getBuildingCards() {
        return buildingCards;
    }

    public void addPoints(int points) {
        this.points += points;
    }

    public void addFood(int food) {
        this.food += food;
    }
    public void addShamanStars(int shamanStars) {
        this.shamanStars += shamanStars;
    }
    public void addFeedingSale(int feedingSale) {
        this.feedingSale += feedingSale;
    }
    public void addBuildingSale(int buildingSale) { this.buildingSale += buildingSale; }
    public void addBuilderPoints(int builderPoints) {
        this.builderPoints += builderPoints;
    }

    public int getCompletedSets() {
        Map<CharacterType, Integer> counts = new EnumMap<>(CharacterType.class);
        for (CharacterType type : CharacterType.values()) counts.put(type, 0);

        for (CharacterCard c : characterCards) {
            counts.put(c.getCharacterType(), counts.get(c.getCharacterType()) + 1);
        }

        return Collections.min(counts.values());
    }

}
