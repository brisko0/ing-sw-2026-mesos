package it.polimi.ingsw.am56.model.card.characterCard;

import it.polimi.ingsw.am56.model.GameModel;

import java.io.Serial;

/**
 * Artist character card. Picking the card has no immediate effect; artists score during the
 * Pitture Rupestri event ({@code ArtistsEvCard}) and through end-of-game tallies.
 */
public class ArtistCharCard extends CharacterCard {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates an artist character card.
     *
     * @param cardId     unique card id
     * @param era        era the card belongs to (1, 2 or 3)
     * @param minPlayers minimum player count required for this card to be in the deck
     */
    public ArtistCharCard(int cardId, int era, int minPlayers) {
        super( cardId, era, minPlayers);
    }

    /**
     * Always returns {@link CharacterType#ARTIST}.
     *
     * @return {@link CharacterType#ARTIST}
     */
    @Override
    public CharacterType getCharacterType() {
        return CharacterType.ARTIST;
    }

    /**
     * No-op: artists do not grant an immediate effect on pick.
     *
     * @param gameModel current game model (unused)
     */
    @Override
    public void resolveEffect(GameModel gameModel){}
}
