package it.polimi.ingsw.am56.model.card.eventCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterType;

public class ArtistsEvCard extends EventCard {
    private final int minArtists;
    private final int losePoints;
    private final int winPointsPerArtist;

    public ArtistsEvCard(int cardId, int era, int minPlayers, int minArtists, int losePoints, int winPointsPerArtist) {
        super(cardId, era, minPlayers);
        this.minArtists = minArtists;
        this.losePoints = losePoints;
        this.winPointsPerArtist = winPointsPerArtist;
    }

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