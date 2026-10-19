package it.polimi.ingsw.am56.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * One participant in the game. Wraps the immutable identity (nickname + totem colour), the
 * per-round picks counters (how many cards the player has drawn from the upper / lower rows
 * for the offer tile they are currently resolving), and the mutable {@link PlayerStats}
 * holding food, points and owned cards.
 * <p>
 * Both nickname and totem colour are uniqueness-checked by {@link it.polimi.ingsw.am56.model.state.PlayersFilling}
 * when the player joins the lobby.
 */
public class Player implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String nickname;
    private final String totemColor;
    private int pickedUpper;
    private int pickedLower;
    private final PlayerStats stats;


    /**
     * Creates a new player with a fresh, empty {@link PlayerStats}. The picks counters are
     * initialised to zero.
     *
     * @param nickname unique display name
     * @param color    unique totem colour
     */
    public Player(String nickname, String color){
        this.nickname = nickname;
        this.totemColor = color;
        pickedUpper = 0;
        pickedLower = 0;
        this.stats = new PlayerStats();
    }

    /**
     * Returns the player's display name.
     *
     * @return the nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Returns the colour of this player's totem.
     *
     * @return the totem colour
     */
    public String getTotemColor() {
        return totemColor;
    }

    /**
     * Returns how many cards this player has already picked from the upper row for the offer
     * tile they are currently resolving. Compared against {@code OfferTile.getUpperCards()}
     * to enforce the row quota.
     *
     * @return the upper-row pick count
     */
    public int getPickedUpper() {
        return pickedUpper;
    }

    /**
     * Returns how many cards this player has already picked from the lower row for the offer
     * tile they are currently resolving. Compared against {@code OfferTile.getLowerCards()}
     * to enforce the row quota.
     *
     * @return the lower-row pick count
     */
    public int getPickedLower() {
        return pickedLower;
    }

    /**
     * Returns the mutable per-player stats object (food, points, owned cards, …).
     *
     * @return the stats backing this player
     */
    public PlayerStats getStats() {
        return stats;
    }

    /**
     * Sets the upper-row pick counter. Typically zeroed by the state machine at the start of
     * a new offer-tile resolution, and incremented as the player picks.
     *
     * @param pickedUpper new upper-row pick count
     */
    public void setPickedUpper(int pickedUpper) {
        this.pickedUpper = pickedUpper;
    }

    /**
     * Sets the lower-row pick counter. Typically zeroed by the state machine at the start of
     * a new offer-tile resolution, and incremented as the player picks.
     *
     * @param pickedLower new lower-row pick count
     */
    public void setPickedLower(int pickedLower) {
        this.pickedLower = pickedLower;
    }
}
