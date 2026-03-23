package it.polimi.ingsw.am56.model.card;
import java.util.ArrayList;
import java.util.List;

public class CharacterCard extends Card {
    private final int shamanStars;
    private final int buildingSale;
    private final int builderPoints;
    private final int feedingSale;
    private final String invention;
    private final boolean providesFood;
    private final CharType charType;

    public CharacterCard(int idCard, int era, int numPlayers, int shamanStars, int buildingSale, int builderPoints, int feedingSale, String invention, boolean providesFood, CharType charType) {
        super(idCard, era, numPlayers);
        this.shamanStars = shamanStars;
        this.buildingSale = buildingSale;
        this.builderPoints = builderPoints;
        this.feedingSale = feedingSale;
        this.invention = invention;
        this.providesFood = providesFood;
        this.charType = charType;
    }

    public int getShamanStars() { return shamanStars; }
    public int getBuildingSale() { return buildingSale; }
    public int getBuilderPoints() { return builderPoints; }
    public int getFeedingSale() { return feedingSale; }
    public String getInvention() { return invention; }
    public boolean isProvidesFood() { return providesFood; }
    public CharType getCharType() { return charType; }

    @Override
    public void resolveEffect() {
        // TODO: implement
    }

}