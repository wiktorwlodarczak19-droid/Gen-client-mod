package pl.genclient.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.hit.EntityHitResult;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import pl.genclient.config.ClientConfig;
import pl.genclient.gui.UiDraw;

/** Compact, centered media capsule inspired by modern desktop dynamic-island HUDs. */
public final class GenHud {
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

    /** A small centered capsule; it deliberately never expands into a full-width banner. */
    private static void drawIsland(DrawContext context, MinecraftClient client) {
        int screenWidth = client.getWindow().getScaledWidth();
        int width = Math.min(326, Math.max(170, screenWidth - 60));
        int height = 36;
        int x = (screenWidth - width) / 2;
        int y = 6;
        int accent = ClientConfig.islandAccentColor();

        SpotifyService.TrackSnapshot track = SpotifyService.getSnapshot();
        boolean hasTrack = track.hasTrack();
        boolean playing = hasTrack && track.isPlaying();

        UiDraw.roundedFill(context, x - 3, y - 2, width + 6, height + 4, 21, 0x52000000);
        UiDraw.roundedPanel(context, x, y, width, height, 18, 0xEF0B0D15, 0xB05D586F);
        UiDraw.roundedFill(context, x + 12, y + 1, width - 24, 1, 1, 0x44FFFFFF);

        // Music mark
        UiDraw.roundedFill(context, x + 7, y + 7, 22, 22, 11, 0xFF242333);
        context.drawCenteredTextWithShadow(client.textRenderer, Text.literal("♫"),
                x + 18, y + 13, accent);

        int visualizerX = x + width - 29;
        int textX = x + 36;
        int textWidth = Math.max(48, visualizerX - textX - 10);
        String title = hasTrack ? track.title()
                : SpotifyService.isConnected() ? "Spotify connected" : "Connect Spotify in PLUGS";
        String artist = hasTrack ? track.artists()
                : SpotifyService.isConnected() ? "Start playback to show your music" : "Link your account to show music";

        drawMarquee(context, client, title, textX, y + 7, textWidth, 0xFFF3F1F7);
        String visibleArtist = client.textRenderer.trimToWidth(artist, textWidth);
        context.drawText(client.textRenderer, Text.literal(visibleArtist), textX, y + 20, 0xFFAAA7B7, false);

        long animation = System.currentTimeMillis() / 120L;
        int visualizerColor = playing ? accent : 0xFF777484;
        for (int i = 0; i < 3; i++) {
            int barHeight = playing && ClientConfig.islandVisualizerAnimated
                    ? 5 + Math.floorMod((int) animation + i * 2, 4) * 3 : 5;
            UiDraw.roundedFill(context, visualizerX + i * 6, y + (height - barHeight) / 2,
                    3, barHeight, 2, visualizerColor);
        }

        if (ClientConfig.fpsCounter) {
            drawFps(context, client, Math.max(5, x - 12 - client.textRenderer.getWidth(fpsLabel(client))),
                    y + 13);
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
        return client.getCurrentFps() + " fps";
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
