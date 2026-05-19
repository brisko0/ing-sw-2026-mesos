package it.polimi.ingsw.am56.model.card.characterCard;

import it.polimi.ingsw.am56.model.GameModel;

import java.io.Serial;

public class ArtistCharCard extends CharacterCard {
    @Serial
    private static final long serialVersionUID = 1L;

    public ArtistCharCard(int cardId, int era, int minPlayers) {
        super( cardId, era, minPlayers);
    }

    @Override
    public CharacterType getCharacterType() {
        return CharacterType.ARTIST;
    }

    @Override
    public void resolveEffect(GameModel gameModel){}
}
