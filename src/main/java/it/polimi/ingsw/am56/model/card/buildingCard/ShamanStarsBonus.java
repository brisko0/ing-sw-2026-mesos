package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;

import java.io.Serial;

public class ShamanStarsBonus extends BuildingCard {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int bonusStars;
    public ShamanStarsBonus(int cardId, int era, int numPlayers, int cost, int points, int bonusPoints) {
        super(cardId, era, numPlayers, cost, points);
        this.bonusStars = bonusPoints;
    }

    @Override
    public void resolveEffect(GameModel  gameModel) {
        super.resolveEffect(gameModel);
        gameModel.getCurrentPlayer().getStats().addShamanStars(bonusStars);
    }
}
