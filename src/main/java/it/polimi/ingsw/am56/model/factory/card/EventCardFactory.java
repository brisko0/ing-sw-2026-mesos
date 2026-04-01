package it.polimi.ingsw.am56.model.factory.card;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.am56.model.card.eventCard.*;
import it.polimi.ingsw.am56.model.factory.CardFactory;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EventCardFactory implements CardFactory<EventCard> {
    private final Gson gson = new Gson();


    private final Map<String, Type> config = Map.of(
            "shaman_cards.json", new TypeToken<ArrayList<ShamansEvCard>>(){}.getType(),
            "hunting_cards.json", new TypeToken<ArrayList<HuntingEvCard>>(){}.getType(),
            "artist_cards.json", new TypeToken<ArrayList<ArtistsEvCard>>(){}.getType(),
            "sustenance_cards.json", new TypeToken<ArrayList<SustenanceEvCard>>(){}.getType()
    );

    @Override
    public List<EventCard> createCards(int era, int gameSize) {
        List<EventCard> filteredCards = new ArrayList<>();

        for (Map.Entry<String, Type> entry : config.entrySet()) {
            String fileName = entry.getKey();
            Type listType = entry.getValue();

            filteredCards.addAll(loadAndFilter(fileName, listType, era, gameSize));
        }

        return filteredCards;
    }

    private List<EventCard> loadAndFilter(String fileName, Type listType, int era, int gameSize) {
        String path = "/it/polimi/ingsw/am56/json/event/" + fileName;

        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream(path))) {
            List<? extends EventCard> allCards = gson.fromJson(reader, listType);
            if (allCards == null) return List.of();
            return allCards.stream()
                    .filter(c -> gameSize >= c.getMinPlayers() && c.getEra() == era)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Errore nel caricamento di " + fileName, e);
        }
    }
}