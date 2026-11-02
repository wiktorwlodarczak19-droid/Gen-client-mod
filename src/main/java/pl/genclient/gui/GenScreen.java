package pl.genclient.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import pl.genclient.config.ClientConfig;

/** Common sharp GEN CLIENT presentation. No renderBackground/applyBlur calls are used here. */
public abstract class GenScreen extends Screen {
    private static final Identifier LOGO_TEXT = Identifier.of("genclient", "textures/gui/logo_text.png");
    protected final Screen parent;

    protected GenScreen(Text title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        drawBackdrop(context);
        drawHeader(context);
        drawPetals(context);
        renderBody(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }

    /** Draw screen panels and labels here; widgets are drawn afterwards by Screen.render. */
    protected void renderBody(DrawContext context, int mouseX, int mouseY, float delta) {}

    protected void drawBackdrop(DrawContext context) {
        if (client != null && client.world != null) {
            // Keep the 3D world crisp behind pause and in-game settings screens.
            context.fill(0, 0, width, height, 0xA807090E);
            return;
        }

        if (ClientConfig.wallpaper.equals("petal_mist")) {
            drawPetalMist(context);
        } else {
            Identifier wallpaper = wallpaperTexture();
            context.drawTexture(wallpaper, 0, 0, width, height, 0.0f, 0.0f, 480, 270, 480, 270);
            context.fillGradient(0, 0, width, height, 0x40060910, 0x9B07090E);
        }
        context.fill(0, 0, width, 1, 0x22FFFFFF);
    }

    private Identifier wallpaperTexture() {
        return switch (ClientConfig.wallpaper) {
            case "midnight_ink" -> Identifier.of("genclient", "textures/gui/wallpapers/midnight_ink.jpg");
            default -> Identifier.of("genclient", "textures/gui/wallpapers/sakura_dark.jpg");
        };
    }

    private void drawPetalMist(DrawContext context) {
        context.fill(0, 0, width, height, 0xFF090B12);
        context.fillGradient(0, 0, width, height, 0xFF18131E, 0xFF080B13);
        int rose = (ClientConfig.accentColor() & 0x00FFFFFF) | 0x26000000;
        int glowX = width * 3 / 4;
        int glowY = height / 5;
        roundedFill(context, glowX - 76, glowY - 28, 152, 76, 38, rose);
        roundedFill(context, width / 7, height * 3 / 4, 116, 34, 17, 0x183C3657);
    }

    private void drawHeader(DrawContext context) {
        if (client == null) return;
        context.drawTexture(LOGO_TEXT, 28, 18, 152, 15, 0.0f, 0.0f, 687, 65, 687, 65);
        context.drawText(client.textRenderer, "CLIENT EXPERIENCE  /  FABRIC 1.21.1", 30, 37, 0xFFB8BAC3, false);
        int accent = ClientConfig.accentColor();
        context.fill(width - 44, 24, width - 31, 37, accent);
        context.drawText(client.textRenderer, "GEN", width - 27, 27, 0xFFE8EAF0, false);
    }

    private void drawPetals(DrawContext context) {
        if (!ClientConfig.sakuraPetals || (client != null && client.world != null)) return;
        long time = System.currentTimeMillis() / 32L;
        int accent = ClientConfig.accentColor();
        for (int i = 0; i < 17; i++) {
            int x = Math.floorMod(i * 149 + (int) (time * (0.35 + (i % 3) * 0.08)), Math.max(width, 1));
            int y = Math.floorMod(i * 83 + (int) (time * (0.48 + (i % 4) * 0.07)), Math.max(height, 1));
            int color = (accent & 0x00FFFFFF) | ((i % 3 == 0 ? 0x55 : 0x33) << 24);
            context.fill(x, y, x + 3, y + 6, color);
            context.fill(x - 1, y + 2, x + 4, y + 4, color);
        }
    }

    protected void drawPanel(DrawContext context, int x, int y, int panelWidth, int panelHeight) {
        roundedFill(context, x - 3, y - 3, panelWidth + 6, panelHeight + 6, 17, 0x30000000);
        roundedPanel(context, x, y, panelWidth, panelHeight, 15, 0xE30C0E14, 0x705B606D);
        roundedFill(context, x + 1, y + 1, panelWidth - 2, 2, 1, 0x38FFFFFF);
        roundedFill(context, x + 1, y + 13, 2, panelHeight - 26, 1, ClientConfig.accentColor());
    }

    protected void roundedFill(DrawContext context, int x, int y, int w, int h, int radius, int color) {
        UiDraw.roundedFill(context, x, y, w, h, radius, color);
    }

    protected void roundedPanel(DrawContext context, int x, int y, int w, int h, int radius, int fill, int border) {
        UiDraw.roundedPanel(context, x, y, w, h, radius, fill, border);
    }

    protected void drawSectionTitle(DrawContext context, String title, String subtitle, int x, int y) {
        context.drawTextWithShadow(textRenderer, title, x, y, 0xFFFFFFFF);
        context.drawText(textRenderer, subtitle, x, y + 15, 0xFF9BA2B1, false);
    }

    @Override
    public void close() {
        if (client == null) return;
        if (parent != null) client.setScreen(parent);
        else client.setScreen(null);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
