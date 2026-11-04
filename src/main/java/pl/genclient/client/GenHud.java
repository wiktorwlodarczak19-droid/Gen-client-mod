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

        if (ClientConfig.dynamicIsland) drawIsland(context, client);
        if (ClientConfig.isEnabled("Target ESP")) drawTarget(context, client);
        if (ClientConfig.modBadges) drawCompanionBadges(context, client);
    }

    private static void drawIsland(DrawContext context, MinecraftClient client) {
        int screenWidth = client.getWindow().getScaledWidth();
        int islandWidth = 520;
        int x = screenWidth / 2 - islandWidth / 2;
        int y = 7;
        int accent = ClientConfig.accentColor();
        int islandHeight = 39;

        UiDraw.roundedFill(context, x - 3, y - 2, islandWidth + 6, islandHeight + 5, 20, 0x33000000);
        UiDraw.roundedPanel(context, x, y, islandWidth, islandHeight, 18, 0xF20A0C12, 0x805B606D);
        UiDraw.roundedFill(context, x + 17, y + 1, islandWidth - 34, 1, 1, 0x55FFFFFF);
        UiDraw.roundedFill(context, x + 13, y + 7, 3, 25, 2, accent);

        context.drawTextWithShadow(client.textRenderer, Text.literal("GEN"), x + 24, y + 8, 0xFFFFFFFF);
        context.drawText(client.textRenderer, Text.literal("CLIENT"), x + 24, y + 20, 0xFF9097A5, false);

        String clock = LocalTime.now().format(CLOCK);
        context.drawCenteredTextWithShadow(client.textRenderer, clock, x + 158, y + 8, 0xFFF2F2F5);
        context.drawCenteredTextWithShadow(client.textRenderer, Text.literal("LOCAL TIME"), x + 158, y + 21, 0xFF858D9B);

        String fps = client.getCurrentFps() + " FPS";
        context.drawTextWithShadow(client.textRenderer, Text.literal(fps), x + 220, y + 8, 0xFFE8EAF0);
        var serverEntry = client.getCurrentServerEntry();
        String lowerLabel = serverEntry == null ? "SINGLEPLAYER" : serverEntry.address.toUpperCase();
        if (serverEntry != null && serverEntry.ping >= 0) lowerLabel += "  /  " + serverEntry.ping + " MS";
        boolean truncated = client.textRenderer.getWidth(lowerLabel) > 95;
        if (truncated) {
            while (!lowerLabel.isEmpty() && client.textRenderer.getWidth(lowerLabel + "...") > 95) {
                lowerLabel = lowerLabel.substring(0, lowerLabel.length() - 1);
            }
            lowerLabel += "...";
        }
        context.drawText(client.textRenderer, Text.literal(lowerLabel), x + 220, y + 21, 0xFF9BA2B0, false);

        SpotifyService.TrackSnapshot track = SpotifyService.getSnapshot();
        String trackTitle = track.hasTrack() ? track.title() : "SPOTIFY NOT CONNECTED";
        String trackArtist = track.hasTrack()
                ? track.artists() + "  ·  " + track.progressLabel()
                : "STYLE  /  SPOTIFY TO CONNECT";
        trackTitle = client.textRenderer.trimToWidth(trackTitle, 164);
        trackArtist = client.textRenderer.trimToWidth(trackArtist, 164);

        int musicColor = track.hasTrack() && track.isPlaying() ? 0xFF8EE3B2 : 0xFF858D9B;
        UiDraw.roundedFill(context, x + 328, y + 15, 4, 4, 2, musicColor);
        context.drawTextWithShadow(client.textRenderer, Text.literal(trackTitle),
                x + 340, y + 8, track.hasTrack() ? 0xFFFFFFFF : 0xFFB9C0CC);
        context.drawText(client.textRenderer, Text.literal(trackArtist),
                x + 340, y + 21, 0xFF9BA2B0, false);

        context.fill(x + 112, y + 8, x + 113, y + 30, 0x2EFFFFFF);
        context.fill(x + 208, y + 8, x + 209, y + 30, 0x2EFFFFFF);
        context.fill(x + 320, y + 8, x + 321, y + 30, 0x2EFFFFFF);
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
