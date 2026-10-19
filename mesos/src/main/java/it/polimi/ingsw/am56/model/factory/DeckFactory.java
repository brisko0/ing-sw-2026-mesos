package it.polimi.ingsw.am56.model.factory;

import it.polimi.ingsw.am56.model.card.Deck;


/**
 * Factory abstraction for assembling a {@link Deck} for a given game configuration. Concrete
 * implementations typically delegate the loading of individual cards to a {@link CardFactory}
 * and then wrap the result in the appropriate {@link Deck} (shuffling it as needed).
 */
public interface DeckFactory {
    /**
     * Builds the deck for the given era and game configuration.
     *
     * @param era      era to build the deck for (1, 2 or 3)
     * @param gameSize number of players in the game; used by implementations whose card pool
     *                 depends on the player count (ignored by those that filter by era only)
     * @return the deck for the requested era and game size
     */
    Deck createDeck(int era, int gameSize);
}
