package it.polimi.ingsw.am56.view.TUI;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * Card/tile lookup for the TUI. For every card id it stores the visual type
 * ({@link ClassKind} + era) plus the numbers a player needs to make decisions
 * (Edificio cost/points, Sciamano stars) and a one-line, rulebook-flavoured
 * effect description. It also exposes the action of each Offer tile and the
 * food/points effect of each Order-of-Turn slot.
 *
 * <p>The five JSON resources under {@code /json/} are read once at construction;
 * failures (missing resource, malformed entry) are swallowed and the catalog
 * returns neutral defaults ({@link ClassKind#UNKNOWN}, {@code -1}, {@code ""}).
 */
public class CardCatalog {

    public enum ClassKind {
        COLLECTOR, HUNTER, BUILDER, SHAMAN, ARTIST, INVENTOR,
        BUILDING, EVENT, UNKNOWN
    }

    private static final int NONE = -1;

    /** Fixed order of the 10 Invention types, so each maps to a stable 1-10 index. */
    private static final String[] INVENTIONS = {
            "AMO", "CANOA", "CORDA", "FLAUTO", "MICROLITO",
            "MORTAIO", "ORNAMENTI", "PANE", "PELLE", "STATUETTA"
    };

    private final Map<Integer, ClassKind> kinds = new HashMap<>();
    private final Map<Integer, Integer> eras = new HashMap<>();
    private final Map<Integer, Integer> costs = new HashMap<>();
    private final Map<Integer, Integer> points = new HashMap<>();
    private final Map<Integer, Integer> shamanStars = new HashMap<>();
    private final Map<Integer, Integer> builderSale = new HashMap<>();
    private final Map<Integer, Integer> inventionIndex = new HashMap<>();
    private final Map<Integer, Boolean> providesFood = new HashMap<>();
    /** Effect text is kept only for Buildings and Events; Personaggi are self-describing via their card. */
    private final Map<Integer, String> effects = new HashMap<>();

    /** Offer tile letter (upper-case) -> {food, upperCards, lowerCards}. */
    private final Map<String, int[]> offerTiles = new HashMap<>();
    /** "numPlayers:slot" -> {foodEffect, pointsEffect}. */
    private final Map<String, int[]> orderTiles = new HashMap<>();

    public CardCatalog() {
        loadCharacterCards("/json/characterCards.json");
        loadBuildingCards("/json/buildingCards.json");
        loadEventCards("/json/eventCards.json");
        loadOfferTiles("/json/offer_tiles.json");
        loadOrderTiles("/json/order_tiles.json");
    }

    // ------------------------------------------------------------------
    // Card lookups
    // ------------------------------------------------------------------

    public ClassKind kindOf(int cardId) {
        return kinds.getOrDefault(cardId, ClassKind.UNKNOWN);
    }

    public int eraOf(int cardId) {
        return eras.getOrDefault(cardId, 0);
    }

    /** @return the Edificio cost in Food, or {@code -1} for non-building cards. */
    public int costOf(int cardId) {
        return costs.getOrDefault(cardId, NONE);
    }

    /** @return the end-game points printed on the card, or {@code -1} if it has none. */
    public int pointsOf(int cardId) {
        return points.getOrDefault(cardId, NONE);
    }

    /** @return the number of Shaman stars on the card, or {@code -1} if not a Shaman. */
    public int shamanStarsOf(int cardId) {
        return shamanStars.getOrDefault(cardId, NONE);
    }

    /** @return the Builder's Food discount on Buildings, or {@code -1} if not a Builder. */
    public int builderSaleOf(int cardId) {
        return builderSale.getOrDefault(cardId, NONE);
    }

    /** @return the Inventor's invention index (1-10), or {@code -1} if not an Inventor. */
    public int inventionIndexOf(int cardId) {
        return inventionIndex.getOrDefault(cardId, NONE);
    }

    /** @return {@code true} if this Hunter provides Food when added to the tribe. */
    public boolean providesFood(int cardId) {
        return providesFood.getOrDefault(cardId, false);
    }

    /** @return a one-line effect description (Buildings/Events only), or {@code ""} otherwise. */
    public String effectOf(int cardId) {
        return effects.getOrDefault(cardId, "");
    }

    // ------------------------------------------------------------------
    // Tile lookups
    // ------------------------------------------------------------------

    /** @return {food, upperCards, lowerCards} for the offer tile, or {@code null}. */
    public int[] offerTileAction(String letter) {
        if (letter == null) return null;
        return offerTiles.get(letter.toUpperCase());
    }

    /** @return {foodEffect, pointsEffect} for the slot at the given table size, or {@code null}. */
    public int[] orderTileEffect(int numPlayers, int slot) {
        return orderTiles.get(numPlayers + ":" + slot);
    }

    // ------------------------------------------------------------------
    // Loaders
    // ------------------------------------------------------------------

    private void loadCharacterCards(String resourcePath) {
        forEachEntry(resourcePath, obj -> {
            int id = obj.get("cardId").getAsInt();
            String type = optStr(obj, "classType", "");
            kinds.put(id, mapCharacterType(type));
            eras.put(id, optInt(obj, "era", 0));
            switch (type) {
                case "SHAMAN" -> shamanStars.put(id, optInt(obj, "shamanStars", 0));
                case "BUILDER" -> builderSale.put(id, optInt(obj, "buildingSale", 0));
                case "INVENTOR" -> inventionIndex.put(id, inventionIndexFromName(optStr(obj, "invention", "")));
                case "HUNTER" -> providesFood.put(id, optBool(obj, "providesFood", false));
                default -> { /* Collectors / Artists are fully identified by their type icon. */ }
            }
        });
    }

    /** Maps an invention name to its fixed 1-10 index, or {@code -1} if unknown. */
    private int inventionIndexFromName(String invention) {
        for (int i = 0; i < INVENTIONS.length; i++) {
            if (INVENTIONS[i].equalsIgnoreCase(invention)) return i + 1;
        }
        return NONE;
    }

    private void loadBuildingCards(String resourcePath) {
        forEachEntry(resourcePath, obj -> {
            int id = obj.get("cardId").getAsInt();
            kinds.put(id, ClassKind.BUILDING);
            eras.put(id, optInt(obj, "era", 0));
            costs.put(id, optInt(obj, "cost", 0));
            points.put(id, optInt(obj, "points", 0));
            effects.put(id, buildingEffect(obj));
        });
    }

    private void loadEventCards(String resourcePath) {
        forEachEntry(resourcePath, obj -> {
            int id = obj.get("cardId").getAsInt();
            kinds.put(id, ClassKind.EVENT);
            eras.put(id, optInt(obj, "era", 0));
            effects.put(id, eventEffect(obj));
        });
    }

    private void loadOfferTiles(String resourcePath) {
        forEachEntry(resourcePath, obj -> offerTiles.put(
                optStr(obj, "letter", "?").toUpperCase(),
                new int[] { optInt(obj, "food", 0), optInt(obj, "upperCards", 0), optInt(obj, "lowerCards", 0) }));
    }

    private void loadOrderTiles(String resourcePath) {
        forEachEntry(resourcePath, obj -> orderTiles.put(
                optInt(obj, "numPlayers", 0) + ":" + optInt(obj, "orderTileId", 0),
                new int[] { optInt(obj, "foodEffect", 0), optInt(obj, "pointsEffect", 0) }));
    }

    // ------------------------------------------------------------------
    // One-line effect builders (rulebook wording, kept terse for the terminal)
    // ------------------------------------------------------------------

    private String buildingEffect(JsonObject obj) {
        return switch (optStr(obj, "classType", "")) {
            case "FoodPerSet" -> "+" + optInt(obj, "bonusFood", 0)
                    + " Cibo per ogni nuovo set di 6 Personaggi diversi";
            case "FeedingSaleCharType" -> "Sostentamento: -1 Cibo per ogni "
                    + charSingular(optStr(obj, "characterType", ""));
            case "ShamanNoLoss" -> "Rituale Sciamanico: non perdi PP con meno stelle degli altri";
            case "FoodBonusOrderTile" -> "+" + optInt(obj, "foodBonus", 0)
                    + " Cibo se piazzi il Totem su uno spazio Ordine con bonus Cibo";
            case "FoodPerInventorPair" -> "+" + optInt(obj, "foodBonus", 0)
                    + " Cibo per ogni nuova coppia di Inventori uguali";
            case "ShamanEventDouble" -> "Rituale Sciamanico: PP raddoppiati se hai più stelle di tutti";
            case "ShamanStarsBonus" -> "Possiedi " + optInt(obj, "bonusStars", 0) + " stelle Sciamano in più";
            case "HuntingEventFoodPointsBonus" -> "Caccia: +1 Cibo e +1 PP per ogni Cacciatore";
            case "DoubleBuildersPoints" -> "A fine partita: PP dei Costruttori raddoppiati";
            case "ArtistEventFoodBonus" -> "Pitture Rupestri: +1 Cibo per ogni Artista";
            case "PointsPerSet" -> "A fine partita: +" + optInt(obj, "bonusPoints", 0)
                    + " PP per ogni set di 6 Personaggi diversi";
            case "PointsPerCharType" -> "A fine partita: +" + optInt(obj, "pointsPerCharacter", 0)
                    + " PP per ogni " + charSingular(optStr(obj, "characterType", ""));
            case "AddPointsEndGame" -> "A fine partita: +" + optInt(obj, "bonusPoints", 0) + " PP";
            case "ExtraUpperCardAfterActions" -> "Pesca bonus: 1 carta dalla fila superiore prima della fine round";
            default -> "Edificio";
        };
    }

    private String eventEffect(JsonObject obj) {
        boolean isFinal = obj.has("isFinal") && obj.get("isFinal").getAsBoolean();
        String suffix = isFinal ? " [finale]" : "";
        return switch (optStr(obj, "classType", "")) {
            case "SUSTENANCE" -> "Sostentamento — paga 1 Cibo/Personaggio; -"
                    + optInt(obj, "losePointsPerChar", 0) + " PP per ogni non sfamato" + suffix;
            case "HUNTER" -> "Caccia — +1 Cibo e +" + optInt(obj, "winPointsPerHunter", 0)
                    + " PP per ogni Cacciatore" + suffix;
            case "SHAMAN" -> "Rituale Sciamanico — più stelle +" + optInt(obj, "winPoints", 0)
                    + " PP, meno stelle -" + optInt(obj, "losePoints", 0) + " PP" + suffix;
            case "ARTIST" -> "Pitture Rupestri — <" + optInt(obj, "minArtists", 0) + " Artisti: -"
                    + optInt(obj, "losePoints", 0) + " PP; altrimenti +"
                    + optInt(obj, "winPointsPerArtist", 0) + " PP/Artista" + suffix;
            default -> "Evento" + suffix;
        };
    }

    // ------------------------------------------------------------------
    // Small helpers
    // ------------------------------------------------------------------

    private ClassKind mapCharacterType(String type) {
        return switch (type) {
            case "COLLECTOR" -> ClassKind.COLLECTOR;
            case "HUNTER" -> ClassKind.HUNTER;
            case "BUILDER" -> ClassKind.BUILDER;
            case "SHAMAN" -> ClassKind.SHAMAN;
            case "ARTIST" -> ClassKind.ARTIST;
            case "INVENTOR" -> ClassKind.INVENTOR;
            default -> ClassKind.UNKNOWN;
        };
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

    private boolean optBool(JsonObject obj, String key, boolean def) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsBoolean() : def;
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
