package it.polimi.ingsw.am56.model.card.eventCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.CardType;

public class HuntingEvCard extends EventCard {
    private final int winPointsPerHunter;

    public HuntingEvCard(int idCard, int era, int minPlayers, int winPointsPerHunter) {
        super(idCard, era, minPlayers, CardType.EVENT);
        this.winPointsPerHunter = winPointsPerHunter;
    }

    public void resolveEffect(GameModel gameModel) {
        for(Player player : gameModel.getPlayers()) {
            int huntersNum = player.getStats().getHuntersNum();
            player.getStats().addFood(huntersNum);
            player.getStats().addPoints(winPointsPerHunter * huntersNum);
        }
    }
}