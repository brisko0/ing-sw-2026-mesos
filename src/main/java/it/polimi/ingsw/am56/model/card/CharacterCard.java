package it.polimi.ingsw.am56.model.card;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CharacterCard extends Card{
    private final CharType type;
    private final int shamanStars;
    private final int buildingSale;
    private final int builderPoints;
    private final int feedingSale;
    private final String invention;
    private final boolean providesFood;

    public CharacterCard(int cardId, int era, int minPlayers, CharType type,
                         int shamanStars, int buildingSale, int builderPoints,
                         int feedingSale, String invention, boolean providesFood) {
        super( cardId, era, minPlayers, CardType.CHARACTER);
        this.type = type;
        this.shamanStars = shamanStars;
        this.buildingSale = buildingSale;
        this.builderPoints = builderPoints;
        this.feedingSale = feedingSale;
        this.invention = invention;
        this.providesFood = providesFood;
    }

    public static List<CharacterCard> createCharCards(int era, int gameSize){
        List<CharacterCard> charCards = new ArrayList<>();
        List<CharacterCard> filteredCards = new ArrayList<>();
        Gson gson = new Gson();

        try (InputStream is = CharacterCard.class.getResourceAsStream("/it/polimi/ingsw/am56/json/character_cards.json")) {

            if (is == null) {
                throw new RuntimeException("File JSON NON trovato!");
            }

            try (Reader reader = new InputStreamReader(is)) {

                Type listType = new TypeToken<ArrayList<CharacterCard>>() {
                }.getType();
                charCards = gson.fromJson(reader, listType);

                if (charCards != null) {
                    for (CharacterCard card : charCards) {
                        if (gameSize >= card.getMinPlayers() && card.getEra() == era) {
                            filteredCards.add(card);
                        }
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Impossibile inizializzare il gioco: file di configurazione mancante o corrotto", e);
        }

        return filteredCards;
    }

    public CharType getType() {
        return type;
    }

    public int getShamanStars() {
        return shamanStars;
    }

    public int getBuildingSale() {
        return buildingSale;
    }

    public int getBuilderPoints() {
        return builderPoints;
    }

    public int getFeedingSale() {
        return feedingSale;
    }

    public String getInvention() {
        return invention;
    }

    public boolean doesProvideFood() {
        return providesFood;
    }

    @Override
    public void resolveEffect(){

    }
}
