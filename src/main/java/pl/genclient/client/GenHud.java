package pl.genclient.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.text.Text;
import pl.genclient.config.ClientConfig;
import pl.genclient.gui.UiDraw;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** Sharp, lightweight HUD elements; deliberately does not invoke Minecraft's blur pipeline. */
public final class GenHud {
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");

    private GenHud() {}

    public static void register() {
        HudRenderCallback.EVENT.register((context, tickCounter) -> render(context));
    }

    private static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.options.hudHidden) return;

        if (ClientConfig.dynamicIsland) {
            drawClock(context, client);
            drawIsland(context, client);
        }
        if (ClientConfig.fpsCounter) drawFpsCounter(context, client);
        if (ClientConfig.isEnabled("Target ESP")) drawTarget(context, client);
        if (ClientConfig.modBadges) drawCompanionBadges(context, client);
    }

    private static void drawClock(DrawContext context, MinecraftClient client) {
        String clock = LocalTime.now().format(CLOCK);
        context.drawTextWithShadow(client.textRenderer, Text.literal(clock), 24, 14, 0xFFF2F2F5);
    }

    private static void drawFpsCounter(DrawContext context, MinecraftClient client) {
        String value = ClientConfig.fpsCounterLabel
                ? "FPS: " + client.getCurrentFps()
                : client.getCurrentFps() + " FPS";
        int x = 108;
        context.drawTextWithShadow(client.textRenderer, Text.literal(value), x, 14, 0xFFE8EAF0);
    }

    private static void drawIsland(DrawContext context, MinecraftClient client) {
        int screenWidth = client.getWindow().getScaledWidth();
        int islandWidth = Math.min(450, Math.max(208, screenWidth - 190));
        int x = screenWidth - islandWidth - 9;
        int y = 7;
        int accent = ClientConfig.islandAccentColor();
        int islandHeight = 39;
        boolean musicPlaying = client.getMusicTracker().isPlayingType(client.getMusicType());

        UiDraw.roundedFill(context, x - 3, y - 2, islandWidth + 6, islandHeight + 5, 20, 0x33000000);
        UiDraw.roundedPanel(context, x, y, islandWidth, islandHeight, 18, 0xF20A0C12, 0x805B606D);
        UiDraw.roundedFill(context, x + 17, y + 1, islandWidth - 34, 1, 1, 0x55FFFFFF);

        UiDraw.roundedPanel(context, x + 10, y + 7, 25, 25, 12, 0xFF25202E, 0x805B536B);
        context.drawCenteredTextWithShadow(client.textRenderer, "G", x + 22, y + 15, accent);

        context.drawTextWithShadow(client.textRenderer, "GEN CLIENT", x + 48, y + 8, 0xFFF2F2F5);
        String musicState = musicPlaying ? "MINECRAFT MUSIC PLAYING" : "MINECRAFT MUSIC IDLE";
        context.drawText(client.textRenderer, Text.literal(musicState), x + 48, y + 21, 0xFF9BA2B0, false);

        int musicColor = musicPlaying ? accent : 0xFF858D9B;
        long animation = System.currentTimeMillis() / 125L;
        int barsX = x + islandWidth - 45;
        for (int i = 0; i < 5; i++) {
            int barHeight = ClientConfig.islandVisualizerAnimated && musicPlaying
                    ? 7 + Math.floorMod((int) animation + i * 3, 5) * 3
                    : 8;
            UiDraw.roundedFill(context, barsX + i * 7, y + (islandHeight - barHeight) / 2,
                    4, barHeight, 2, musicColor);
        }
    }

    private static void drawTarget(DrawContext context, MinecraftClient client) {
        if (!(client.crosshairTarget instanceof EntityHitResult hit)) return;
        Entity target = hit.getEntity();
        if (target == client.player || !target.isAlive()) return;

        int centerX = client.getWindow().getScaledWidth() / 2;
        int centerY = client.getWindow().getScaledHeight() / 2;
        String name = target.getName().getString();
        if (name.length() > 22) name = name.substring(0, 19) + "...";
        String health = target instanceof LivingEntity living
                ? MathHelper.ceil(living.getHealth()) + " HP" : "ENTITY";
        int panelWidth = Math.max(104, client.textRenderer.getWidth(name) + 42);
        int left = Math.min(centerX + 14, Math.max(6, client.getWindow().getScaledWidth() - panelWidth - 6));
        int top = Math.min(centerY + 12, Math.max(6, client.getWindow().getScaledHeight() - 38));
        int accent = ClientConfig.accentColor();

        UiDraw.roundedPanel(context, left, top, panelWidth, 32, 8, 0xE30A0C12, 0x705B606D);
        UiDraw.roundedFill(context, left + 1, top + 6, 2, 20, 1, accent);
        context.drawTextWithShadow(client.textRenderer, Text.literal(name), left + 9, top + 6, 0xFFFFFFFF);
        context.drawText(client.textRenderer, Text.literal(health), left + 9, top + 17, 0xFFB5BCC9, false);
    }

    private static void drawCompanionBadges(DrawContext context, MinecraftClient client) {
        boolean flashback = FabricLoader.getInstance().isModLoaded("flashback");
        boolean modMenu = FabricLoader.getInstance().isModLoaded("modmenu");
        if (!flashback && !modMenu) return;

        int x = client.getWindow().getScaledWidth() - 30;
        int y = client.getWindow().getScaledHeight() / 2 - ((flashback && modMenu) ? 25 : 12);
        if (flashback) drawBadge(context, x, y, 0xFFBFA7FF, true);
        if (modMenu) drawBadge(context, x, y + (flashback ? 27 : 0), 0xFF9ECbFF, false);
    }

    private static void drawBadge(DrawContext context, int x, int y, int accent, boolean cameraIcon) {
        UiDraw.roundedPanel(context, x - 2, y - 2, 24, 24, 7, 0xE90B0D12, 0x705B606D);
        UiDraw.roundedFill(context, x + 2, y - 1, 16, 2, 1, accent);
        if (cameraIcon) {
            // Flashback-style camera glyph.
            UiDraw.roundedFill(context, x + 4, y + 7, 13, 9, 2, accent);
            UiDraw.roundedFill(context, x + 7, y + 5, 5, 3, 1, accent);
            UiDraw.roundedFill(context, x + 6, y + 9, 9, 5, 2, 0xFF171923);
            UiDraw.roundedFill(context, x + 9, y + 10, 3, 3, 2, 0xFFFFFFFF);
        } else {
            // Mod Menu-style four-tile grid glyph.
            UiDraw.roundedFill(context, x + 5, y + 6, 5, 5, 1, accent);
            UiDraw.roundedFill(context, x + 12, y + 6, 5, 5, 1, accent);
            UiDraw.roundedFill(context, x + 5, y + 13, 5, 5, 1, accent);
            UiDraw.roundedFill(context, x + 12, y + 13, 5, 5, 1, accent);
        }
    }
}
