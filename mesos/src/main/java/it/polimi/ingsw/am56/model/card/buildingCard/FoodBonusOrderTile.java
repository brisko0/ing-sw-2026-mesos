package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;

import java.io.Serial;

/**
 * Building card that grants {@code foodBonus} extra food at the end of the owner's turn,
 * provided the order tile assigned to that player for the current round has a positive food
 * effect.
 */
public class FoodBonusOrderTile extends BuildingCard{
    @Serial
    private static final long serialVersionUID = 1L;

    private final int foodBonus;

    /**
     * Creates a food-bonus-on-order-tile building card.
     *
     * @param cardId     unique card id
     * @param era        era the card belongs to (1, 2 or 3)
     * @param numPlayers minimum player count required for this card to be in the deck
     * @param cost       food cost to acquire the building (before the building-sale discount)
     * @param points     base victory points granted at the end of the game
     * @param foodBonus  amount of food granted each turn the trigger fires
     */
    public FoodBonusOrderTile(int cardId, int era, int numPlayers, int cost, int points, int foodBonus) {
        super(cardId, era, numPlayers, cost, points);
        this.foodBonus = foodBonus;
    }

    /**
     * If the order tile assigned to the current player has a positive food effect, adds
     * {@code foodBonus} food to the current player's stats.
     *
     * @param gameModel current game model
     */
    @Override
    public void onOwnTurnEnding(GameModel gameModel) {
        if(gameModel.getPlayerToOrderTile().get(gameModel.getCurrentPlayer()).getFoodEffect() > 0){
            gameModel.getCurrentPlayer().getStats().addFood(foodBonus);
        }
    }
}
