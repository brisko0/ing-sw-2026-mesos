package it.polimi.ingsw.am56.model.card;

import it.polimi.ingsw.am56.model.card.buildingCard.BuildingCard;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link Deck} specialisation holding only {@link BuildingCard}s. The override of
 * {@link #drawCard()} narrows the return type so callers don't have to cast.
 */
public class BuildingDeck extends Deck{
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a building deck holding a defensive copy of {@code buildingCards} and shuffles it.
     *
     * @param deckId        identifier of the deck
     * @param era           era the deck belongs to (1, 2 or 3)
     * @param buildingCards building cards used to populate the deck (copied, not retained)
     */
    public BuildingDeck(int deckId, int era, List<BuildingCard> buildingCards) {
        super(deckId, era, new ArrayList<>(buildingCards));
        shuffle();
    }

    /**
     * Removes and returns the building on top of the deck.
     *
     * @return the drawn building card
     * @throws IndexOutOfBoundsException if the deck is empty
     */
    @Override
    public BuildingCard drawCard(){
        return (BuildingCard) super.drawCard();
    }
}
