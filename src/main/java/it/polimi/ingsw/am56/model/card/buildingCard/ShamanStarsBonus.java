package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;

import java.io.Serial;

/**
 * Building card that, upon being purchased, immediately grants the buyer {@code bonusStars}
 * shaman stars in addition to charging the building's food cost.
 */
public class ShamanStarsBonus extends BuildingCard {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int bonusStars;

    /**
     * Creates a shaman-stars-bonus building card.
     *
     * @param cardId      unique card id
     * @param era         era the card belongs to (1, 2 or 3)
     * @param numPlayers  minimum player count required for this card to be in the deck
     * @param cost        food cost to acquire the building (before the building-sale discount)
     * @param points      base victory points granted at the end of the game
     * @param bonusPoints shaman stars granted to the buyer on purchase
     */
    public ShamanStarsBonus(int cardId, int era, int numPlayers, int cost, int points, int bonusPoints) {
        super(cardId, era, numPlayers, cost, points);
        this.bonusStars = bonusPoints;
    }

    /**
     * Charges the building cost (via {@code super}) and grants the current player
     * {@code bonusStars} shaman stars.
     *
     * @param gameModel current game model
     */
    @Override
    public void resolveEffect(GameModel  gameModel) {
        super.resolveEffect(gameModel);
        gameModel.getCurrentPlayer().getStats().addShamanStars(bonusStars);
    }
}
