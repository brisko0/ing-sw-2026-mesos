package it.polimi.ingsw.am56.model.board;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;

public class OrderTile {
    private final int orderTileId;
    private final int foodEffect;
    private final int pointsEffect;
    private final int numPlayers;
    private boolean isFree = true;

    public OrderTile(int orderTileId, int foodEffect, int pointsEffect, int numPlayers) {
        this.orderTileId = orderTileId;
        this.foodEffect = foodEffect;
        this.pointsEffect = pointsEffect;
        this.numPlayers = numPlayers;
    }

    public static List<OrderTile> createOrderTiles(int gameSize) {
        List<OrderTile> filteredTiles = new ArrayList<>();
        Gson gson = new Gson();

        try (InputStream is = OrderTile.class.getResourceAsStream("/it/polimi/ingsw/am56/json/order_tiles.json")) {

            if (is == null) {
                throw new RuntimeException("File JSON NON trovato!");
            }

            try (Reader reader = new InputStreamReader(is)) {
                Type listType = new TypeToken<ArrayList<OrderTile>>(){}.getType();
                List<OrderTile> allTiles = gson.fromJson(reader, listType);

                if (allTiles != null) {
                    for (OrderTile tile : allTiles) {
                        if (gameSize == tile.numPlayers) {
                            filteredTiles.add(tile);
                        }
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Impossibile inizializzare il gioco: file di configurazione mancante o corrotto", e);
        }

        return filteredTiles;
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