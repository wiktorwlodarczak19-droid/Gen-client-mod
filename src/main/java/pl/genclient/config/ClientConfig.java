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
import java.util.LinkedHashMap;
import java.util.Map;

/** Persistent client-only settings. Values are deliberately simple and safe to edit by hand. */
public final class ClientConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("genclient.json");
    private static final Map<String, Boolean> MODULES = new LinkedHashMap<>();

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
                if (saved.equals("sakura_dark") || saved.equals("midnight_ink") || saved.equals("petal_mist")) wallpaper = saved;
            }
            if (root.has("sakuraPetals")) sakuraPetals = root.get("sakuraPetals").getAsBoolean();
            if (root.has("dynamicIsland")) dynamicIsland = root.get("dynamicIsland").getAsBoolean();
            if (root.has("modBadges")) modBadges = root.get("modBadges").getAsBoolean();
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
