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

/** Persistent client settings, portable config import/export and weekly playtime counters. */
public final class ClientConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
    private static final Path FILE = CONFIG_DIR.resolve("genclient.json");
    private static final Path EXPORT_FILE = CONFIG_DIR.resolve("genclient-export.json");
    private static final Path IMPORT_FILE = CONFIG_DIR.resolve("genclient-import.json");
    private static final Map<String, Boolean> MODULES = new LinkedHashMap<>();
    private static final Map<String, Long> PLAYTIME = new LinkedHashMap<>();

    public static String accent = "SAKURA";
    public static String wallpaper = "sakura_dark";
    public static boolean sakuraPetals = false;
    public static boolean dynamicIsland = true;
    public static boolean modBadges = true;

    static {
        MODULES.put("Jump Circle", true);
        MODULES.put("Glass Vapor", true);
        MODULES.put("Target ESP", false);
        MODULES.put("Hit Sparks", true);
        MODULES.put("Kill Effect", false);
        MODULES.put("Custom Sky", false);
        MODULES.put("Glint Hand", false);
        MODULES.put("Aura Glint Light 3D", false);
        MODULES.put("Armor Glass Light 3D", false);
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

    /** Writes a portable copy without replacing the live settings file. */
    public static String exportConfig() {
        try {
            Files.createDirectories(CONFIG_DIR);
            Files.writeString(EXPORT_FILE, GSON.toJson(toJson()));
            return "EXPORTED  /  config/genclient-export.json";
        } catch (IOException exception) {
            System.err.println("[GEN CLIENT] Could not export settings: " + exception.getMessage());
            return "EXPORT FAILED  /  check config folder permissions";
        }
    }

    /** Imports config/genclient-import.json, validates known fields, and saves it as the live config. */
    public static String importConfig() {
        if (!Files.isRegularFile(IMPORT_FILE)) {
            return "IMPORT FILE NOT FOUND  /  place genclient-import.json in config";
        }
        try {
            JsonObject root = JsonParser.parseString(Files.readString(IMPORT_FILE)).getAsJsonObject();
            apply(root);
            save();
            return "IMPORTED  /  settings updated";
        } catch (Exception exception) {
            System.err.println("[GEN CLIENT] Could not import settings: " + exception.getMessage());
            return "IMPORT FAILED  /  invalid JSON or unsupported values";
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_DIR);
            Files.writeString(FILE, GSON.toJson(toJson()));
        } catch (IOException exception) {
            System.err.println("[GEN CLIENT] Could not save settings: " + exception.getMessage());
        }
    }

    private static JsonObject toJson() {
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
        return root;
    }

    private static void load() {
        if (!Files.isRegularFile(FILE)) return;
        try {
            apply(JsonParser.parseString(Files.readString(FILE)).getAsJsonObject());
        } catch (Exception exception) {
            System.err.println("[GEN CLIENT] Settings file could not be read; using defaults: " + exception.getMessage());
        }
    }

    private static void apply(JsonObject root) {
        JsonObject savedModules = root.has("modules") && root.get("modules").isJsonObject()
                ? root.getAsJsonObject("modules") : new JsonObject();
        for (Map.Entry<String, Boolean> entry : MODULES.entrySet()) {
            JsonElement value = savedModules.get(entry.getKey());
            if (value == null && entry.getKey().equals("Aura Glint Light 3D")) {
                value = savedModules.get("Aura Glint");
            }
            if (value == null && entry.getKey().equals("Armor Glass Light 3D")) {
                value = savedModules.get("Armor Glass Light");
            }
            if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean()) {
                entry.setValue(value.getAsBoolean());
            }
        }

        if (root.has("accent") && root.get("accent").isJsonPrimitive()) {
            accent = safeAccent(root.get("accent").getAsString());
        }
        if (root.has("wallpaper") && root.get("wallpaper").isJsonPrimitive()) {
            String saved = root.get("wallpaper").getAsString();
            if (saved.equals("sakura_dark") || saved.equals("midnight_ink") || saved.equals("petal_mist")) {
                wallpaper = saved;
            }
        }
        if (root.has("sakuraPetals") && root.get("sakuraPetals").isJsonPrimitive()) {
            sakuraPetals = root.get("sakuraPetals").getAsBoolean();
        }
        if (root.has("dynamicIsland") && root.get("dynamicIsland").isJsonPrimitive()) {
            dynamicIsland = root.get("dynamicIsland").getAsBoolean();
        }
        if (root.has("modBadges") && root.get("modBadges").isJsonPrimitive()) {
            modBadges = root.get("modBadges").getAsBoolean();
        }

        if (root.has("playtime") && root.get("playtime").isJsonObject()) {
            PLAYTIME.clear();
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
    }

    private static String safeAccent(String value) {
        return switch (value) {
            case "PEARL", "ICE", "LAVENDER", "MINT" -> value;
            default -> "SAKURA";
        };
    }
}
