package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;

import java.io.Serial;

/**
 * Building card that grants extra upper-row card picks after every player has completed
 * the normal card-picking phase.
 */
public class ExtraUpperCardAfterActions extends BuildingCard {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int upperCardBonus;

    /**
     * Creates an extra-upper-card building card.
     *
     * @param cardId         unique card id
     * @param era            era the card belongs to (1, 2 or 3)
     * @param minPlayers     minimum player count required for this card to be in the deck
     * @param cost           food cost to acquire the building (before the building-sale discount)
     * @param points         base victory points granted at the end of the game
     * @param upperCardBonus number of extra upper-row cards granted after normal picking
     */
    public ExtraUpperCardAfterActions(int cardId, int era, int minPlayers, int cost, int points, int upperCardBonus) {
        super(cardId, era, minPlayers, cost, points);
        this.upperCardBonus = upperCardBonus;
    }

    /**
     * Returns the configured number of extra upper-row picks granted to the owner.
     *
     * @param gameModel current game model
     * @param player    the owner of this building card
     * @return the number of extra upper-row cards the owner may pick
     */
    @Override
    public int onFinishedPicking(GameModel gameModel, Player player) {
        return upperCardBonus;
    }
}
