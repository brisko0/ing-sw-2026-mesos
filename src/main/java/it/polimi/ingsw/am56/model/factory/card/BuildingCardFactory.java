package it.polimi.ingsw.am56.model.factory.card;

import com.google.gson.*;
import it.polimi.ingsw.am56.model.card.buildingCard.*;
import it.polimi.ingsw.am56.model.factory.CardFactory;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

/**
 * {@link CardFactory} implementation that loads {@link BuildingCard}s from
 * {@code /json/buildingCards.json}. Each JSON entry carries a {@code classType}
 * discriminator that selects the concrete subclass to deserialise into; the {@link #typeToClass}
 * map is the single source of truth for the JSON-to-Java mapping, so adding a new building
 * variant means adding an entry there.
 * <p>
 * Buildings are filtered by era only — unlike characters and events, their availability does
 * not depend on the player count.
 */
public class BuildingCardFactory implements CardFactory<BuildingCard> {
    private final Gson gson = new Gson();

    // Mappa che associa il "classType" del JSON alla classe Java concreta
    private final Map<String, Class<? extends BuildingCard>> typeToClass = Map.ofEntries(
            entry("FoodPerSet", FoodPerSet.class),
            entry("FeedingSaleCharType", FeedingSaleCharType.class),
            entry("ShamanNoLoss", ShamanNoLoss.class),
            entry("FoodBonusOrderTile", FoodBonusOrderTile.class),
            entry("FoodPerInventorPair", FoodPerInventorPair.class),
            entry("ShamanEventDouble", ShamanEventDouble.class),
            entry("ShamanStarsBonus", ShamanStarsBonus.class),
            entry("HuntingEventFoodPointsBonus", HuntingEventFoodPointsBonus.class),
            entry("DoubleBuildersPoints", DoubleBuildersPoints.class),
            entry("ArtistEventFoodBonus", ArtistEventFoodBonus.class),
            entry("PointsPerSet", PointsPerSet.class),
            entry("PointsPerCharType", PointsPerCharType.class),
            entry("AddPointsEndGame", AddPointsEndGame.class)
    );

    /**
     * Reads {@code /json/buildingCards.json} and returns the buildings that belong to the
     * given era. Entries whose {@code classType} is not present in {@link #typeToClass} are
     * silently skipped — useful when extending the JSON ahead of the Java side.
     *
     * @param era      era to load buildings for (1, 2 or 3)
     * @param gameSize number of players in the game (unused for buildings, kept for the
     *                 {@link CardFactory} contract)
     * @return the building cards belonging to the requested era
     * @throws RuntimeException if the resource is missing or cannot be parsed
     */
    @Override
    public List<BuildingCard> createCards(int era, int gameSize) {
        List<BuildingCard> filteredCards = new ArrayList<>();
        String path = "/json/buildingCards.json";

        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream(path))) {
            // Leggiamo il file come un array di elementi JSON generici
            JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();

            for (JsonElement element : jsonArray) {
                JsonObject jsonObject = element.getAsJsonObject();

                // Recuperiamo il discriminatore
                String classType = jsonObject.get("classType").getAsString();
                int cardEra = jsonObject.get("era").getAsInt();

                // Filtriamo per Era (gli edifici non dipendono solitamente dal gameSize nei requisiti standard)
                if (cardEra == era) {
                    Class<? extends BuildingCard> targetClass = typeToClass.get(classType);
                    if (targetClass != null) {
                        filteredCards.add(gson.fromJson(element, targetClass));
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Errore nel caricamento dei building", e);
        }

        return filteredCards;
    }
}
