package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;

import java.io.Serial;

/**
 * Building card that, at the end of the game, awards the owner a second copy of their
 * builder points (effectively doubling the contribution of builder character cards).
 */
public class DoubleBuildersPoints extends BuildingCard{
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a double-builders-points building card.
     *
     * @param cardId     unique card id
     * @param era        era the card belongs to (1, 2 or 3)
     * @param numPlayers minimum player count required for this card to be in the deck
     * @param cost       food cost to acquire the building (before the building-sale discount)
     * @param points     base victory points granted at the end of the game
     */
    public DoubleBuildersPoints(int cardId, int era, int numPlayers, int cost, int points){
        super(cardId, era, numPlayers, cost, points);
    }

    /**
     * Grants the base building points (via {@code super}) and adds an extra copy of the
     * owner's builder points.
     *
     * @param gameModel current game model
     * @param player    the owner of this building card
     */
    @Override
    public void onGameEnding(GameModel gameModel, Player player) {
        super.onGameEnding(gameModel, player);
        player.getStats().addPoints(player.getStats().getBuilderPoints());
    }
}
