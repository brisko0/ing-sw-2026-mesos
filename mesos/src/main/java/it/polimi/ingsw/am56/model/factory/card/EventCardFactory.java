package it.polimi.ingsw.am56.model.factory.card;

import com.google.gson.*;

import it.polimi.ingsw.am56.model.card.eventCard.*;
import it.polimi.ingsw.am56.model.factory.CardFactory;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * {@link CardFactory} implementation that loads {@link EventCard}s from
 * {@code /json/eventCards.json}. Each JSON entry carries a {@code classType} discriminator
 * (the event family, e.g. {@code "ARTIST"} for {@link ArtistsEvCard}) that selects the
 * concrete subclass to deserialise into.
 * <p>
 * Events are filtered by both era and the per-card {@code minPlayers} threshold, mirroring
 * {@link CharacterCardFactory}: only events that apply to a game of the given size during the
 * given era are returned.
 */
public class EventCardFactory implements CardFactory<EventCard> {
    private final Gson gson = new Gson();

    private final Map<String, Class<? extends EventCard>> typeToClass = Map.of(
            "ARTIST", ArtistsEvCard.class,
            "HUNTER", HuntingEvCard.class,
            "SHAMAN", ShamansEvCard.class,
            "SUSTENANCE", SustenanceEvCard.class
    );

    /**
     * Reads {@code /json/eventCards.json} and returns the events that belong to the given era
     * and are unlocked at the given player count. Entries whose {@code classType} is not
     * present in {@link #typeToClass} are silently skipped.
     *
     * @param era      era to load events for (1, 2 or 3)
     * @param gameSize number of players in the game
     * @return the event cards available for the requested era and player count
     * @throws RuntimeException if the resource is missing or cannot be parsed
     */
    @Override
    public List<EventCard> createCards(int era, int gameSize) {
        List<EventCard> filteredCards = new ArrayList<>();
        String path = "/json/eventCards.json";

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
                        Class<? extends EventCard> targetClass = typeToClass.get(classType);
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
