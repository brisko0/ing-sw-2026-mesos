package it.polimi.ingsw.am56.model;

import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterCard;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterType;

import java.util.*;

public class PlayerStats {
    private int food;
    private int points;
    private int huntersNum;
    private int artistsNum;
    private int inventorsNum;
    private int shamanStars;
    private int feedingSale;
    private int buildingSale;
    private int builderPoints;
    private Set<String>  inventions;
    private List<CharacterCard> characterCards;
    private List<Card> buildingCards;

    public PlayerStats(){
        this.food = 0;
        this.points = 0;
        this.huntersNum = 0;
        this.artistsNum = 0;
        this.inventorsNum = 0;
        this.shamanStars = 0;
        this.feedingSale = 0;
        this.buildingSale = 0;
        this.builderPoints = 0;
        this.inventions = new HashSet<String>();
        this.characterCards = new ArrayList<CharacterCard>();
        this.buildingCards = new ArrayList<Card>();
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

    public List<Card> getBuildingCards() {
        return buildingCards;
    }

    public void setInitialFood(int position){
        switch(position){
            case 1 -> this.food = 2;
            case 2,3 -> this.food = 3;
            case 4,5 -> this.food = 4;
        }
    }

    public void addPoints(int points) {
        this.points += points;
    }

    public void addFood(int food) {
        this.food += food;
    }
    public void subtractFood(int food) {
        this.food -= food;
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
    public void addInventor() {
        this.inventorsNum++;
    }
    public void addArtist() {
        this.artistsNum++;
    }
    public void addHunter() {
        this.huntersNum++;
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
