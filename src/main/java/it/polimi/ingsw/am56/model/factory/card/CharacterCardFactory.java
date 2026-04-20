package it.polimi.ingsw.am56.model.factory.card;

import com.google.gson.*;
import it.polimi.ingsw.am56.model.card.characterCard.*;
import it.polimi.ingsw.am56.model.factory.CardFactory;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CharacterCardFactory implements CardFactory<CharacterCard> {
    private final Gson gson = new Gson();


    private final Map<String, Class<? extends CharacterCard>> typeToClass = Map.of(
            "ARTIST", ArtistCharCard.class,
            "BUILDER", BuilderCharCard.class,
            "COLLECTOR", CollectorCharCard.class,
            "HUNTER", HunterCharCard.class,
            "INVENTOR", InventorCharCard.class,
            "SHAMAN", ShamanCharCard.class
    );

    @Override
    public List<CharacterCard> createCards(int era, int gameSize) {
        List<CharacterCard> filteredCards = new ArrayList<>();
        String path = "/json/characterCards.json";

        try (var is = getClass().getResourceAsStream(path)) {
            if (is == null) throw new RuntimeException("File non trovato: " + path);

            try (Reader reader = new InputStreamReader(is)) {
                JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();

                for (JsonElement element : jsonArray) {
                    JsonObject jsonObject = element.getAsJsonObject();

                    String classType = jsonObject.get("classType").getAsString();
                    int cardEra = jsonObject.get("era").getAsInt();
                    int minPlayers = jsonObject.get("minPlayers").getAsInt();

                    if (cardEra == era && gameSize >= minPlayers) {
                        Class<? extends CharacterCard> targetClass = typeToClass.get(classType);
                        if (targetClass != null) {
                            filteredCards.add(gson.fromJson(element, targetClass));
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Errore durante il caricamento dei personaggi", e);
        }

        return filteredCards;
    }
}
