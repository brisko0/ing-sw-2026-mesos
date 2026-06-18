package it.polimi.ingsw.am56.view.GUI;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * Builds rich, human-readable Italian descriptions for every card and tile, used as hover
 * tooltips in the game view. The card/tile data under {@code /json/} is read once at
 * construction and turned into a finished description string per card id (and per offer-tile
 * letter); failures are swallowed so an unknown id simply falls back to a generic label.
 *
 * <p>The wording mirrors the official rulebook terminology (Personaggi, Edifici, Eventi,
 * tessere Offerta / Ordine di Turno).
 */
public class CardInfoCatalog {

    private final Map<Integer, String> cardDescriptions = new HashMap<>();
    private final Map<String, String> offerTileDescriptions = new HashMap<>();

    public CardInfoCatalog() {
        loadCharacterCards("/json/characterCards.json");
        loadBuildingCards("/json/buildingCards.json");
        loadEventCards("/json/eventCards.json");
        loadOfferTiles("/json/offer_tiles.json");
    }

    /**
     * @param cardId the card id shown on the board
     * @return a multi-line Italian description of the card, or a generic fallback
     */
    public String describeCard(int cardId) {
        return cardDescriptions.getOrDefault(cardId, "Carta ID: " + cardId);
    }

    /**
     * @param letter the offer-tile letter
     * @return an Italian description of the offer tile's action, or a generic fallback
     */
    public String describeOfferTile(String letter) {
        if (letter == null) return "Tessera Offerta";
        return offerTileDescriptions.getOrDefault(letter.toUpperCase(), "Tessera Offerta " + letter);
    }

    /**
     * @return a fixed description of the turn-order tile
     */
    public String describeOrderTile() {
        return "Tessera Ordine di Turno\n"
                + "Determina l'ordine di gioco del round in base alla posizione dei Totem.\n"
                + "Quando vi riporti il Totem alcuni spazi danno o tolgono Cibo/PP; "
                + "l'ultimo spazio costa 1 Cibo (oppure -2 PP se non ne hai).";
    }

    /**
     * @param era the current era number (1-3), or 0 if unknown
     * @return a description of the Tribù deck
     */
    public String describeDeck(int era) {
        String eraPart = era > 0 ? " (Era " + era + ")" : "";
        return "Mazzo Tribù" + eraPart + "\n"
                + "Da qui si pescano le nuove carte per la fila superiore alla fine di ogni round.\n"
                + "La partita termina dopo 10 round, all'esaurimento del mazzo.";
    }

    // ------------------------------------------------------------------
    // Loaders
    // ------------------------------------------------------------------

    private void loadCharacterCards(String resourcePath) {
        forEachEntry(resourcePath, obj -> {
            int id = obj.get("cardId").getAsInt();
            int era = optInt(obj, "era", 0);
            String type = optStr(obj, "classType", "");
            cardDescriptions.put(id, characterDescription(type, era, obj));
        });
    }

    private void loadBuildingCards(String resourcePath) {
        forEachEntry(resourcePath, obj -> {
            int id = obj.get("cardId").getAsInt();
            cardDescriptions.put(id, buildingDescription(obj));
        });
    }

    private void loadEventCards(String resourcePath) {
        forEachEntry(resourcePath, obj -> {
            int id = obj.get("cardId").getAsInt();
            cardDescriptions.put(id, eventDescription(obj));
        });
    }

    private void loadOfferTiles(String resourcePath) {
        forEachEntry(resourcePath, obj -> {
            String letter = optStr(obj, "letter", "?");
            offerTileDescriptions.put(letter.toUpperCase(), offerTileDescription(letter, obj));
        });
    }

    // ------------------------------------------------------------------
    // Description builders
    // ------------------------------------------------------------------

