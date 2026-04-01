package it.polimi.ingsw.am56.model.factory.card;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.am56.model.card.characterCard.*;
import it.polimi.ingsw.am56.model.factory.CardFactory;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CharacterCardFactory implements CardFactory<CharacterCard> {
    private final Gson gson = new Gson();


    private final Map<String, Type> config = Map.of(
            "artist_characters.json", new TypeToken<ArrayList<ArtistCharCard>>(){}.getType(),
            "builder_characters.json", new TypeToken<ArrayList<BuilderCharCard>>(){}.getType(),
            "collector_characters.json", new TypeToken<ArrayList<CollectorCharCard>>(){}.getType(),
            "hunting_characters.json", new TypeToken<ArrayList<HunterCharCard>>(){}.getType(),
            "inventor_characters.json", new TypeToken<ArrayList<InventorCharCard>>(){}.getType(),
            "shaman_characters.json", new TypeToken<ArrayList<ShamanCharCard>>(){}.getType()
    );

    @Override
    public List<CharacterCard> createCards(int era, int gameSize) {
        List<CharacterCard> filteredCards = new ArrayList<>();

        for (Map.Entry<String, Type> entry : config.entrySet()) {
            String fileName = entry.getKey();
            Type listType = entry.getValue();

            filteredCards.addAll(loadAndFilter(fileName, listType, era, gameSize));
        }

        return filteredCards;
    }

    private List<CharacterCard> loadAndFilter(String fileName, Type listType, int era, int gameSize) {
        String path = "/it/polimi/ingsw/am56/json/character/" + fileName;

        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream(path))) {
            List<? extends CharacterCard> allCards = gson.fromJson(reader, listType);
            if (allCards == null) return List.of();
            return allCards.stream()
                    .filter(c -> gameSize >= c.getMinPlayers() && c.getEra() == era)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Errore nel caricamento di " + fileName, e);
        }
    }
}
