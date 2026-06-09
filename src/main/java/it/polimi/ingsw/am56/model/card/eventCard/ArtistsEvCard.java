package it.polimi.ingsw.am56.model.card.eventCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterType;

import java.io.Serial;

/**
 * Pitture Rupestri (artists) event: players with fewer than {@code minArtists} artists lose
 * {@code losePoints} victory points; the rest gain {@code winPointsPerArtist} per artist
 * they own. The exact thresholds and amounts vary by era (configured via JSON).
 */
public class ArtistsEvCard extends EventCard {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int minArtists;
    private final int losePoints;
    private final int winPointsPerArtist;

    /**
     * Creates an artists event card.
     *
     * @param cardId             unique card id
     * @param era                era the card belongs to (1, 2 or 3)
     * @param numPlayers         minimum player count required for this card to be in the deck
     * @param minArtists         threshold of artists needed to score positively
     * @param losePoints         victory points subtracted from players below the threshold
     * @param winPointsPerArtist victory points granted per artist owned by players at or above the threshold
     */
    public ArtistsEvCard(int cardId, int era, int numPlayers, int minArtists, int losePoints, int winPointsPerArtist) {
        super(cardId, era, numPlayers);
        this.minArtists = minArtists;
        this.losePoints = losePoints;
        this.winPointsPerArtist = winPointsPerArtist;
    }

    /**
     * For each player: if their artist count is below {@code minArtists} they lose
     * {@code losePoints} points, otherwise they gain {@code winPointsPerArtist} points per
     * artist they own.
     *
     * @param gameModel current game model
     */
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

    /**
     * Always returns {@link EvType#ARTIST}.
     *
     * @return {@link EvType#ARTIST}
     */
    @Override
    public EvType getEventType() {
        return EvType.ARTIST;
    }

}
