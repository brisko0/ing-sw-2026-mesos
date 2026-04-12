package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterType;
import it.polimi.ingsw.am56.model.card.eventCard.EvType;
import it.polimi.ingsw.am56.model.card.eventCard.EventCard;

public class FeedingSaleCharType extends BuildingCard {
    private final CharacterType characterType;
    public FeedingSaleCharType(int cardId, int era, int minPlayers, int cost, int points, CharacterType characterType) {
        super(cardId, era, minPlayers, cost, points);
        this.characterType = characterType;
    }

    @Override
    public void onEventResolving(GameModel gameModel, EventCard eventCard, Player player) {
        if(eventCard.getEventType() == EvType.SUSTENANCE){
            player.getStats().addFood(player.getStats().getCharacterTypeNum(characterType));
        }
    }
}