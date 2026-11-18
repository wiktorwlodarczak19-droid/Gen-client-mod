package pl.genclient.gui;

import net.minecraft.client.gui.DrawContext;

/**
 * Small original icons for GEN CLIENT modules.
 *
 * Icons are drawn from rounded primitives so they remain crisp at GUI scale,
 * do not depend on a symbol font, and keep the menu visually consistent.
 */
final class ModuleIconRenderer {
    private ModuleIconRenderer() {}

    static void draw(DrawContext context, String module, int x, int y,
                     int accent, boolean enabled) {
        int color = enabled ? accent : 0xFF858B98;
        int soft = (color & 0x00FFFFFF) | 0x99000000;

        switch (module) {
            case "Jump Circle" -> {
                dot(context, x + 10, y + 2, color);
                dot(context, x + 17, y + 5, color);
                dot(context, x + 19, y + 12, color);
                dot(context, x + 14, y + 19, color);
                dot(context, x + 6, y + 19, color);
                dot(context, x + 2, y + 12, color);
                dot(context, x + 4, y + 5, color);
                UiDraw.roundedFill(context, x + 10, y + 9, 4, 7, 2, soft);
                UiDraw.roundedFill(context, x + 8, y + 11, 3, 2, 1, color);
                UiDraw.roundedFill(context, x + 13, y + 11, 3, 2, 1, color);
            }
            case "Glass Vapor" -> {
                UiDraw.roundedFill(context, x + 4, y + 8, 3, 10, 2, color);
                UiDraw.roundedFill(context, x + 10, y + 3, 3, 16, 2, color);
                UiDraw.roundedFill(context, x + 16, y + 6, 3, 12, 2, color);
                UiDraw.roundedFill(context, x + 5, y + 4, 2, 2, 1, soft);
                UiDraw.roundedFill(context, x + 16, y + 2, 2, 2, 1, soft);
            }
            case "Target ESP" -> {
                UiDraw.roundedFill(context, x + 4, y + 3, 7, 2, 1, color);
                UiDraw.roundedFill(context, x + 4, y + 3, 2, 7, 1, color);
                UiDraw.roundedFill(context, x + 13, y + 3, 7, 2, 1, color);
                UiDraw.roundedFill(context, x + 18, y + 3, 2, 7, 1, color);
                UiDraw.roundedFill(context, x + 4, y + 14, 2, 7, 1, color);
                UiDraw.roundedFill(context, x + 4, y + 19, 7, 2, 1, color);
                UiDraw.roundedFill(context, x + 18, y + 14, 2, 7, 1, color);
                UiDraw.roundedFill(context, x + 13, y + 19, 7, 2, 1, color);
                UiDraw.roundedFill(context, x + 10, y + 10, 4, 4, 2, soft);
                UiDraw.roundedFill(context, x + 11, y + 11, 2, 2, 1, color);
            }
            case "Kill Effect" -> {
                UiDraw.roundedFill(context, x + 10, y + 10, 4, 4, 2, color);
                UiDraw.roundedFill(context, x + 11, y + 2, 2, 5, 1, color);
                UiDraw.roundedFill(context, x + 11, y + 17, 2, 5, 1, color);
                UiDraw.roundedFill(context, x + 2, y + 11, 5, 2, 1, color);
                UiDraw.roundedFill(context, x + 17, y + 11, 5, 2, 1, color);
                dot(context, x + 5, y + 5, soft);
                dot(context, x + 16, y + 5, soft);
                dot(context, x + 5, y + 16, soft);
                dot(context, x + 16, y + 16, soft);
            }
            case "Hit Sparks" -> {
                UiDraw.roundedFill(context, x + 10, y + 2, 4, 7, 2, color);
                UiDraw.roundedFill(context, x + 10, y + 15, 4, 7, 2, color);
                UiDraw.roundedFill(context, x + 2, y + 10, 7, 4, 2, color);
                UiDraw.roundedFill(context, x + 15, y + 10, 7, 4, 2, color);
                UiDraw.roundedFill(context, x + 10, y + 10, 4, 4, 2, soft);
            }
            case "Custom Sky" -> {
                UiDraw.roundedFill(context, x + 9, y + 4, 6, 6, 3, color);
                UiDraw.roundedFill(context, x + 11, y + 1, 2, 2, 1, color);
                UiDraw.roundedFill(context, x + 11, y + 11, 2, 3, 1, color);
                UiDraw.roundedFill(context, x + 5, y + 6, 3, 2, 1, color);
                UiDraw.roundedFill(context, x + 16, y + 6, 3, 2, 1, color);
                UiDraw.roundedFill(context, x + 4, y + 16, 16, 2, 1, color);
                UiDraw.roundedFill(context, x + 7, y + 20, 10, 2, 1, soft);
            }
            case "Glint Hand" -> {
                UiDraw.roundedFill(context, x + 6, y + 5, 3, 9, 2, color);
                UiDraw.roundedFill(context, x + 10, y + 3, 3, 11, 2, color);
                UiDraw.roundedFill(context, x + 14, y + 6, 3, 8, 2, color);
                UiDraw.roundedFill(context, x + 5, y + 12, 13, 8, 3, soft);
                UiDraw.roundedFill(context, x + 3, y + 12, 4, 6, 2, color);
            }
            case "Aura Glint Light 3D" -> {
                UiDraw.roundedFill(context, x + 11, y + 2, 2, 4, 1, color);
                UiDraw.roundedFill(context, x + 8, y + 6, 8, 3, 1, color);
                UiDraw.roundedFill(context, x + 5, y + 9, 14, 5, 2, color);
                UiDraw.roundedFill(context, x + 8, y + 14, 8, 3, 1, color);
                UiDraw.roundedFill(context, x + 11, y + 17, 2, 4, 1, color);
                UiDraw.roundedFill(context, x + 11, y + 7, 2, 10, 1, soft);
            }
            case "Armor Glass Light 3D" -> {
                UiDraw.roundedFill(context, x + 6, y + 4, 12, 3, 1, color);
                UiDraw.roundedFill(context, x + 4, y + 7, 4, 8, 2, color);
                UiDraw.roundedFill(context, x + 16, y + 7, 4, 8, 2, color);
                UiDraw.roundedFill(context, x + 7, y + 14, 10, 5, 2, color);
                UiDraw.roundedFill(context, x + 9, y + 8, 6, 5, 2, soft);
                UiDraw.roundedFill(context, x + 11, y + 8, 2, 5, 1, color);
            }
            case "Dynamic Island" -> {
                UiDraw.roundedPanel(context, x + 2, y + 7, 20, 10, 5, soft, color);
                UiDraw.roundedFill(context, x + 6, y + 11, 4, 2, 1, color);
                UiDraw.roundedFill(context, x + 14, y + 11, 4, 2, 1, color);
            }
            case "FPS Counter" -> {
                UiDraw.roundedFill(context, x + 6, y + 3, 12, 3, 1, color);
                UiDraw.roundedFill(context, x + 6, y + 3, 3, 18, 1, color);
                UiDraw.roundedFill(context, x + 8, y + 10, 9, 3, 1, color);
            }
            case "Low FX Mode" -> {
                UiDraw.roundedFill(context, x + 10, y + 3, 4, 17, 2, color);
                UiDraw.roundedFill(context, x + 5, y + 8, 6, 3, 2, color);
                UiDraw.roundedFill(context, x + 13, y + 13, 6, 3, 2, color);
            }
            case "Sky Petals" -> {
                UiDraw.roundedFill(context, x + 5, y + 5, 4, 6, 2, color);
                UiDraw.roundedFill(context, x + 14, y + 3, 4, 6, 2, soft);
                UiDraw.roundedFill(context, x + 10, y + 13, 4, 6, 2, color);
                UiDraw.roundedFill(context, x + 17, y + 16, 3, 3, 1, soft);
            }
            case "Spotify Player" -> {
                UiDraw.roundedFill(context, x + 10, y + 4, 3, 14, 1, color);
                UiDraw.roundedFill(context, x + 10, y + 4, 9, 3, 1, color);
                UiDraw.roundedFill(context, x + 5, y + 16, 7, 4, 2, color);
                UiDraw.roundedFill(context, x + 14, y + 14, 7, 4, 2, color);
            }
            case "Island Text" -> {
                UiDraw.roundedFill(context, x + 4, y + 5, 16, 3, 1, color);
                UiDraw.roundedFill(context, x + 11, y + 8, 3, 12, 1, color);
                UiDraw.roundedFill(context, x + 7, y + 18, 11, 2, 1, soft);
            }
            case "Companion Badges" -> {
                UiDraw.roundedFill(context, x + 5, y + 4, 14, 3, 1, color);
                UiDraw.roundedFill(context, x + 4, y + 7, 16, 8, 3, color);
                UiDraw.roundedFill(context, x + 7, y + 15, 10, 4, 2, color);
                UiDraw.roundedFill(context, x + 10, y + 8, 4, 6, 2, soft);
            }
            default -> {
                UiDraw.roundedFill(context, x + 8, y + 8, 8, 8, 4, color);
            }
        }
    }

    private static void dot(DrawContext context, int x, int y, int color) {
        UiDraw.roundedFill(context, x, y, 3, 3, 1, color);
    }
}
