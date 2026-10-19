package it.polimi.ingsw.am56.model.factory.board;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.am56.model.board.OrderTile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads the turn-order-tile configuration from the bundled {@code /json/order_tiles.json}
 * resource and filters it for the current player count. Unlike {@link OfferTilesFactory},
 * each {@link OrderTile} layout is keyed to a single exact player count, so the filter uses
 * equality ({@code numPlayers == gameSize}) rather than a minimum threshold.
 */
public class OrderTilesFactory {

    /**
     * Reads {@code /json/order_tiles.json}, deserialises it into {@link OrderTile}s, and keeps
     * only the tiles belonging to the layout for the given player count.
     *
     * @param gameSize number of players in the game
     * @return the order tiles for the layout matching the given player count
     * @throws RuntimeException if the resource is missing or cannot be parsed
     */
    public static List<OrderTile> createOrderTiles(int gameSize) {
        List<OrderTile> filteredTiles = new ArrayList<>();
        Gson gson = new Gson();

        try (InputStream is = OrderTile.class.getResourceAsStream("/json/order_tiles.json")) {

            if (is == null) {
                throw new RuntimeException("File JSON NON trovato!");
            }

            try (Reader reader = new InputStreamReader(is)) {
                Type listType = new TypeToken<ArrayList<OrderTile>>(){}.getType();
                List<OrderTile> allTiles = gson.fromJson(reader, listType);

                if (allTiles != null) {
                    for (OrderTile tile : allTiles) {
                        if (gameSize == tile.getNumPlayers()) {
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
}
