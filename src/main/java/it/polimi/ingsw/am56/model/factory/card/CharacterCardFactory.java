package it.polimi.ingsw.am56.model.factory.card;

import com.google.gson.*;
import it.polimi.ingsw.am56.model.card.characterCard.*;
import it.polimi.ingsw.am56.model.factory.CardFactory;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * {@link CardFactory} implementation that loads {@link CharacterCard}s from
 * {@code /json/characterCards.json}. Each JSON entry carries a {@code classType} discriminator
 * (the character archetype, e.g. {@code "ARTIST"}) that selects the concrete subclass to
 * deserialise into.
 * <p>
 * Characters are filtered by both era and the per-card {@code minPlayers} threshold, so the
 * resulting list contains only the characters that should appear in a game of the given size
 * during the given era.
 */
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

    /**
     * Reads {@code /json/characterCards.json} and returns the characters that belong to the
     * given era and are unlocked at the given player count. Entries whose {@code classType}
     * is not present in {@link #typeToClass} are silently skipped.
     *
     * @param era      era to load characters for (1, 2 or 3)
     * @param gameSize number of players in the game
     * @return the character cards available for the requested era and player count
     * @throws RuntimeException if the resource is missing or cannot be parsed
     */
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
