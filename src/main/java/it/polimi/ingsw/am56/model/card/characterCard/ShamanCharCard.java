package it.polimi.ingsw.am56.model.card.characterCard;

import it.polimi.ingsw.am56.model.GameModel;

import java.io.Serial;

public class ShamanCharCard extends CharacterCard{
    @Serial
    private static final long serialVersionUID = 1L;

    private final int shamanStars;

    public ShamanCharCard(int cardId, int era, int minPlayers, int shamanStars) {
        super( cardId, era, minPlayers);
        this.shamanStars = shamanStars;
    }

    @Override
    public CharacterType getCharacterType() {
        return CharacterType.SHAMAN;
    }

    @Override
    public void resolveEffect(GameModel gameModel){

        gameModel.getCurrentPlayer().getStats().addShamanStars(shamanStars);

    }
}