    private String characterDescription(String type, int era, JsonObject obj) {
        String eraPart = era > 0 ? " (Era " + era + ")" : "";
        return switch (type) {
            case "COLLECTOR" -> "Personaggio - Raccoglitore" + eraPart + "\n"
                    + "Durante l'Evento Sostentamento fornisce uno sconto di 3 Cibo sul totale da pagare.";
            case "HUNTER" -> "Personaggio - Cacciatore" + eraPart + "\n"
                    + "Quando aggiungi un Cacciatore con l'icona Caccia, prendi subito 1 Cibo per ogni "
                    + "Cacciatore nella tua tribù (con o senza icona); senza icona non ottieni nulla.\n"
                    + "Durante l'Evento Caccia prendi Cibo e PP in base al numero di Cacciatori nella tua tribù.";
            case "BUILDER" -> "Personaggio - Costruttore" + eraPart + "\n"
                    + "Riduce di " + optInt(obj, "buildingSale", 0)
                    + " il costo in Cibo di ogni Edificio che prendi.\n"
                    + "A fine partita fornisce " + optInt(obj, "builderPoints", 0) + " PP.";
            case "INVENTOR" -> "Personaggio - Inventore" + eraPart + "\n"
                    + "A fine partita: PP = numero di Inventori x numero di icone Invenzione diverse "
                    + "possedute (10 icone in totale).";
            case "SHAMAN" -> "Personaggio - Sciamano" + eraPart + "\n"
                    + "Aggiunge " + optInt(obj, "shamanStars", 0) + " stella/e.\n"
                    + "Durante il Rituale Sciamanico la maggioranza di stelle fa guadagnare PP, "
                    + "la minoranza li fa perdere.";
            case "ARTIST" -> "Personaggio - Artista" + eraPart + "\n"
                    + "Durante l'Evento Pitture Rupestri puoi guadagnare o perdere PP in base al numero di Artisti.\n"
                    + "A fine partita: 10 PP per ogni 2 Artisti nella tua tribù.";
            default -> "Personaggio" + eraPart;
        };
    }

    private String buildingDescription(JsonObject obj) {
        int era = optInt(obj, "era", 0);
        int cost = optInt(obj, "cost", 0);
        int points = optInt(obj, "points", 0);
        String type = optStr(obj, "classType", "");

        String header = "Edificio - Costo: " + cost + " Cibo - " + points + " PV base"
                + (era > 0 ? " (Era " + era + ")" : "");

        String effect = switch (type) {
            case "FoodPerSet" -> "Prendi " + optInt(obj, "bonusFood", 0)
                    + " Cibo ogni volta che completi un nuovo set di 6 Personaggi di tipo diverso.";
            case "FeedingSaleCharType" -> "Durante l'Evento Sostentamento: sconto di 1 Cibo per ogni "
                    + charSingular(optStr(obj, "characterType", "")) + " nella tua tribù.";
            case "ShamanNoLoss" -> "Durante il Rituale Sciamanico non perdi PP anche se hai meno stelle "
                    + "degli altri giocatori.";
            case "FoodBonusOrderTile" -> "A fine turno, se piazzi il Totem su uno spazio Ordine di Turno con "
                    + "bonus Cibo, prendi " + optInt(obj, "foodBonus", 0) + " Cibo aggiuntivo.";
            case "FoodPerInventorPair" -> "Prendi " + optInt(obj, "foodBonus", 0)
                    + " Cibo ogni volta che formi una nuova coppia di Inventori uguali.";
            case "ShamanEventDouble" -> "Durante il Rituale Sciamanico, se hai più stelle di tutti gli altri, "
                    + "guadagni il doppio dei PP (anche in caso di parità).";
            case "ShamanStarsBonus" -> "All'acquisto ricevi subito " + optInt(obj, "bonusStars", 0)
                    + " stelle Sciamano.";
            case "HuntingEventFoodPointsBonus" -> "Durante l'Evento Caccia, per ogni Cacciatore prendi "
                    + "1 Cibo e 1 PP aggiuntivi.";
            case "DoubleBuildersPoints" -> "A fine partita guadagni il doppio dei PP indicati sulle tue "
                    + "carte Costruttore.";
            case "ArtistEventFoodBonus" -> "Durante l'Evento Pitture Rupestri prendi 1 Cibo per ogni Artista "
                    + "nella tua tribù.";
            case "PointsPerSet" -> "A fine partita guadagni " + optInt(obj, "bonusPoints", 0)
                    + " PP per ogni set di 6 Personaggi di tipo diverso completato.";
            case "PointsPerCharType" -> "A fine partita guadagni " + optInt(obj, "pointsPerCharacter", 0)
                    + " PP per ogni " + charSingular(optStr(obj, "characterType", "")) + " nella tua tribù.";
            case "AddPointsEndGame" -> "A fine partita guadagni " + optInt(obj, "bonusPoints", 0) + " PP.";
            case "ExtraUpperCardAfterActions" -> "Dopo che tutti hanno risolto le azioni e prima della Fine "
                    + "del Round, puoi prendere 1 carta extra (pagandone il costo) dalla fila superiore.";
            default -> "Edificio.";
        };

        return header + "\n" + effect;
    }

