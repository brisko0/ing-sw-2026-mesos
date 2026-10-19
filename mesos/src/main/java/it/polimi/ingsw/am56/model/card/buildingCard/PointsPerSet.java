package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;

import java.io.Serial;

/**
 * Building card that, at the end of the game, awards the owner {@code bonusPoints} victory
 * points per character set they have completed.
 */
public class PointsPerSet extends BuildingCard {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int bonusPoints;

    /**
     * Creates a points-per-set building card.
     *
     * @param cardId      unique card id
     * @param era         era the card belongs to (1, 2 or 3)
     * @param minPlayers  minimum player count required for this card to be in the deck
     * @param cost        food cost to acquire the building (before the building-sale discount)
     * @param points      base victory points granted at the end of the game
     * @param bonusPoints victory points granted per completed character set
     */
    public PointsPerSet(int cardId, int era, int minPlayers, int cost, int points, int bonusPoints) {
        super(cardId, era, minPlayers, cost, points);
        this.bonusPoints = bonusPoints;
    }

    /**
     * Grants the base building points (via {@code super}) and adds {@code bonusPoints} per
     * completed set the owner has.
     *
     * @param gameModel current game model
     * @param player    the owner of this building card
     */
    @Override
    public void onGameEnding(GameModel gameModel, Player player) {
        super.onGameEnding(gameModel, player);
        player.getStats().addPoints(player.getStats().getCompletedSets() * bonusPoints);
    }
}
