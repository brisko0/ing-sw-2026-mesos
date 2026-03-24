package it.polimi.ingsw.am56.model.board;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken; // Fondamentale per TypeToken
import java.lang.reflect.Type;               // Fondamentale per Type
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;

public class OfferTile {
    // string a causa di JSON
    private final String letter;
    private final int food;
    private final int upperCards;
    private final int lowerCards;
    private final int minPlayers;
    private boolean isFree = true;

    public OfferTile(String letter, int food, int upperCards, int lowerCards, int minPlayers) {
        this.letter = letter;
        this.food = food;
        this.upperCards = upperCards;
        this.lowerCards = lowerCards;
        this.minPlayers = minPlayers;
    }

    public static List<OfferTile> createOfferTiles(int gameSize) {
        List<OfferTile> allTiles;
        List<OfferTile> filteredTiles = new ArrayList<>();
        Gson gson = new Gson();

        try (InputStream is = OfferTile.class.getResourceAsStream("/it/polimi/ingsw/am56/json/offer_tiles.json")) {

            if (is == null) {
                throw new RuntimeException("File JSON NON trovato!");
            }

            try (Reader reader = new InputStreamReader(is)) {

                Type listType = new TypeToken<ArrayList<OfferTile>>() {
                }.getType();
                allTiles = gson.fromJson(reader, listType);

                if (allTiles != null) {
                    for (OfferTile tile : allTiles) {
                        if (gameSize >= tile.minPlayers) {
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

    public String getLetter() {
        return letter;
    }

    public int getUpperCards() {
        return upperCards;
    }

    public int getLowerCards() {
        return lowerCards;
    }

    public boolean isFree() {
        return isFree;
    }

    public void setIsFree(boolean isFree) {
        this.isFree = isFree;
    }
}
