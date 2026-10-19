package it.polimi.ingsw.am56.model.board;

import java.io.Serial;
import java.io.Serializable;

/**
 * One of the totem-placement slots on the board, identified by a letter ({@code A}–{@code G}).
 * Placing a totem on the slot grants its reward — a food bonus and / or the right to pick a
 * fixed number of cards from the upper / lower rows. Some letters are unlocked only above a
 * minimum player count. The {@code isFree} flag tracks whether the slot is still empty in the
 * current round.
 */
public class OfferTile implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String letter;
    private final int food;
    private final int upperCards;
    private final int lowerCards;
    private final int minPlayers;
    private boolean isFree;

    /**
     * Creates an offer tile.
     *
     * @param letter     letter id of the slot ({@code A}..{@code G})
     * @param food       food bonus granted by claiming this slot
     * @param upperCards number of cards the claimer may pick from the upper row
     * @param lowerCards number of cards the claimer may pick from the lower row
     * @param minPlayers minimum player count required for this slot to be in play
     * @param isFree     initial free / claimed state
     */
    public OfferTile(String letter, int food, int upperCards, int lowerCards, int minPlayers, boolean isFree) {
        this.letter = letter;
        this.food = food;
        this.upperCards = upperCards;
        this.lowerCards = lowerCards;
        this.minPlayers = minPlayers;
        this.isFree = isFree;
    }

    /**
     * Returns the slot's letter id.
     *
     * @return the letter (e.g. {@code "A"}, {@code "B"}, …)
     */
    public String getLetter() {
        return letter;
    }

    /**
     * Returns the food bonus granted by claiming this slot.
     *
     * @return the food bonus
     */
    public int getFoodBonus() {
        return food;
    }

    /**
     * Returns the number of cards the claimer may pick from the upper row.
     *
     * @return the upper-row pick count
     */
    public int getUpperCards() {
        return upperCards;
    }

    /**
     * Returns the number of cards the claimer may pick from the lower row.
     *
     * @return the lower-row pick count
     */
    public int getLowerCards() {
        return lowerCards;
    }

    /**
     * Returns the minimum player count required for this slot to be in play.
     *
     * @return the minimum player count
     */
    public int getMinPlayers() {
        return minPlayers;
    }

    /**
     * Returns whether the slot has no totem on it this round.
     *
     * @return {@code true} if the slot is still free
     */
    public boolean isFree() {
        return isFree;
    }

    /**
     * Marks the slot free or claimed.
     *
     * @param isFree {@code true} to mark the slot free, {@code false} to mark it claimed
     */
    public void setIsFree(boolean isFree) {
        this.isFree = isFree;
    }
}
