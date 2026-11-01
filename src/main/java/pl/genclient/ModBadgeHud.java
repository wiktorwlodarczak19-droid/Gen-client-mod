package pl.genclient;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Small right-side strip showing optional companion mods that are loaded.
 * The badges are intentionally lightweight and do not depend on those mods.
 */
public final class ModBadgeHud {
    private ModBadgeHud() {}

    public static void register() {
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden) return;

            boolean flashback = FabricLoader.getInstance().isModLoaded("flashback");
            boolean modMenu = FabricLoader.getInstance().isModLoaded("modmenu");
            if (!flashback && !modMenu) return;

            int x = client.getWindow().getScaledWidth() - 30;
            int y = client.getWindow().getScaledHeight() / 2 - (flashback && modMenu ? 25 : 12);
            drawBadge(context, x, y, flashback ? "F" : "M",
                    flashback ? 0xFFB58CFF : 0xFF79C8FF,
                    flashback ? "Flashback" : "Mod Menu");
            if (flashback && modMenu) {
                drawBadge(context, x, y + 27, "M", 0xFF79C8FF, "Mod Menu");
            }
        });
    }

    private static void drawBadge(DrawContext context, int x, int y, String letter,
                                 int accent, String tooltip) {
        context.fill(x - 2, y - 2, x + 22, y + 22, 0xD9161924);
        context.fill(x - 2, y - 2, x + 22, y, accent);
        context.fill(x - 2, y + 20, x + 22, y + 22, 0xAA000000);
        context.drawCenteredTextWithShadow(
                MinecraftClient.getInstance().textRenderer,
                Text.literal(letter), x + 10, y + 5, 0xFFFFFFFF
        );
    }
}
