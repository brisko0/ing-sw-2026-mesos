package it.polimi.ingsw.am56.model.card.eventCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterType;

import java.io.Serial;

public class ArtistsEvCard extends EventCard {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int minArtists;
    private final int losePoints;
    private final int winPointsPerArtist;

    public ArtistsEvCard(int cardId, int era, int numPlayers, int minArtists, int losePoints, int winPointsPerArtist) {
        super(cardId, era, numPlayers);
        this.minArtists = minArtists;
        this.losePoints = losePoints;
        this.winPointsPerArtist = winPointsPerArtist;
    }

    @Override
    public void resolveEffect(GameModel gameModel) {
        for(Player player : gameModel.getPlayers()) {
            int artistsNum = player.getStats().getCharacterTypeNum(CharacterType.ARTIST);
            if(artistsNum < minArtists) {
                player.getStats().addPoints(-losePoints);
            }
            else {
                player.getStats().addPoints(winPointsPerArtist * artistsNum);
            }
        }
    }

    @Override
    public EvType getEventType() {
        return EvType.ARTIST;
    }

}
