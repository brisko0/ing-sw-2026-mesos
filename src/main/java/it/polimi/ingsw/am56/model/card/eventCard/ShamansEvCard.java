package it.polimi.ingsw.am56.model.card.eventCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;

import java.io.Serial;

public class ShamansEvCard extends EventCard {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int winPoints;
    private final int losePoints;

    public ShamansEvCard(int cardId, int era, int minPlayers, int winPoints, int losePoints) {
        super(cardId, era, minPlayers);
        this.winPoints = winPoints;
        this.losePoints = losePoints;
    }

    @Override
    public void resolveEffect(GameModel gameModel) {
        int minStars = 20;
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

    @Override
    public EvType getEventType() {
        return EvType.SHAMAN;
    }

    public int getWinPoints() {
        return winPoints;
    }

    public int getLosePoints() {
        return losePoints;
    }
}
