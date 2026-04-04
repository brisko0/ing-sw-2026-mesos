package it.polimi.ingsw.am56.model.card.characterCard;

import it.polimi.ingsw.am56.model.GameModel;

public class ArtistCharCard extends CharacterCard {

    public ArtistCharCard(int cardId, int era, int minPlayers) {
        super( cardId, era, minPlayers);
    }

    @Override
    public void resolveEffect(GameModel gameModel){
        gameModel.getCurrentPlayer().getStats().addArtist();
    }

    @Override
    public CharacterType getCharacterType(){
        return CharacterType.ARTIST;
    }
}