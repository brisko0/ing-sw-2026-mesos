package it.polimi.ingsw.am56.model;

import it.polimi.ingsw.am56.model.card.Card;

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
    private List<Card> characterCards;
    private List<Card> buildingCards;
    private Set<String>  inventions;

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

    public List<Card> getCharacterCards() {
        return characterCards;
    }

    public List<Card> getBuildingCards() {
        return buildingCards;
    }

    public Set<String> getInventions() {
        return inventions;
    }

    public void addPoints(int points) {
        this.points += points;
    }
    public void addFood(int food) {
        this.food += food;
    }
    public void AddCardStats(Card card) {
        this.characterCards.add(card);
    }
}
