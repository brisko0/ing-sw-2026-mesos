package it.polimi.ingsw.am56.model.board;

import java.io.Serial;
import java.io.Serializable;

public class OfferTile implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String letter;
    private final int food;
    private final int upperCards;
    private final int lowerCards;
    private final int minPlayers;
    private boolean isFree;

    public OfferTile(String letter, int food, int upperCards, int lowerCards, int minPlayers, boolean isFree) {
        this.letter = letter;
        this.food = food;
        this.upperCards = upperCards;
        this.lowerCards = lowerCards;
        this.minPlayers = minPlayers;
        this.isFree = isFree;
    }

    public String getLetter() {
        return letter;
    }

    public int getFoodBonus() {
        return food;
    }

    public int getUpperCards() {
        return upperCards;
    }

    public int getLowerCards() {
        return lowerCards;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public boolean isFree() {
        return isFree;
    }

    public void setIsFree(boolean isFree) {
        this.isFree = isFree;
    }
}
