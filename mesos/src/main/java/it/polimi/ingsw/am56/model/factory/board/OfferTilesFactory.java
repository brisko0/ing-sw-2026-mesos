package it.polimi.ingsw.am56.model.factory.board;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.am56.model.board.OfferTile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads the offer-tile configuration from the bundled {@code /json/offer_tiles.json} resource
 * and filters it for the current player count. Only tiles whose {@link OfferTile#getMinPlayers()
 * minimum player count} is met by the running game are returned, so the seven slots exposed on
 * the board match the rulebook layout for that game size.
 */
public class OfferTilesFactory {

    /**
     * Reads {@code /json/offer_tiles.json}, deserialises it into {@link OfferTile}s, and keeps
     * only the tiles unlocked for the given player count.
     *
     * @param gameSize number of players in the game
     * @return the offer tiles valid for the given player count
     * @throws RuntimeException if the resource is missing or cannot be parsed
     */
    public static List<OfferTile> createOfferTiles(int gameSize) {
        List<OfferTile> allTiles;
        List<OfferTile> filteredTiles = new ArrayList<>();
        Gson gson = new Gson();

        try (InputStream is = OfferTile.class.getResourceAsStream("/json/offer_tiles.json")) {

            if (is == null) {
                throw new RuntimeException("File JSON NON trovato!");
            }

            try (Reader reader = new InputStreamReader(is)) {

                Type listType = new TypeToken<ArrayList<OfferTile>>() {}.getType();
                allTiles = gson.fromJson(reader, listType);

                if (allTiles != null) {
                    for (OfferTile tile : allTiles) {
                        if (gameSize >= tile.getMinPlayers()) {
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
