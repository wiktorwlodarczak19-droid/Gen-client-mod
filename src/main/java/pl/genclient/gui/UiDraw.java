package pl.genclient.gui;

import net.minecraft.client.gui.DrawContext;

/** Small immediate-mode drawing helpers shared by GEN CLIENT screens and HUD. */
public final class UiDraw {
    private UiDraw() {}

    public static void roundedFill(DrawContext context, int x, int y, int width, int height,
                                   int radius, int color) {
        if (width <= 0 || height <= 0) return;
        int r = Math.max(0, Math.min(radius, Math.min(width, height) / 2));
        for (int row = 0; row < height; row++) {
            double edgeDistance = row < r ? r - row - 0.5
                    : row >= height - r ? row - (height - r) + 0.5 : 0.0;
            int inset = edgeDistance <= 0.0 ? 0
                    : (int) Math.ceil(r - Math.sqrt(Math.max(0.0, r * (double) r - edgeDistance * edgeDistance)));
            context.fill(x + inset, y + row, x + width - inset, y + row + 1, color);
        }
    }

    public static void roundedPanel(DrawContext context, int x, int y, int width, int height,
                                    int radius, int fill, int border) {
        roundedFill(context, x - 1, y - 1, width + 2, height + 2, radius + 1, border);
        roundedFill(context, x, y, width, height, radius, fill);
    }
}
