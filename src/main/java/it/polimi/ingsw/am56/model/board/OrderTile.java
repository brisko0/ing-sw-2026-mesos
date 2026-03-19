package it.polimi.ingsw.am56.model.board;

public class OrderTile {
    private final int OrderTileId;
    private final int foodEffect;
    private final int pointsEffect;
    private boolean isFree;

    public OrderTile(int orderTileId, int foodEffect, int pointsEffect) {
        OrderTileId = orderTileId;
        this.foodEffect = foodEffect;
        this.pointsEffect = pointsEffect;
        isFree = true;
    }

    public int getOrderTileId(){
        return OrderTileId;
    }

    public int getFoodEffect() {
        return foodEffect;
    }

    public int getPointsEffect() {
        return pointsEffect;
    }

    public boolean isFree(){
        return isFree;
    }
}
