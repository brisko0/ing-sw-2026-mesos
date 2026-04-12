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

public class BuildingCardFactory implements CardFactory<BuildingCard> {
    private final Gson gson = new Gson();

    // mappa per associare classType alla classe corretta
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

    @Override
    public List<BuildingCard> createCards(int era, int gameSize) {
        List<BuildingCard> filteredCards = new ArrayList<>();
        String path = "/json/buildingCards.json";

        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream(path))) {

            JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();

            for (JsonElement element : jsonArray) {
                JsonObject jsonObject = element.getAsJsonObject();

                String classType = jsonObject.get("classType").getAsString();
                int cardEra = jsonObject.get("era").getAsInt();

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