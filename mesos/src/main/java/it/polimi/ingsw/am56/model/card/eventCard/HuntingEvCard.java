package it.polimi.ingsw.am56.model.card.eventCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterType;

import java.io.Serial;

/**
 * Hunting event: each player gains one food per hunter they own and
 * {@code winPointsPerHunter} victory points per hunter.
 */
public class HuntingEvCard extends EventCard {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int winPointsPerHunter;

    /**
     * Creates a hunting event card.
     *
     * @param cardId             unique card id
     * @param era                era the card belongs to (1, 2 or 3)
     * @param minPlayers         minimum player count required for this card to be in the deck
     * @param winPointsPerHunter victory points granted per hunter owned
     */
    public HuntingEvCard(int cardId, int era, int minPlayers, boolean isFinal, int winPointsPerHunter) {
        super(cardId, era, minPlayers, isFinal);
        this.winPointsPerHunter = winPointsPerHunter;
    }

    /**
     * For every player, adds one food per hunter they own and
     * {@code winPointsPerHunter} points per hunter.
     *
     * @param gameModel current game model
     */
    @Override
    public void resolveEffect(GameModel gameModel) {
        for(Player player : gameModel.getPlayers()) {
            int huntersNum = player.getStats().getCharacterTypeNum(CharacterType.HUNTER);
            player.getStats().addFood(huntersNum);
            player.getStats().addPoints(winPointsPerHunter * huntersNum);
        }
    }

    /**
     * Always returns {@link EvType#HUNTING}.
     *
     * @return {@link EvType#HUNTING}
     */
    @Override
    public EvType getEventType() {
        return EvType.HUNTING;
    }

}
