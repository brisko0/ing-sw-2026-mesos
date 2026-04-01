package it.polimi.ingsw.am56.model;

import it.polimi.ingsw.am56.model.card.Card;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private List<Card> characterCards;
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
        this.characterCards = new ArrayList<Card>();
        this.buildingCards = new ArrayList<Card>();
    }

    public int getFood() {
        return food;
    }

    public int getPoints() {
        return points;
    }

    public int getHuntersNum() {
        return huntersNum;
    }

    public int getArtistsNum() {
        return artistsNum;
    }

    public int getInventorsNum() {
        return inventorsNum;
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

    public List<Card> getCharacterCards() {
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

}
