package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterCard;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterType;

import java.io.Serial;

public class FoodPerInventorPair extends BuildingCard {
    @Serial
    private static final long serialVersionUID = 1L;

    private int currentPairs;
    private final int foodBonus;
    public FoodPerInventorPair(int cardId, int era, int numPlayers, int cost, int points, int foodBonus) {
        super(cardId, era, numPlayers, cost, points);
        currentPairs = 0;
        this.foodBonus = foodBonus;
    }

    @Override
    public void resolveEffect(GameModel gameModel){
        super.resolveEffect(gameModel);
        currentPairs = calculatePairs(gameModel);
    }

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
