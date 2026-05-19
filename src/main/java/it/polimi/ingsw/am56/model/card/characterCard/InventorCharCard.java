package it.polimi.ingsw.am56.model.card.characterCard;

import it.polimi.ingsw.am56.model.GameModel;

import java.io.Serial;


public class InventorCharCard extends CharacterCard {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String invention;

    public InventorCharCard(int cardId, int era, int minPlayers, String invention) {
        super( cardId, era, minPlayers);
        this.invention = invention;
    }

    @Override
    public CharacterType getCharacterType() {
        return CharacterType.INVENTOR;
    }

    @Override
    public void resolveEffect(GameModel gameModel){

        gameModel.getCurrentPlayer().getStats().getInventions().add(invention);

    }
}
