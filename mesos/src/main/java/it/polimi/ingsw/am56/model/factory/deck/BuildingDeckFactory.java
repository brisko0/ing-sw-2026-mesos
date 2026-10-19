package it.polimi.ingsw.am56.model.factory.deck;

import it.polimi.ingsw.am56.model.card.BuildingDeck;
import it.polimi.ingsw.am56.model.card.buildingCard.BuildingCard;
import it.polimi.ingsw.am56.model.factory.DeckFactory;
import it.polimi.ingsw.am56.model.factory.card.BuildingCardFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link DeckFactory} implementation that assembles the shuffled {@link BuildingDeck} for a
 * given era. Delegates the loading of the individual {@link BuildingCard}s to
 * {@link BuildingCardFactory}, then wraps them in a {@link BuildingDeck} (which shuffles them
 * in place).
 * <p>
 * The {@link #createDeck(int, int)} return type is narrowed to {@link BuildingDeck} so callers
 * holding a {@code BuildingDeckFactory} reference don't need to cast.
 */
public class BuildingDeckFactory implements DeckFactory {

    /**
     * Builds the building deck for the given era. The {@code gameSize} parameter is accepted
     * for the {@link DeckFactory} contract but ignored — buildings are filtered by era only.
     *
     * @param era      era to build the deck for (1, 2 or 3)
     * @param gameSize number of players in the game (unused for building decks)
     * @return a shuffled deck of the building cards belonging to the given era
     */
    @Override
    public BuildingDeck createDeck(int era, int gameSize) {

        BuildingCardFactory buildingCardFactory = new BuildingCardFactory();

        List<BuildingCard> buildingCards = buildingCardFactory.createCards(era, gameSize);

        return new BuildingDeck(era, era, new ArrayList<>(buildingCards));
    }
}
