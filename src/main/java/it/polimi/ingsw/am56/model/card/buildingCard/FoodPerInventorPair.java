package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterCard;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterType;

import java.io.Serial;

/**
 * Building card that grants {@code foodBonus} food each time the owner forms a new pair of
 * inventors (i.e. when they own an inventor that has not yet contributed a unique invention).
 * The current pair count is tracked across picks to award the bonus only on increments.
 */
public class FoodPerInventorPair extends BuildingCard {
    @Serial
    private static final long serialVersionUID = 1L;

    private int currentPairs;
    private final int foodBonus;

    /**
     * Creates a food-per-inventor-pair building card.
     *
     * @param cardId     unique card id
     * @param era        era the card belongs to (1, 2 or 3)
     * @param numPlayers minimum player count required for this card to be in the deck
     * @param cost       food cost to acquire the building (before the building-sale discount)
     * @param points     base victory points granted at the end of the game
     * @param foodBonus  food granted each time the inventor-pair count increases
     */
    public FoodPerInventorPair(int cardId, int era, int numPlayers, int cost, int points, int foodBonus) {
        super(cardId, era, numPlayers, cost, points);
        currentPairs = 0;
        this.foodBonus = foodBonus;
    }

    /**
     * Charges the building cost (via {@code super}) and initializes the pair counter to the
     * owner's current inventor-pair count, so only future increases trigger the bonus.
     *
     * @param gameModel current game model
     */
    @Override
    public void resolveEffect(GameModel gameModel){
        super.resolveEffect(gameModel);
        currentPairs = calculatePairs(gameModel);
    }

    /**
     * When a character is picked, recomputes the inventor-pair count and grants
     * {@code foodBonus} food if it increased.
     *
     * @param gameModel     current game model
     * @param characterCard the character card that has just been picked
     */
    @Override
    public void onCardPicked(GameModel gameModel, CharacterCard characterCard) {
        int new_pairs = calculatePairs(gameModel);
        if(new_pairs > currentPairs){
            gameModel.getCurrentPlayer().getStats().addFood(foodBonus);
            currentPairs = new_pairs;
        }
    }

    private int calculatePairs(GameModel gameModel){
        return gameModel.getCurrentPlayer().getStats().getCharacterTypeNum(CharacterType.INVENTOR) -
                gameModel.getCurrentPlayer().getStats().getInventions().size();
    }
}
