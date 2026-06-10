package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.eventCard.EvType;
import it.polimi.ingsw.am56.model.card.eventCard.EventCard;
import it.polimi.ingsw.am56.model.card.eventCard.ShamansEvCard;

import java.io.Serial;

/**
 * Building card that, during a {@link EvType#SHAMAN shaman} event, refunds the owner the
 * event's lose-points if they are tied at the lowest shaman-stars total — effectively
 * cancelling out the shaman event penalty for the loser(s).
 */
public class ShamanNoLoss extends BuildingCard {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a shaman-no-loss building card.
     *
     * @param cardId     unique card id
     * @param era        era the card belongs to (1, 2 or 3)
     * @param numPlayers minimum player count required for this card to be in the deck
     * @param cost       food cost to acquire the building (before the building-sale discount)
     * @param points     base victory points granted at the end of the game
     */
    public ShamanNoLoss(int cardId, int era, int numPlayers, int cost, int points) {
        super(cardId, era, numPlayers, cost, points);
    }

    /**
     * If the resolving event is the shaman event and the owner is tied at the lowest
     * shaman-stars total, adds the event's lose-points back to the owner, cancelling the
     * penalty. Other events are ignored.
     *
     * @param gameModel current game model
     * @param evCard    the event card being resolved
     * @param player    the owner of this building card
     */
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
