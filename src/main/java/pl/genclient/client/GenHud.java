package pl.genclient.client;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.text.Text;
import pl.genclient.config.ClientConfig;
import pl.genclient.gui.UiDraw;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** Compact Dynamic Island HUD: clock and measured FPS at the left, media capsule at the right. */
public final class GenHud {
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");
    private static final Identifier LOGO_MARK = Identifier.of("genclient", "textures/gui/logo_mark.png");
    private GenHud() {}
    public static void register() { HudRenderCallback.EVENT.register((context, tickCounter) -> render(context)); }

    private static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.options.hudHidden) return;
        if (ClientConfig.dynamicIsland) {
            context.drawTextWithShadow(client.textRenderer, Text.literal(LocalTime.now().format(CLOCK)), 24, 26, 0xFFF2F2F5);
            drawIsland(context, client);
        }
        if (ClientConfig.fpsCounter) {
            String fps = ClientConfig.fpsCounterLabel ? "FPS: " + client.getCurrentFps() : client.getCurrentFps() + " FPS";
            context.drawTextWithShadow(client.textRenderer, Text.literal(fps), 110, 26, 0xFFE8EAF0);
        }
        if (ClientConfig.isEnabled("Target ESP")) drawTarget(context, client);
        if (ClientConfig.modBadges) drawCompanionBadges(context, client);
    }

    private static void drawIsland(DrawContext context, MinecraftClient client) {
        int screenWidth = client.getWindow().getScaledWidth();
        int islandWidth = Math.min(560, Math.max(270, (int) (screenWidth * 0.68f)));
        if (screenWidth < 520) islandWidth = Math.max(210, screenWidth - 18);
        int x = Math.max(9, screenWidth - islandWidth - 9);
        int y = 14, height = 48;
        int accent = ClientConfig.islandAccentColor();
        SpotifyService.TrackSnapshot track = SpotifyService.getSnapshot();
        boolean spotifyPlaying = track.hasTrack() && track.isPlaying();
        boolean gameMusicPlaying = client.getMusicTracker().isPlayingType(client.getMusicType());
        boolean playing = spotifyPlaying || gameMusicPlaying;

        UiDraw.roundedFill(context, x - 4, y - 3, islandWidth + 8, height + 6, 25, 0x38000000);
        UiDraw.roundedPanel(context, x, y, islandWidth, height, 24, 0xF20D0D16, 0xA05A536D);
        UiDraw.roundedFill(context, x + 18, y + 1, islandWidth - 36, 1, 1, 0x42FFFFFF);
        UiDraw.roundedFill(context, x + 9, y + 8, 32, 32, 16, 0xFF262230);
        context.drawTexture(LOGO_MARK, x + 11, y + 8, 28, 32, 0, 0, 530, 483, 530, 483);

        String line = ClientConfig.islandMessageEnabled && !ClientConfig.islandMessage.isBlank()
                ? ClientConfig.islandMessage
                : track.hasTrack() ? track.title() + "  ·  " + track.artists()
                : SpotifyService.isConnected() ? "Spotify ready — start playback" : "Connect Spotify in PLUGS";
        int barsX = x + islandWidth - 47;
        int textLeft = x + 54, textRight = barsX - 12;
        String trimmed = client.textRenderer.trimToWidth(line, Math.max(24, textRight - textLeft));
        int center = (textLeft + textRight) / 2;
        if (ClientConfig.islandMessageEnabled && !ClientConfig.islandMessage.isBlank())
            drawHighlightedLastWord(context, client, trimmed, center, y + 17, accent);
        else
            context.drawCenteredTextWithShadow(client.textRenderer, Text.literal(trimmed), center, y + 17, 0xFFF5F1F7);

        long animation = System.currentTimeMillis() / 120L;
        int visualizerColor = playing ? accent : 0xFF8A8494;
        for (int i = 0; i < 5; i++) {
            int barHeight = ClientConfig.islandVisualizerAnimated && playing
                    ? 7 + Math.floorMod((int) animation + i * 3, 5) * 3 : 7;
            UiDraw.roundedFill(context, barsX + i * 7, y + (height - barHeight) / 2, 4, barHeight, 2, visualizerColor);
        }

        int progressX = x + 14, progressY = y + height - 4, progressWidth = islandWidth - 28;
        UiDraw.roundedFill(context, progressX, progressY, progressWidth, 2, 1, 0x665F596A);
        if (track.hasTrack() && track.durationMs() > 0) {
            float progress = Math.max(0.0f, Math.min(1.0f, track.currentProgressMs() / (float) track.durationMs()));
            UiDraw.roundedFill(context, progressX, progressY, Math.max(2, (int) (progressWidth * progress)), 2, 1, accent);
        } else if (playing) {
            int pulse = 22 + (int) Math.floorMod(animation, 42);
            UiDraw.roundedFill(context, progressX, progressY, Math.min(progressWidth, pulse), 2, 1, accent);
        }
    }

    private static void drawHighlightedLastWord(DrawContext context, MinecraftClient client, String value,
                                                   int centerX, int y, int accent) {
        if (value.isBlank()) return;
        int split = value.lastIndexOf(' ');
        String prefix = split >= 0 ? value.substring(0, split + 1) : "";
        String word = split >= 0 ? value.substring(split + 1) : value;
        int prefixWidth = client.textRenderer.getWidth(prefix), wordWidth = client.textRenderer.getWidth(word);
        int left = centerX - (prefixWidth + wordWidth) / 2;
        if (!prefix.isEmpty()) context.drawTextWithShadow(client.textRenderer, Text.literal(prefix), left, y, 0xFFF5F1F7);
        UiDraw.roundedFill(context, left + prefixWidth - 2, y - 3, wordWidth + 5, 15, 8,
                (accent & 0x00FFFFFF) | 0x66000000);
        context.drawTextWithShadow(client.textRenderer, Text.literal(word), left + prefixWidth, y, 0xFFFFF6FB);
    }

    private static void drawTarget(DrawContext context, MinecraftClient client) {
        if (!(client.crosshairTarget instanceof EntityHitResult hit)) return;
        Entity target = hit.getEntity();
        if (target == client.player || !target.isAlive()) return;
        int centerX = client.getWindow().getScaledWidth() / 2, centerY = client.getWindow().getScaledHeight() / 2;
        String name = target.getName().getString();
        if (name.length() > 22) name = name.substring(0, 19) + "...";
        String health = target instanceof LivingEntity living ? MathHelper.ceil(living.getHealth()) + " HP" : "ENTITY";
        int panelWidth = Math.max(104, client.textRenderer.getWidth(name) + 42);
        int left = Math.min(centerX + 14, Math.max(6, client.getWindow().getScaledWidth() - panelWidth - 6));
        int top = Math.min(centerY + 12, Math.max(6, client.getWindow().getScaledHeight() - 38));
        int accent = ClientConfig.effectColor("Target ESP");
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
        if (modMenu) drawBadge(context, x, y + (flashback ? 27 : 0), 0xFF9ECBFF, false);
    }
    private static void drawBadge(DrawContext context, int x, int y, int accent, boolean cameraIcon) {
        UiDraw.roundedPanel(context, x - 2, y - 2, 24, 24, 7, 0xE90B0D12, 0x705B606D);
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
