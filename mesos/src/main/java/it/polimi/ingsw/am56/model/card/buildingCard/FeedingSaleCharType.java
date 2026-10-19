package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterType;
import it.polimi.ingsw.am56.model.card.eventCard.EvType;
import it.polimi.ingsw.am56.model.card.eventCard.EventCard;

import java.io.Serial;

/**
 * Building card that, during a {@link EvType#SUSTENANCE sustenance} event, grants the owner
 * one extra food per character of a specific {@link CharacterType}. Acts as a per-character
 * feeding discount tied to that archetype.
 */
public class FeedingSaleCharType extends BuildingCard {
    @Serial
    private static final long serialVersionUID = 1L;

    private final CharacterType characterType;

    /**
     * Creates a feeding-sale-per-character-type building card.
     *
     * @param cardId        unique card id
     * @param era           era the card belongs to (1, 2 or 3)
     * @param minPlayers    minimum player count required for this card to be in the deck
     * @param cost          food cost to acquire the building (before the building-sale discount)
     * @param points        base victory points granted at the end of the game
     * @param characterType the character archetype this card rewards
     */
    public FeedingSaleCharType(int cardId, int era, int minPlayers, int cost, int points, CharacterType characterType) {
        super(cardId, era, minPlayers, cost, points);
        this.characterType = characterType;
    }

    /**
     * If the resolving event is the sustenance event, grants the owner one food per character
     * card of {@code characterType} they own. Other events are ignored.
     *
     * @param gameModel current game model
     * @param eventCard the event card being resolved
     * @param player    the owner of this building card
     */
    @Override
    public void onEventResolving(GameModel gameModel, EventCard eventCard, Player player) {
        if(eventCard.getEventType() == EvType.SUSTENANCE){
            player.getStats().addFood(player.getStats().getCharacterTypeNum(characterType));
        }
    }
}
