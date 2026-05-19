package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;

import java.io.Serial;

public class AddPointsEndGame extends BuildingCard {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int bonusPoints;

    public AddPointsEndGame(int cardId, int era, int numPlayers, int cost, int points, int bonusPoints) {
        super(cardId, era, numPlayers, cost, points);
        this.bonusPoints = bonusPoints;
    }

    @Override
    public void onGameEnding(GameModel gameModel, Player player) {
        super.onGameEnding(gameModel, player);
        player.getStats().addPoints(bonusPoints);
    }

}