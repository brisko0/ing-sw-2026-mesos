package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.card.CardType;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterCard;
import it.polimi.ingsw.am56.model.card.eventCard.EventCard;

import java.io.Serial;

/**
 * Base class for all building cards. A building card is bought by the current player at the
 * cost of {@code cost} food (reduced by the player's building-sale discount, never below
 * zero) and grants {@code points} victory points at the end of the game. Concrete subclasses
 * extend the base behavior by overriding one or more of the lifecycle hooks
 * ({@link #onCardPicked}, {@link #onEventResolving}, {@link #onOwnTurnEnding},
 * {@link #onGameEnding}) and/or {@link #resolveEffect(GameModel)} to apply their
 * rule-specific bonuses.
 */
public abstract class BuildingCard extends Card {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int cost;
    private final int points;

    /**
     * Creates a building card.
     *
     * @param cardId     unique card id
     * @param era        era the card belongs to (1, 2 or 3)
     * @param minPlayers minimum player count required for this card to be in the deck
     * @param cost       food cost to acquire the building (before the building-sale discount)
     * @param points     base victory points granted at the end of the game
     */
    public BuildingCard(int cardId, int era, int minPlayers, int cost, int points) {
        super(cardId,era, minPlayers);
        this.cost = cost;
        this.points = points;
    }

    /**
     * Returns the food cost to acquire this building, before any building-sale discount is
     * applied.
     *
     * @return the food cost
     */
    @Override
    public int  getCost() {
        return cost;
    }

    /**
     * Hook invoked when the owner picks a character card. The default is a no-op; subclasses
     * that reward picking specific characters override it.
     *
     * @param gameModel current game model
     * @param c         the character card that has just been picked
     */
    public void onCardPicked(GameModel gameModel, CharacterCard c) {}

    /**
     * Hook invoked while an event card is being resolved, once per owner. The default is a
     * no-op; subclasses that react to specific events override it.
     *
     * @param gameModel current game model
     * @param evCard    the event card being resolved
     * @param player    the owner of this building card
     */
    public void onEventResolving(GameModel gameModel, EventCard evCard, Player player) {}

    /**
     * Hook invoked at the end of the owner's turn. The default is a no-op; subclasses that
     * grant per-turn bonuses override it.
     *
     * @param gameModel current game model
     */
    public void onOwnTurnEnding(GameModel gameModel) {}

    /**
     * Hook invoked when the game ends. The base implementation grants the building's base
     * {@code points} to its owner; subclasses that add end-of-game bonuses should call
     * {@code super.onGameEnding(...)} first and then apply their extra scoring.
     *
     * @param gameModel current game model
     * @param player    the owner of this building card
     */
    public void onGameEnding(GameModel gameModel, Player player) {
        player.getStats().addPoints(points);
    }

    /**
     * Adds this card to the given player's building collection.
     *
     * @param player the player acquiring the building
     */
    @Override
    public void addCardToPlayer(Player player){
        player.getStats().getBuildingCards().add(this);
    }

    /**
     * Charges the current player the building's food cost, reduced by their building-sale
     * discount and clamped to zero.
     *
     * @param gameModel current game model
     */
    @Override
    public void resolveEffect(GameModel  gameModel) {
        gameModel.getCurrentPlayer().getStats().addFood(-Math.max(0, cost - gameModel.getCurrentPlayer().getStats().getBuildingSale()));
    }

    /**
     * Always returns {@link CardType#BUILDING}.
     *
     * @return {@link CardType#BUILDING}
     */
    @Override
    public CardType getCardType() {
        return CardType.BUILDING;
    }
}
