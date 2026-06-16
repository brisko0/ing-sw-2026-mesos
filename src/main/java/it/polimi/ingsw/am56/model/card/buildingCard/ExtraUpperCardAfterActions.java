package it.polimi.ingsw.am56.model.card.buildingCard;

import java.io.Serial;

/**
 * Building card that grants one extra upper-row card pick after every player has completed
 * the normal card-picking phase.
 */
public class ExtraUpperCardAfterActions extends BuildingCard {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates an extra-upper-card building card.
     *
     * @param cardId     unique card id
     * @param era        era the card belongs to (1, 2 or 3)
     * @param minPlayers minimum player count required for this card to be in the deck
     * @param cost       food cost to acquire the building (before the building-sale discount)
     * @param points     base victory points granted at the end of the game
     */
    public ExtraUpperCardAfterActions(int cardId, int era, int minPlayers, int cost, int points) {
        super(cardId, era, minPlayers, cost, points);
    }

    /**
     * @return {@code true}, because this is the unique building with the extra-pick rule
     */
    @Override
    public boolean grantsExtraUpperCardAfterActions() {
        return true;
    }
}
