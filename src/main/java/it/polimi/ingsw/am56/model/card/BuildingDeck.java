package it.polimi.ingsw.am56.model.card;

import it.polimi.ingsw.am56.model.card.buildingCard.BuildingCard;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

public class BuildingDeck extends Deck{
    @Serial
    private static final long serialVersionUID = 1L;

    public BuildingDeck(int deckId, int era, List<BuildingCard> buildingCards) {
        super(deckId, era, new ArrayList<>(buildingCards));
        shuffle();
    }

    @Override
    public BuildingCard drawCard(){
        return (BuildingCard) super.drawCard();
    }
}
