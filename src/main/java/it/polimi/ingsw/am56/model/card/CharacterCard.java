package it.polimi.ingsw.am56.model.card;

public class CharacterCard extends Card{
    private final CharType type;
    private final int shamanStars;
    private final int buildingSale;
    private final int builderPoints;
    private final int feedingSale;
    private final String invention;
    private final boolean providesFood;

    public CharacterCard(int era, int numPlayers, CharType type,
                         int shamanStars, int buildingSale, int builderPoints,
                         int feedingSale, String invention, boolean providesFood) {
        super(era, numPlayers);
        this.type = type;
        this.shamanStars = shamanStars;
        this.buildingSale = buildingSale;
        this.builderPoints = builderPoints;
        this.feedingSale = feedingSale;
        this.invention = invention;
        this.providesFood = providesFood;
    }

    @Override
    public void resolveEffect(){}
}
