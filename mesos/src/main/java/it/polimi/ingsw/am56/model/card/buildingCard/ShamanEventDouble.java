package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.eventCard.EvType;
import it.polimi.ingsw.am56.model.card.eventCard.EventCard;
import it.polimi.ingsw.am56.model.card.eventCard.ShamansEvCard;

import java.io.Serial;

/**
 * Building card that, during a {@link EvType#SHAMAN shaman} event, awards the owner an
 * additional copy of the event's win-points if they are tied at the highest shaman-stars
 * total — effectively doubling the shaman event reward for the winner(s).
 */
public class ShamanEventDouble extends BuildingCard {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a shaman-event-double building card.
     *
     * @param cardId     unique card id
     * @param era        era the card belongs to (1, 2 or 3)
     * @param numPlayers minimum player count required for this card to be in the deck
     * @param cost       food cost to acquire the building (before the building-sale discount)
     * @param points     base victory points granted at the end of the game
     */
    public ShamanEventDouble(int cardId, int era, int numPlayers, int cost, int points) {
        super(cardId, era, numPlayers, cost, points);
    }

    /**
     * If the resolving event is the shaman event and the owner is tied at the highest
     * shaman-stars total, adds another copy of the event's win-points to the owner. Other
     * events are ignored.
     *
     * @param gameModel current game model
     * @param evCard    the event card being resolved
     * @param player    the owner of this building card
     */
    @Override
    public void onEventResolving(GameModel gameModel, EventCard evCard, Player player) {
        if(evCard.getEventType() == EvType.SHAMAN){
            ShamansEvCard shamanCard = (ShamansEvCard) evCard;
            int maxStars = gameModel.getPlayers().stream()
                    .mapToInt(p -> p.getStats().getShamanStars())
                    .max()
                    .orElse(0);
            if(player.getStats().getShamanStars() == maxStars){
                player.getStats().addPoints(shamanCard.getWinPoints());
            }
        }
    }
}
