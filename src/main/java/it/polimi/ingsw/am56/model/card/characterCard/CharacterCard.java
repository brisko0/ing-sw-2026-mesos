package it.polimi.ingsw.am56.model.card.characterCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.card.CardType;
import it.polimi.ingsw.am56.model.card.buildingCard.BuildingCard;

import java.io.Serial;

public abstract class CharacterCard extends Card {
    @Serial
    private static final long serialVersionUID = 1L;

    public CharacterCard(int cardId, int era, int minPlayers) {
        super( cardId, era, minPlayers);
    }

    public abstract CharacterType getCharacterType();

    @Override
    public void resolveBuildingsEffects(GameModel gameModel) {
        for(BuildingCard buildingCard : gameModel.getCurrentPlayer().getStats().getBuildingCards()) {
            buildingCard.onCardPicked(gameModel, this);
        }
    }

    @Override
    public void addCardToPlayer(Player player){
        player.getStats().getCharacterCards().add(this);
    }

    @Override
    public CardType getCardType() {
        return CardType.CHARACTER;
    }

}
