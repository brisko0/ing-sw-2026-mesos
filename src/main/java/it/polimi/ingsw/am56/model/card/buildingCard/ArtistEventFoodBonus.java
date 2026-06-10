package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterType;
import it.polimi.ingsw.am56.model.card.eventCard.EvType;
import it.polimi.ingsw.am56.model.card.eventCard.EventCard;

import java.io.Serial;

/**
 * Building card that grants the owner one food per artist they own whenever the
 * {@link EvType#ARTIST Pitture Rupestri} event is resolved.
 */
public class ArtistEventFoodBonus extends BuildingCard {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates an artist-event food-bonus building card.
     *
     * @param cardId     unique card id
     * @param era        era the card belongs to (1, 2 or 3)
     * @param numPlayers minimum player count required for this card to be in the deck
     * @param cost       food cost to acquire the building (before the building-sale discount)
     * @param points     base victory points granted at the end of the game
     */
    public ArtistEventFoodBonus(int cardId, int era, int numPlayers, int cost, int points) {
        super(cardId, era, numPlayers, cost, points);
    }

    /**
     * If the resolving event is the artist event, adds one food to the owner per artist they
     * own. Other events are ignored.
     *
     * @param gameModel current game model
     * @param eventCard the event card being resolved
     * @param player    the owner of this building card
     */
    @Override
    public void onEventResolving(GameModel gameModel, EventCard eventCard, Player player) {
        if(eventCard.getEventType() == EvType.ARTIST)
        {
            player.getStats().addFood(player.getStats().getCharacterTypeNum(CharacterType.ARTIST));
        }
    }

}
