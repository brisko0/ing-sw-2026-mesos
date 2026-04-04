package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;

public class PointsPerSet extends BuildingCard{
    private final int bonusPoints;

    public PointsPerSet(int cardId, int era, int minPlayers, int cost, int points, int bonusPoints) {
        super(cardId, era, minPlayers, cost, points);
        this.bonusPoints = bonusPoints;
    }
    @Override
    public void onGameEnding(GameModel gameModel, Player player) {
        super.onGameEnding(gameModel, player);
        player.getStats().addPoints(player.getStats().getCompletedSets() * bonusPoints);
    }
}