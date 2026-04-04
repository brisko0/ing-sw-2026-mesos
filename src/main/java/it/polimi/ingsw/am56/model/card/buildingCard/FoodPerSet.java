package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterCard;

public class FoodPerSet extends BuildingCard {
    private int lastCheckedSets;
    private final int bonusFood;

    public FoodPerSet(int cardId, int era, int minPlayers, int cost, int points, int bonusFood) {
        super(cardId, era, minPlayers, cost, points);
        this.bonusFood = bonusFood;
    }
    @Override
    public void resolveEffect(GameModel gameModel) {
        super.resolveEffect(gameModel);
        this.lastCheckedSets = gameModel.getCurrentPlayer().getStats().getCompletedSets();
    }
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