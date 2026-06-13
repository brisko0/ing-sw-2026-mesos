package it.polimi.ingsw.am56.model.card.eventCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;

import java.io.Serial;

/**
 * Sustenance event: each player must pay one food per character card they own, reduced by
 * their feeding-sale discount (never below zero). Players who cannot pay in full lose
 * {@code losePointsPerChar} victory points per missing unit of food.
 */
public class SustenanceEvCard extends EventCard {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int losePointsPerChar;

    /**
     * Creates a sustenance event card.
     *
     * @param cardId                       unique card id
     * @param era                          era the card belongs to (1, 2 or 3)
     * @param minPlayers                   minimum player count required for this card to be in the deck
     * @param losePointsPerCharSustenance  victory points lost per unit of food the player cannot pay
     */
    public SustenanceEvCard(int cardId, int era, int minPlayers, boolean isFinal, int losePointsPerCharSustenance) {
        super(cardId, era, minPlayers, isFinal);
        this.losePointsPerChar = losePointsPerCharSustenance;
    }

    /**
     * Charges each player one food per character card minus the feeding-sale discount; any
     * shortfall in food translates into a loss of {@code losePointsPerChar} points per
     * missing food unit.
     *
     * @param gameModel current game model
     */
    @Override
    public void resolveEffect(GameModel gameModel) {
        for(Player player : gameModel.getPlayers()) {
            int playerFood = player.getStats().getFood();
            int haveToPay = Math.max(player.getStats().getCharacterCards().size() - player.getStats().getFeedingSale(), 0);
            if(playerFood >= haveToPay) {
                player.getStats().addFood(-haveToPay);
            }
            else {
                player.getStats().addFood(-playerFood);
                player.getStats().addPoints(-losePointsPerChar * (haveToPay - playerFood));
            }
        }
    }

    /**
     * Always returns {@link EvType#SUSTENANCE}.
     *
     * @return {@link EvType#SUSTENANCE}
     */
    @Override
    public EvType getEventType() {
        return EvType.SUSTENANCE;
    }

}
