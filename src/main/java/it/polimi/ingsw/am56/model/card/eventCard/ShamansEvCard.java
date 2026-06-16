package it.polimi.ingsw.am56.model.card.eventCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;

import java.io.Serial;

/**
 * Shaman event: compares the shaman-star totals of all players. Player(s) tied at the
 * highest total gain {@code winPoints}; player(s) tied at the lowest total lose
 * {@code losePoints}. When max equals min (all tied), every player both gains and loses.
 */
public class ShamansEvCard extends EventCard {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int winPoints;
    private final int losePoints;

    /**
     * Creates a shaman event card.
     *
     * @param cardId     unique card id
     * @param era        era the card belongs to (1, 2 or 3)
     * @param minPlayers minimum player count required for this card to be in the deck
     * @param winPoints  victory points granted to the player(s) with the most shaman stars
     * @param losePoints victory points subtracted from the player(s) with the fewest shaman stars
     */
    public ShamansEvCard(int cardId, int era, int minPlayers, boolean isFinal, int winPoints, int losePoints) {
        super(cardId, era, minPlayers, isFinal);
        this.winPoints = winPoints;
        this.losePoints = losePoints;
    }

    /**
     * Awards {@code winPoints} to every player tied at the highest shaman-stars count and
     * subtracts {@code losePoints} from every player tied at the lowest count.
     *
     * @param gameModel current game model
     */
    @Override
    public void resolveEffect(GameModel gameModel) {
        int minStars = Integer.MAX_VALUE;
        int maxStars = 0;
        for(Player player : gameModel.getPlayers()){
            int playerStars = player.getStats().getShamanStars();
            if(playerStars < minStars){
                minStars = playerStars;
            }
            if(playerStars > maxStars){
                maxStars = playerStars;
            }
        }
        for(Player player : gameModel.getPlayers()){
            int playerStars = player.getStats().getShamanStars();
            if(playerStars == maxStars){
                player.getStats().addPoints(winPoints);
            }
            if(playerStars ==  minStars){
                player.getStats().addPoints(-losePoints);
            }
        }
    }

    /**
     * Always returns {@link EvType#SHAMAN}.
     *
     * @return {@link EvType#SHAMAN}
     */
    @Override
    public EvType getEventType() {
        return EvType.SHAMAN;
    }

    /**
     * Returns the points granted to the player(s) with the most shaman stars.
     *
     * @return the win-points amount
     */
    public int getWinPoints() {
        return winPoints;
    }

    /**
     * Returns the points subtracted from the player(s) with the fewest shaman stars.
     *
     * @return the lose-points amount
     */
    public int getLosePoints() {
        return losePoints;
    }
}
