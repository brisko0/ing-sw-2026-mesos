package it.polimi.ingsw.am56.model.player;

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

    public int getSizeTribu() {
        return characterCards.size();
    }


}
