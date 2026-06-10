package it.polimi.ingsw.am56.view.TUI;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class CardCatalog {

    public enum ClassKind {
        COLLECTOR, HUNTER, BUILDER, SHAMAN, ARTIST, INVENTOR,
        BUILDING, EVENT, UNKNOWN
    }

    private final Map<Integer, ClassKind> kinds = new HashMap<>();
    private final Map<Integer, Integer> eras = new HashMap<>();

    public CardCatalog() {
        loadCharacterCards("/json/characterCards.json");
        loadFixedKind("/json/buildingCards.json", ClassKind.BUILDING);
        loadFixedKind("/json/eventCards.json", ClassKind.EVENT);
    }

    public ClassKind kindOf(int cardId) {
        return kinds.getOrDefault(cardId, ClassKind.UNKNOWN);
    }

    public int eraOf(int cardId) {
        return eras.getOrDefault(cardId, 0);
    }

    private void loadCharacterCards(String resourcePath) {
        forEachEntry(resourcePath, obj -> {
            int id = obj.get("cardId").getAsInt();
            int era = obj.has("era") ? obj.get("era").getAsInt() : 0;
            String type = obj.has("classType") ? obj.get("classType").getAsString() : "";
            kinds.put(id, mapCharacterType(type));
            eras.put(id, era);
        });
    }

    private void loadFixedKind(String resourcePath, ClassKind fixed) {
        forEachEntry(resourcePath, obj -> {
            int id = obj.get("cardId").getAsInt();
            int era = obj.has("era") ? obj.get("era").getAsInt() : 0;
            kinds.put(id, fixed);
            eras.put(id, era);
        });
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
}