    private String eventDescription(JsonObject obj) {
        int era = optInt(obj, "era", 0);
        boolean isFinal = obj.has("isFinal") && obj.get("isFinal").getAsBoolean();
        String type = optStr(obj, "classType", "");

        String eraPart = era > 0 ? " (Era " + era + ")" : "";
        String finalPart = isFinal ? " - Evento finale" : "";

        String body = switch (type) {
            case "SUSTENANCE" -> "Evento - Sostentamento\n"
                    + "Paga 1 Cibo per ogni Personaggio nella tua tribù (gli Edifici non contano).\n"
                    + "Per ogni Personaggio non sfamato perdi " + optInt(obj, "losePointsPerChar", 0) + " PP.\n"
                    + "Si risolve sempre per ultimo.";
            case "HUNTER" -> "Evento - Caccia\n"
                    + "Prendi 1 Cibo e guadagni " + optInt(obj, "winPointsPerHunter", 0)
                    + " PP per ogni Cacciatore nella tua tribù.";
            case "SHAMAN" -> "Evento - Rituale Sciamanico\n"
                    + "Chi ha più stelle guadagna " + optInt(obj, "winPoints", 0) + " PP; "
                    + "chi ne ha meno perde " + optInt(obj, "losePoints", 0) + " PP.\n"
                    + "In caso di parità, tutti i pari guadagnano o perdono.";
            case "ARTIST" -> "Evento - Pitture Rupestri\n"
                    + "Con meno di " + optInt(obj, "minArtists", 0) + " Artisti perdi "
                    + optInt(obj, "losePoints", 0) + " PP;\n"
                    + "con almeno " + optInt(obj, "minArtists", 0) + " guadagni "
                    + optInt(obj, "winPointsPerArtist", 0) + " PP per ogni Artista.";
            default -> "Evento";
        };

        int nl = body.indexOf('\n');
        if (nl < 0) return body + eraPart + finalPart;
        return body.substring(0, nl) + eraPart + finalPart + body.substring(nl);
    }

    private String offerTileDescription(String letter, JsonObject obj) {
        int food = optInt(obj, "food", 0);
        int upper = optInt(obj, "upperCards", 0);
        int lower = optInt(obj, "lowerCards", 0);

        StringBuilder action = new StringBuilder();
        if (upper > 0) action.append("prendi ").append(cards(upper)).append(" dalla fila superiore");
        if (lower > 0) {
            action.append(action.length() > 0 ? " e " : "prendi ");
            action.append(cards(lower)).append(" dalla fila inferiore");
        }
        if (food > 0) {
            if (action.length() > 0) action.append("; ");
            action.append("ottieni ").append(food).append(" Cibo");
        }
        if (action.length() == 0) action.append("nessuna azione");

        String act = action.toString();
        act = Character.toUpperCase(act.charAt(0)) + act.substring(1);

        return "Tessera Offerta " + letter.toUpperCase() + "\n" + act + ".";
    }

    // ------------------------------------------------------------------
    // Small helpers
    // ------------------------------------------------------------------

    private String cards(int n) {
        return n + (n == 1 ? " carta" : " carte");
    }

    private String charSingular(String type) {
        return switch (type) {
            case "COLLECTOR" -> "Raccoglitore";
            case "HUNTER" -> "Cacciatore";
            case "BUILDER" -> "Costruttore";
            case "INVENTOR" -> "Inventore";
            case "SHAMAN" -> "Sciamano";
            case "ARTIST" -> "Artista";
            default -> "Personaggio";
        };
    }

    private int optInt(JsonObject obj, String key, int def) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsInt() : def;
    }

    private String optStr(JsonObject obj, String key, String def) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : def;
    }

    private void forEachEntry(String resourcePath, java.util.function.Consumer<JsonObject> sink) {
        var stream = getClass().getResourceAsStream(resourcePath);
        if (stream == null) return;
        try (Reader r = new InputStreamReader(stream)) {
            JsonElement root = JsonParser.parseReader(r);
            if (!root.isJsonArray()) return;
            JsonArray arr = root.getAsJsonArray();
            for (JsonElement el : arr) {
                if (el.isJsonObject()) sink.accept(el.getAsJsonObject());
            }
        } catch (Exception ignored) {
        }
    }
}
