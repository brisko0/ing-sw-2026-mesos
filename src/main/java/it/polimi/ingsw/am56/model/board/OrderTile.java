package it.polimi.ingsw.am56.model.board;

import java.io.Serial;
import java.io.Serializable;

/**
 * Turn-order tile picked by each player at the start of a round. The tile determines the
 * player's position in the turn sequence and applies a food / victory-points modifier (the
 * last tile in each player-count layout carries a penalty). The {@code isFree} flag tracks
 * whether the tile is still available to pick.
 */
public class OrderTile implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int orderTileId;
    private final int foodEffect;
    private final int pointsEffect;
    private final int numPlayers;
    private boolean isFree = true;

    /**
     * Creates a turn-order tile.
     *
     * @param orderTileId  1-based position of the tile within its player-count layout
     * @param foodEffect   food modifier applied to the player who takes this tile
     * @param pointsEffect victory-points modifier applied to the player who takes this tile
     * @param numPlayers   player count this tile belongs to
     */
    public OrderTile(int orderTileId, int foodEffect, int pointsEffect, int numPlayers) {
        this.orderTileId = orderTileId;
        this.foodEffect = foodEffect;
        this.pointsEffect = pointsEffect;
        this.numPlayers = numPlayers;
    }

    /**
     * Returns the tile's 1-based position within its player-count layout.
     *
     * @return the order-tile id
     */
    public int getOrderTileId(){
        return orderTileId;
    }

    /**
     * Returns the food modifier applied when this tile is taken.
     *
     * @return the food effect
     */
    public int getFoodEffect() {
        return foodEffect;
    }

    /**
     * Returns the victory-points modifier applied when this tile is taken.
     *
     * @return the points effect
     */
    public int getPointsEffect() {
        return pointsEffect;
    }

    /**
     * Returns the player count this tile belongs to. Tiles are filtered by this value at
     * setup so that only the layout matching the current game size is exposed to players.
     *
     * @return the player count this tile is valid for
     */
    public int getNumPlayers() {
        return numPlayers;
    }

    /**
     * Returns whether the tile is still available to pick this round.
     *
     * @return {@code true} if no player has claimed this tile yet
     */
    public boolean isFree(){
        return isFree;
    }

    /**
     * Marks the tile available or taken.
     *
     * @param isFree {@code true} to make the tile available, {@code false} to mark it taken
     */
    public void setIsFree(boolean isFree){
        this.isFree = isFree;
    }
}