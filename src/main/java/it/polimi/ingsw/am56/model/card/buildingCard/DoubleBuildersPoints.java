package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;

public class DoubleBuildersPoints extends BuildingCard{
    public DoubleBuildersPoints(int cardId, int era, int numPlayers, int cost, int points){
        super(cardId, era, numPlayers, cost, points);
    }

    @Override
    public void onGameEnding(GameModel gameModel, Player player) {
        super.onGameEnding(gameModel, player);
        player.getStats().addPoints(player.getStats().getBuilderPoints());
    }
}