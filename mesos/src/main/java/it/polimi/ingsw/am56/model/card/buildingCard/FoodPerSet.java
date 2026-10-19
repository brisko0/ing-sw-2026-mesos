package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterCard;

import java.io.Serial;

/**
 * Building card that grants {@code bonusFood} food each time the owner completes a new set
 * of characters. The last-known set count is tracked so the bonus only fires on increments.
 */
public class FoodPerSet extends BuildingCard {
    @Serial
    private static final long serialVersionUID = 1L;

    private int lastCheckedSets;
    private final int bonusFood;

    /**
     * Creates a food-per-set building card.
     *
     * @param cardId     unique card id
     * @param era        era the card belongs to (1, 2 or 3)
     * @param minPlayers minimum player count required for this card to be in the deck
     * @param cost       food cost to acquire the building (before the building-sale discount)
     * @param points     base victory points granted at the end of the game
     * @param bonusFood  food granted per newly completed set
     */
    public FoodPerSet(int cardId, int era, int minPlayers, int cost, int points, int bonusFood) {
        super(cardId, era, minPlayers, cost, points);
        lastCheckedSets = 0;
        this.bonusFood = bonusFood;
    }

    /**
     * Charges the building cost (via {@code super}) and initializes the set counter to the
     * owner's current completed-set count, so only future increases trigger the bonus.
     *
     * @param gameModel current game model
     */
    @Override
    public void resolveEffect(GameModel gameModel) {
        super.resolveEffect(gameModel);
        this.lastCheckedSets = gameModel.getCurrentPlayer().getStats().getCompletedSets();
    }

    /**
     * When a character is picked, recomputes the completed-set count and grants
     * {@code bonusFood} food for each newly completed set.
     *
     * @param gameModel current game model
     * @param c         the character card that has just been picked
     */
    @Override
    public void onCardPicked(GameModel gameModel, CharacterCard c) {
        int currentSets = gameModel.getCurrentPlayer().getStats().getCompletedSets();
        if (currentSets > lastCheckedSets) {
            int newSetsCompleted = currentSets - lastCheckedSets;
            gameModel.getCurrentPlayer().getStats().addFood(newSetsCompleted * bonusFood);
            lastCheckedSets = currentSets;
        }
    }
}
