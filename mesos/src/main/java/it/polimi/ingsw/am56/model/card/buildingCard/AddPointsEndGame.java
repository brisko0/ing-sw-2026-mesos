package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;

import java.io.Serial;

/**
 * Building card that grants a flat extra amount of victory points at the end of the game,
 * on top of the building's own base points.
 */
public class AddPointsEndGame extends BuildingCard {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int bonusPoints;

    /**
     * Creates an add-points-end-game building card.
     *
     * @param cardId      unique card id
     * @param era         era the card belongs to (1, 2 or 3)
     * @param numPlayers  minimum player count required for this card to be in the deck
     * @param cost        food cost to acquire the building (before the building-sale discount)
     * @param points      base victory points granted at the end of the game
     * @param bonusPoints additional victory points granted at the end of the game
     */
    public AddPointsEndGame(int cardId, int era, int numPlayers, int cost, int points, int bonusPoints) {
        super(cardId, era, numPlayers, cost, points);
        this.bonusPoints = bonusPoints;
    }

    /**
     * Grants the base building points (via {@code super}) and adds the flat {@code bonusPoints}
     * to the owner.
     *
     * @param gameModel current game model
     * @param player    the owner of this building card
     */
    @Override
    public void onGameEnding(GameModel gameModel, Player player) {
        super.onGameEnding(gameModel, player);
        player.getStats().addPoints(bonusPoints);
    }

}
