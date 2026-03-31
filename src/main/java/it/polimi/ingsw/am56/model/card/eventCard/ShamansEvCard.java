package it.polimi.ingsw.am56.model.card.eventCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.CardType;

public class ShamansEvCard extends EventCard {
    private final int winPoints;
    private final int losePoints;

    public ShamansEvCard(int cardId, int era, int minPlayers, int winPoints, int losePoints) {
        super(cardId, era, minPlayers, CardType.EVENT);
        this.winPoints = winPoints;
        this.losePoints = losePoints;
    }

    public void resolveEffect(GameModel gameModel) {
        int minStars = 10;
        int maxStars = 0;
        for(Player player : gameModel.getPlayers()){
            int playerStars = player.getStats().getShamanStars();
            if(playerStars < minStars){
                minStars = playerStars;
            }
            if(playerStars > maxStars){
                maxStars = playerStars;
            }
        }
        for(Player player : gameModel.getPlayers()){
            int playerStars = player.getStats().getShamanStars();
            if(playerStars == maxStars){
                player.getStats().addPoints(winPoints);
            }
            if(playerStars ==  minStars){
                player.getStats().addPoints(-losePoints);
            }
        }
    }
}
