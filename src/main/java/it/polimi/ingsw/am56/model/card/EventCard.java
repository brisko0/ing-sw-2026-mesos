package it.polimi.ingsw.am56.model.card;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.am56.model.GameModel;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class EventCard extends Card{
    private final EvType type;
    private final int winPointsUpdateShamans;
    private final int losePointsUpdateShamans;
    private final int minArtists;
    private final int losePointsUpdateArtists;
    private final int winPointsUpdatePerArtist;
    private final int losePointsPerCharSustenance;
    private final int winPointsUpdatePerHunter;

    public EventCard(int cardId, int era, int minPlayers, EvType type, int winPointsUpdateShamans,
                     int losePointsUpdateShamans, int minArtists,
                     int losePointsUpdateArtists, int winPointsUpdatePerArtist,
                     int losePointsPerCharSustenance, int winPointsUpdatePerHunter) {
        super( cardId, era, minPlayers, CardType.EVENT);
        this.type = type;
        this.winPointsUpdateShamans = winPointsUpdateShamans;
        this.losePointsUpdateShamans = losePointsUpdateShamans;
        this.minArtists = minArtists;
        this.losePointsUpdateArtists = losePointsUpdateArtists;
        this.winPointsUpdatePerArtist = winPointsUpdatePerArtist;
        this.losePointsPerCharSustenance = losePointsPerCharSustenance;
        this.winPointsUpdatePerHunter = winPointsUpdatePerHunter;
    }

    public static List<EventCard> createEvCards(int era, int gameSize) {
        List<EventCard> eventCards = new ArrayList<>();
        List<EventCard> filteredCards = new ArrayList<>();
        Gson gson = new Gson();

        try (InputStream is = EventCard.class.getResourceAsStream("/it/polimi/ingsw/am56/json/event_cards.json")) {

            if (is == null) {
                throw new RuntimeException("File JSON NON trovato!");
            }

            try (Reader reader = new InputStreamReader(is)) {

                Type listType = new TypeToken<ArrayList<EventCard>>() {
                }.getType();
                eventCards = gson.fromJson(reader, listType);

                if (eventCards != null) {
                    for (EventCard card : eventCards) {
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

    @Override
    public void resolveEffect(GameModel gameModel) {}
}
