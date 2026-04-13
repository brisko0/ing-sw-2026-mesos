package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;

public class FoodBonusOrderTile extends BuildingCard{
    private final int foodBonus;
    public FoodBonusOrderTile(int cardId, int era, int numPlayers, int cost, int points, int foodBonus) {
        super(cardId, era, numPlayers, cost, points);
        this.foodBonus = foodBonus;
    }

    @Override
    public void onOwnTurnEnding(GameModel gameModel) {
        if(gameModel.getPlayerToOrderTile().get(gameModel.getCurrentPlayer()).getFoodEffect() > 0){
            gameModel.getCurrentPlayer().getStats().addFood(foodBonus);
        }
    }
}