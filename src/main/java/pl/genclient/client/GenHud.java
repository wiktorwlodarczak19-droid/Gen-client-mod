package pl.genclient.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import pl.genclient.config.ClientConfig;
import pl.genclient.gui.UiDraw;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** Modern top HUD: local clock and measured FPS on the left, Spotify capsule on the right. */
public final class GenHud {
    private static final DateTimeFormatter CLOCK_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private GenHud() {}

    public static void register() {
        HudRenderCallback.EVENT.register((context, tickCounter) -> render(context));
    }

    private static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.options.hudHidden) return;

        if (ClientConfig.dynamicIsland) {
            drawIsland(context, client);
        } else if (ClientConfig.fpsCounter) {
            drawFps(context, client, 12, 10);
        }

        if (ClientConfig.isEnabled("Target ESP")) drawTarget(context, client);
        if (ClientConfig.modBadges) drawCompanionBadges(context, client);
    }

    /**
     * Mirrors the supplied layout: live local time/FPS at the upper left and a compact,
     * right-aligned media capsule. On narrow GUI widths the capsule moves to a second row
     * rather than colliding with the status cluster.
     */
    private static void drawIsland(DrawContext context, MinecraftClient client) {
        int screenWidth = client.getWindow().getScaledWidth();
        int top = 8;
        int statusWidth = statusClusterWidth(client);
        boolean stacked = screenWidth < 420;

        drawStatusCluster(context, client, 12, top, statusWidth);

        int capsuleWidth = stacked
                ? Math.max(150, screenWidth - 24)
                : Math.max(180, Math.min(340, screenWidth - statusWidth - 36));
        capsuleWidth = Math.min(capsuleWidth, Math.max(150, screenWidth - 24));
        int capsuleX = stacked ? (screenWidth - capsuleWidth) / 2 : screenWidth - capsuleWidth - 12;
        int capsuleY = stacked ? top + 48 : top;
        drawMediaCapsule(context, client, capsuleX, capsuleY, capsuleWidth);
    }

    private static int statusClusterWidth(MinecraftClient client) {
        String time = LocalTime.now().format(CLOCK_FORMAT);
        int width = 27 + client.textRenderer.getWidth(time) + 10;
        if (ClientConfig.fpsCounter) {
            width += 1 + 9 + client.textRenderer.getWidth(fpsLabel(client)) + 10;
        }
        return width;
    }

    private static void drawStatusCluster(DrawContext context, MinecraftClient client,
                                          int x, int y, int width) {
        int height = 40;
        int accent = ClientConfig.islandAccentColor();
        UiDraw.roundedFill(context, x - 2, y - 2, width + 4, height + 4, 20, 0x52000000);
        UiDraw.roundedPanel(context, x, y, width, height, 17, 0xE90B0D15, 0x805D586F);

        // A small hand-drawn clock glyph avoids relying on Minecraft's pixel-font icon set.
        UiDraw.roundedFill(context, x + 9, y + 14, 12, 12, 6, 0xFF292633);
        UiDraw.roundedFill(context, x + 14, y + 16, 1, 5, 1, accent);
        UiDraw.roundedFill(context, x + 14, y + 20, 4, 1, 1, accent);

        String time = LocalTime.now().format(CLOCK_FORMAT);
        int timeX = x + 27;
        context.drawTextWithShadow(client.textRenderer, Text.literal(time), timeX, y + 15, 0xFFF3F1F7);

        if (ClientConfig.fpsCounter) {
            int separatorX = timeX + client.textRenderer.getWidth(time) + 8;
            UiDraw.roundedFill(context, separatorX, y + 10, 1, 20, 1, 0x665D586F);
            context.drawTextWithShadow(client.textRenderer, Text.literal(fpsLabel(client)),
                    separatorX + 10, y + 15, 0xFFD4D5DE);
        }
    }

    private static void drawMediaCapsule(DrawContext context, MinecraftClient client,
                                         int x, int y, int width) {
        int accent = ClientConfig.islandAccentColor();
        SpotifyService.TrackSnapshot track = SpotifyService.getSnapshot();
        boolean hasTrack = track.hasTrack();
        boolean playing = hasTrack && track.isPlaying();
        boolean showManualMessage = ClientConfig.islandMessageEnabled
                && !ClientConfig.islandMessage.isBlank();
        int height = showManualMessage ? 50 : 40;

        UiDraw.roundedFill(context, x - 3, y - 2, width + 6, height + 4, 21, 0x52000000);
        UiDraw.roundedPanel(context, x, y, width, height, 19, 0xE90B0D15, 0xB05D586F);
        UiDraw.roundedFill(context, x + 13, y + 1, width - 26, 1, 1, 0x44FFFFFF);

        // Spotify artwork is intentionally not downloaded or cached; use a neutral music glyph.
        int iconY = y + (height - 24) / 2;
        UiDraw.roundedPanel(context, x + 8, iconY, 24, 24, 12, 0xFF242333, 0x605D586F);
        context.drawCenteredTextWithShadow(client.textRenderer, Text.literal("♫"),
                x + 20, iconY + 8, accent);

        int visualizerX = x + width - 34;
        int textX = x + 40;
        int textWidth = Math.max(40, visualizerX - textX - 8);
        String title = hasTrack ? track.title()
                : SpotifyService.isConnected() ? "Spotify connected" : "Connect Spotify in PLUGS";
        String artist = hasTrack ? track.artists()
                : SpotifyService.isConnected() ? "Start playback to show your music" : "Link your account to show music";

        drawMarquee(context, client, title, textX, y + (showManualMessage ? 6 : 8),
                textWidth, 0xFFF3F1F7);
        String visibleArtist = client.textRenderer.trimToWidth(artist, textWidth);
        context.drawText(client.textRenderer, Text.literal(visibleArtist), textX,
                y + (showManualMessage ? 18 : 21), 0xFFAAA7B7, false);
        if (showManualMessage) {
            drawMarquee(context, client, ClientConfig.islandMessage, textX, y + 31,
                    textWidth, 0xFFD9C8FF);
        }

        long animationTime = System.currentTimeMillis();
        int visualizerColor = playing ? accent : 0xFF777484;
        for (int i = 0; i < 5; i++) {
            int barHeight = 4;
            if (playing && ClientConfig.islandVisualizerAnimated) {
                double phase = animationTime / 245.0 + i * 0.88;
                barHeight += (int) Math.round((Math.sin(phase) + 1.0) * 3.5);
            }
            UiDraw.roundedFill(context, visualizerX + i * 5, y + (height - barHeight) / 2,
                    3, barHeight, 2, visualizerColor);
        }

        int progressX = x + 12;
        int progressY = y + height - 2;
        int progressWidth = width - 24;
        UiDraw.roundedFill(context, progressX, progressY, progressWidth, 1, 1, 0x665E596A);
        if (hasTrack && track.durationMs() > 0) {
            float progress = Math.max(0.0f, Math.min(1.0f,
                    track.currentProgressMs() / (float) track.durationMs()));
            UiDraw.roundedFill(context, progressX, progressY,
                    Math.max(1, (int) (progressWidth * progress)), 1, 1, accent);
        }
    }

    /** Long track names scroll inside the capsule instead of being permanently cut in half. */
    private static void drawMarquee(DrawContext context, MinecraftClient client, String value,
                                    int x, int y, int maxWidth, int color) {
        if (maxWidth <= 0 || value == null || value.isBlank()) return;
        int textWidth = client.textRenderer.getWidth(value);
        if (textWidth <= maxWidth) {
            context.drawTextWithShadow(client.textRenderer, Text.literal(value), x, y, color);
            return;
        }

        int gap = 28;
        int cycle = textWidth + gap;
        int offset = (int) ((System.currentTimeMillis() / 45L) % cycle);
        context.enableScissor(x, y - 1, x + maxWidth, y + 10);
        context.drawTextWithShadow(client.textRenderer, Text.literal(value), x - offset, y, color);
        context.drawTextWithShadow(client.textRenderer, Text.literal(value),
                x - offset + cycle, y, color);
        context.disableScissor();
    }

    private static String fpsLabel(MinecraftClient client) {
        return ClientConfig.fpsCounterLabel
                ? "FPS: " + client.getCurrentFps()
                : client.getCurrentFps() + " FPS";
    }

    private static void drawFps(DrawContext context, MinecraftClient client, int x, int y) {
        context.drawTextWithShadow(client.textRenderer, Text.literal(fpsLabel(client)), x, y, 0xFFD4D5DE);
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
        int left = Math.min(centerX + 14,
                Math.max(6, client.getWindow().getScaledWidth() - panelWidth - 6));
        int top = Math.min(centerY + 12,
                Math.max(6, client.getWindow().getScaledHeight() - 38));
        int accent = ClientConfig.effectColor("Target ESP");
        UiDraw.roundedPanel(context, left, top, panelWidth, 32, 8,
                0xE30A0C12, 0x705B606D);
        UiDraw.roundedFill(context, left + 1, top + 6, 2, 20, 1, accent);
        context.drawTextWithShadow(client.textRenderer, Text.literal(name),
                left + 9, top + 6, 0xFFFFFFFF);
        context.drawText(client.textRenderer, Text.literal(health),
                left + 9, top + 17, 0xFFB5BCC9, false);
    }

    private static void drawCompanionBadges(DrawContext context, MinecraftClient client) {
        boolean flashback = FabricLoader.getInstance().isModLoaded("flashback");
        boolean modMenu = FabricLoader.getInstance().isModLoaded("modmenu");
        if (!flashback && !modMenu) return;
        int x = client.getWindow().getScaledWidth() - 30;
        int y = client.getWindow().getScaledHeight() / 2 - ((flashback && modMenu) ? 25 : 12);
        if (flashback) drawBadge(context, x, y, 0xFFBFA7FF, true);
        if (modMenu) drawBadge(context, x, y + (flashback ? 27 : 0), 0xFF9ECBFF, false);
    }

    private static void drawBadge(DrawContext context, int x, int y, int accent, boolean cameraIcon) {
        UiDraw.roundedPanel(context, x - 2, y - 2, 24, 24, 7,
                0xE90B0D12, 0x705B606D);
        UiDraw.roundedFill(context, x + 2, y - 1, 16, 2, 1, accent);
        if (cameraIcon) {
            UiDraw.roundedFill(context, x + 4, y + 7, 13, 9, 2, accent);
            UiDraw.roundedFill(context, x + 7, y + 5, 5, 3, 1, accent);
            UiDraw.roundedFill(context, x + 6, y + 9, 9, 5, 2, 0xFF171923);
            UiDraw.roundedFill(context, x + 9, y + 10, 3, 3, 2, 0xFFFFFFFF);
        } else {
            UiDraw.roundedFill(context, x + 5, y + 6, 5, 5, 1, accent);
            UiDraw.roundedFill(context, x + 12, y + 6, 5, 5, 1, accent);
            UiDraw.roundedFill(context, x + 5, y + 13, 5, 5, 1, accent);
            UiDraw.roundedFill(context, x + 12, y + 13, 5, 5, 1, accent);
        }
    }
}
