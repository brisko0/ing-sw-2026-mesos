package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.eventCard.EvType;
import it.polimi.ingsw.am56.model.card.eventCard.EventCard;
import it.polimi.ingsw.am56.model.card.eventCard.ShamansEvCard;

public class ShamanNoLoss extends BuildingCard {
    public ShamanNoLoss(int cardId, int era, int numPlayers, int cost, int points) {
        super(cardId, era, numPlayers, cost, points);
    }
    @Override
    public void onEventResolving(GameModel gameModel, EventCard evCard, Player player) {
        if(evCard.getEventType() == EvType.SHAMAN){
            ShamansEvCard shamanCard = (ShamansEvCard) evCard;
            int minStars = gameModel.getPlayers().stream()
                    .mapToInt(p -> p.getStats().getShamanStars())
                    .min()
                    .orElse(0);
            if(player.getStats().getShamanStars() == minStars){
                player.getStats().addPoints(shamanCard.getLosePoints());
            }
        }
    }
}
