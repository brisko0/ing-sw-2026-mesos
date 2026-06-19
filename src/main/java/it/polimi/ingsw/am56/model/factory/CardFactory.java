package it.polimi.ingsw.am56.model.factory;

import it.polimi.ingsw.am56.model.card.Card;

import java.util.List;

/**
 * Factory abstraction for loading a family of {@link Card}s for a given game configuration.
 * Concrete implementations (one per card kind) know how to read their JSON resource and
 * deserialise it into the appropriate {@link Card} subclasses.
 *
 * @param <T> the concrete {@link Card} type produced by this factory
 */
public interface CardFactory<T extends Card> {
    /**
     * Creates the cards that belong to the given era and game configuration.
     *
     * @param era      era to load cards for (1, 2 or 3)
     * @param gameSize number of players in the game; used by implementations whose card pool
     *                 depends on the player count (ignored by those that filter by era only)
     * @return the cards matching the requested era and game size
     */
    List<T> createCards(int era, int gameSize);
}
