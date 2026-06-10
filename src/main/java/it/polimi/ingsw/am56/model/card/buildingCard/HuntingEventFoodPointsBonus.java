package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterType;
import it.polimi.ingsw.am56.model.card.eventCard.EvType;
import it.polimi.ingsw.am56.model.card.eventCard.EventCard;

import java.io.Serial;

/**
 * Building card that, during a {@link EvType#HUNTING hunting} event, grants the owner an
 * extra unit of food and an extra victory point per hunter they own (on top of the
 * hunting event's own rewards).
 */
public class HuntingEventFoodPointsBonus extends BuildingCard {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a hunting-event food-and-points bonus building card.
     *
     * @param cardId     unique card id
     * @param era        era the card belongs to (1, 2 or 3)
     * @param numPlayers minimum player count required for this card to be in the deck
     * @param cost       food cost to acquire the building (before the building-sale discount)
     * @param points     base victory points granted at the end of the game
     */
    public HuntingEventFoodPointsBonus(int cardId, int era, int numPlayers, int cost, int points) {
        super(cardId, era, numPlayers, cost, points);
    }

    /**
     * If the resolving event is the hunting event, grants the owner one food and one victory
     * point per hunter they own. Other events are ignored.
     *
     * @param gameModel current game model
     * @param eventCard the event card being resolved
     * @param player    the owner of this building card
     */
    @Override
    public void onEventResolving(GameModel gameModel, EventCard eventCard, Player player) {
        if(eventCard.getEventType() == EvType.HUNTING){
            player.getStats().addFood(player.getStats().getCharacterTypeNum(CharacterType.HUNTER));
            player.getStats().addPoints(player.getStats().getCharacterTypeNum(CharacterType.HUNTER));
        }
    }
}
