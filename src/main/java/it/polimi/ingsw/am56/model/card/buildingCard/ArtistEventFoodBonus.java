package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterType;
import it.polimi.ingsw.am56.model.card.eventCard.EvType;
import it.polimi.ingsw.am56.model.card.eventCard.EventCard;

import java.io.Serial;

public class ArtistEventFoodBonus extends BuildingCard {
    @Serial
    private static final long serialVersionUID = 1L;

    public ArtistEventFoodBonus(int cardId, int era, int numPlayers, int cost, int points) {
        super(cardId, era, numPlayers, cost, points);
    }

    @Override
    public void onEventResolving(GameModel gameModel, EventCard eventCard, Player player) {
        if(eventCard.getEventType() == EvType.ARTIST)
        {
            player.getStats().addFood(player.getStats().getCharacterTypeNum(CharacterType.ARTIST));
        }
    }

}