package pl.genclient.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/** Persistent, client-only settings and real weekly playtime counters. */
public final class ClientConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("genclient.json");
    private static final Map<String, Boolean> MODULES = new LinkedHashMap<>();
    private static final Map<String, Long> PLAYTIME = new LinkedHashMap<>();

    public static String accent = "SAKURA";
    public static String wallpaper = "sakura_dark";
    public static boolean sakuraPetals = true;
    public static boolean dynamicIsland = true;
    public static boolean modBadges = true;

    static {
        MODULES.put("Jump Circle", true);
        MODULES.put("Glass Vapor", true);
        MODULES.put("Target ESP", false);
        MODULES.put("Hit Sparks", true);
        MODULES.put("Aura Glint", false);
        MODULES.put("Armor Glass Light", false);
        load();
    }

    private ClientConfig() {}

    public static boolean isEnabled(String module) {
        return MODULES.getOrDefault(module, false);
    }

    public static Map<String, Boolean> modules() {
        return Map.copyOf(MODULES);
    }

    public static void toggle(String module) {
        MODULES.put(module, !isEnabled(module));
        save();
    }

    public static void setWallpaper(String value) {
        if (value.equals("sakura_dark") || value.equals("midnight_ink") || value.equals("petal_mist")) {
            wallpaper = value;
            save();
        }
    }

    public static void cycleWallpaper() {
        wallpaper = switch (wallpaper) {
            case "sakura_dark" -> "midnight_ink";
            case "midnight_ink" -> "petal_mist";
            default -> "sakura_dark";
        };
        save();
    }

    public static void cycleAccent() {
        accent = switch (accent) {
            case "SAKURA" -> "PEARL";
            case "PEARL" -> "ICE";
            case "ICE" -> "LAVENDER";
            case "LAVENDER" -> "MINT";
            default -> "SAKURA";
        };
        save();
    }

    public static int accentColor() {
        return switch (accent) {
            case "PEARL" -> 0xFFFFFFFF;
            case "ICE" -> 0xFF9ECbFF;
            case "LAVENDER" -> 0xFFC4B5FD;
            case "MINT" -> 0xFF9BE8C5;
            default -> 0xFFF4BFD3;
        };
    }

    /** Called once per real second while a world is active and unpaused. */
    public static void recordPlaySecond() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        long updated = PLAYTIME.getOrDefault(today, 0L) + 1L;
        PLAYTIME.put(today, updated);

        LocalDate cutoff = LocalDate.now().minusDays(14);
        PLAYTIME.keySet().removeIf(key -> {
            try {
                return LocalDate.parse(key).isBefore(cutoff);
            } catch (Exception ignored) {
                return true;
            }
        });

        if (updated % 60L == 0L) save();
    }

    public static long playtimeFor(LocalDate date) {
        return PLAYTIME.getOrDefault(date.format(DateTimeFormatter.ISO_LOCAL_DATE), 0L);
    }

    public static long weeklyPlaytime() {
        long total = 0L;
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) total += playtimeFor(today.minusDays(i));
        return total;
    }

    public static String weeklyPlaytimeLabel() {
        long minutes = weeklyPlaytime() / 60L;
        return (minutes / 60L) + "h " + (minutes % 60L) + "m  this week";
    }

    public static void save() {
        JsonObject root = new JsonObject();
        JsonObject modules = new JsonObject();
        MODULES.forEach(modules::addProperty);
        root.add("modules", modules);
        root.addProperty("accent", accent);
        root.addProperty("wallpaper", wallpaper);
        root.addProperty("sakuraPetals", sakuraPetals);
        root.addProperty("dynamicIsland", dynamicIsland);
        root.addProperty("modBadges", modBadges);

        JsonObject playtime = new JsonObject();
        PLAYTIME.forEach(playtime::addProperty);
        root.add("playtime", playtime);

        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(root));
        } catch (IOException exception) {
            System.err.println("[GEN CLIENT] Could not save settings: " + exception.getMessage());
        }
    }

    private static void load() {
        if (!Files.isRegularFile(FILE)) return;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();
            JsonObject savedModules = root.has("modules") && root.get("modules").isJsonObject()
                    ? root.getAsJsonObject("modules") : new JsonObject();
            for (Map.Entry<String, Boolean> entry : MODULES.entrySet()) {
                JsonElement value = savedModules.get(entry.getKey());
                if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean()) {
                    entry.setValue(value.getAsBoolean());
                }
            }
            if (root.has("accent")) accent = safeAccent(root.get("accent").getAsString());
            if (root.has("wallpaper")) {
                String saved = root.get("wallpaper").getAsString();
                if (saved.equals("sakura_dark") || saved.equals("midnight_ink") || saved.equals("petal_mist")) {
                    wallpaper = saved;
                }
            }
            if (root.has("sakuraPetals")) sakuraPetals = root.get("sakuraPetals").getAsBoolean();
            if (root.has("dynamicIsland")) dynamicIsland = root.get("dynamicIsland").getAsBoolean();
            if (root.has("modBadges")) modBadges = root.get("modBadges").getAsBoolean();

            if (root.has("playtime") && root.get("playtime").isJsonObject()) {
                JsonObject savedTime = root.getAsJsonObject("playtime");
                for (Map.Entry<String, JsonElement> entry : savedTime.entrySet()) {
                    if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isNumber()) {
                        try {
                            LocalDate.parse(entry.getKey());
                            PLAYTIME.put(entry.getKey(), Math.max(0L, entry.getValue().getAsLong()));
                        } catch (Exception ignored) {
                            // Ignore malformed dates in an edited config file.
                        }
                    }
                }
            }
        } catch (Exception exception) {
            System.err.println("[GEN CLIENT] Settings file could not be read; using defaults: " + exception.getMessage());
        }
    }

    private static String safeAccent(String value) {
        return switch (value) {
            case "PEARL", "ICE", "LAVENDER", "MINT" -> value;
            default -> "SAKURA";
        };
    }
}
