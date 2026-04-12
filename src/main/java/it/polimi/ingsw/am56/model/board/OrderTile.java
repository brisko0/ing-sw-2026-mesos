package it.polimi.ingsw.am56.model.board;

public class OrderTile {
    private final int orderTileId;
    private final int foodEffect;
    private final int pointsEffect;
    private final int numPlayers;
    private boolean isFree;

    public OrderTile(int orderTileId, int foodEffect, int pointsEffect, int numPlayers,  boolean isFree) {
        this.orderTileId = orderTileId;
        this.foodEffect = foodEffect;
        this.pointsEffect = pointsEffect;
        this.numPlayers = numPlayers;
        this.isFree = isFree;
    }

    public int getOrderTileId(){
        return orderTileId;
    }

    public int getFoodEffect() {
        return foodEffect;
    }

    public int getPointsEffect() {
        return pointsEffect;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public boolean isFree(){
        return isFree;
    }

    public void setIsFree(boolean isFree){
        this.isFree = isFree;
    }
}