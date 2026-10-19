package it.polimi.ingsw.am56.model.card;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;

import java.io.Serial;
import java.io.Serializable;

/**
 * Common base class for every card in the game. Each card carries an immutable identity
 * (id, era, minimum player count) plus two hooks that subclasses override:
 * {@link #resolveEffect(GameModel)} for the card's own effect and
 * {@link #resolveBuildingsEffects(GameModel)} for the cascade onto buildings owned by the
 * relevant player(s). Concrete kinds live in the {@code characterCard}, {@code eventCard},
 * and {@code buildingCard} packages.
 */
public abstract class Card implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;


    private final int cardId;
    private final int era;
    private final int minPlayers;

    /**
     * Creates a card with the given identity.
     *
     * @param cardId     unique card id
     * @param era        era the card belongs to (1, 2 or 3)
     * @param minPlayers minimum player count required for this card to be in the deck
     */
    public Card(int cardId, int era, int minPlayers) {
        this.cardId = cardId;
        this.era = era;
        this.minPlayers = minPlayers;
    }

    /**
     * Applies the card's effect to the game state. Called by the state machine when the
     * card is resolved (on pick for characters / buildings, at end of round for events).
     *
     * @param gameModel current game model
     */
    public abstract void resolveEffect(GameModel gameModel);

    /**
     * Adds this card to the given player's hand. The default implementation always throws,
     * since most cards (notably events) cannot be picked into a hand; subclasses that
     * <em>can</em> be picked override it.
     *
     * @param player the player picking the card
     * @throws InvalidActionException if this card kind is not pickable
     */
    public void addCardToPlayer(Player player) throws InvalidActionException {
        throw new InvalidActionException("Non è possibile pescare una carta evento!");
    }

    /**
     * Returns the discriminator identifying which family this card belongs to.
     *
     * @return the card type
     */
    public abstract CardType getCardType();

    /**
     * Returns the era the card belongs to (1, 2 or 3).
     *
     * @return the era
     */
    public int getEra() {
        return era;
    }

    /**
     * Returns the minimum player count required for this card to appear in the deck.
     *
     * @return the minimum player count
     */
    public int getMinPlayers() {
        return minPlayers;
    }

    /**
     * Fires building reactions triggered by this card. The default is a no-op; character and
     * event subclasses override it to notify the relevant building cards.
     *
     * @param gameModel current game model
     */
    public void resolveBuildingsEffects(GameModel gameModel) {}

    /**
     * Returns the food cost to acquire this card. Only buildings have a non-zero cost; the
     * base implementation returns {@code 0}.
     *
     * @return the food cost
     */
    public int getCost() {
        return 0;
    }

    /**
     * Returns the card's unique id.
     *
     * @return the card id
     */
    public int getCardId() {
        return cardId;
    }
}
