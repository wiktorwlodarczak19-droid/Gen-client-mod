package pl.genclient.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import pl.genclient.config.ClientConfig;

public final class StyleScreen extends GenScreen {
    private int panelWidth;
    private int panelHeight;
    private int panelX;
    private int panelY;
    private int buttonX;
    private int buttonWidth;
    private int firstY;
    private int rowGap;
    private String configMessage = "CONFIG  /  config/genclient.json";

    public StyleScreen(Screen parent) {
        super(Text.literal("GEN CLIENT STYLE"), parent);
    }

    @Override
    protected void init() {
        panelWidth = Math.min(560, width - 40);
        panelHeight = Math.min(360, height - 96);
        panelHeight = Math.max(220, panelHeight);
        panelX = (width - panelWidth) / 2;
        panelY = Math.max(58, (height - panelHeight) / 2);
        buttonX = panelX + 34;
        buttonWidth = panelWidth - 68;
        firstY = panelY + Math.max(78, Math.min(92, panelHeight / 4));
        rowGap = Math.max(22, Math.min(39, (panelHeight - 162) / 4));

        addDrawableChild(new GenButton(buttonX, firstY, buttonWidth, 26, accentLabel(), () -> {
            ClientConfig.cycleAccent();
            refreshMessages();
        }));
        addDrawableChild(new GenButton(buttonX, firstY + rowGap, buttonWidth, 24, wallpaperLabel(), () -> {
            ClientConfig.cycleWallpaper();
            refreshMessages();
        }));
        addDrawableChild(new GenButton(buttonX, firstY + rowGap * 2, buttonWidth, 24,
                toggleLabel("FALLING SAKURA", ClientConfig.sakuraPetals), () -> {
            ClientConfig.sakuraPetals = !ClientConfig.sakuraPetals;
            ClientConfig.save();
            refreshMessages();
        }));
        addDrawableChild(new GenButton(buttonX, firstY + rowGap * 3, buttonWidth, 24,
                toggleLabel("DYNAMIC ISLAND HUD", ClientConfig.dynamicIsland), () -> {
            ClientConfig.dynamicIsland = !ClientConfig.dynamicIsland;
            ClientConfig.save();
            refreshMessages();
        }));
        addDrawableChild(new GenButton(buttonX, firstY + rowGap * 4, buttonWidth, 24,
                toggleLabel("COMPANION MOD BADGES", ClientConfig.modBadges), () -> {
            ClientConfig.modBadges = !ClientConfig.modBadges;
            ClientConfig.save();
            refreshMessages();
        }));

        int footerY = panelY + panelHeight - 42;
        addDrawableChild(new GenButton(panelX + 30, footerY, 98, 24,
                Text.literal("SPOTIFY"), () -> client.setScreen(new SpotifyScreen(this)), true));
        addDrawableChild(new GenButton(panelX + 136, footerY, 96, 24,
                Text.literal("EXPORT"), () -> configMessage = ClientConfig.exportConfig()));
        addDrawableChild(new GenButton(panelX + 240, footerY, 96, 24,
                Text.literal("IMPORT"), () -> {
            configMessage = ClientConfig.importConfig();
            refreshMessages();
        }));
        addDrawableChild(new GenButton(panelX + panelWidth - 116, footerY, 82, 24,
                Text.literal("BACK"), this::close));
    }

    @Override
    protected void renderBody(DrawContext context, int mouseX, int mouseY, float delta) {
        drawPanel(context, panelX, panelY, panelWidth, panelHeight);
        drawSectionTitle(context, "STYLE & PERSONALIZATION",
                "Sakura Dark / Pearl / Ice / Lavender / Mint.",
                panelX + 34, panelY + 26);
        context.drawText(textRenderer, "ACCENT COLOR", panelX + 34, firstY - 14, 0xFF8F98A8, false);
        context.drawText(textRenderer, "WALLPAPER", panelX + 34, firstY + rowGap - 14, 0xFF8F98A8, false);
        context.drawText(textRenderer, "ATMOSPHERE", panelX + 34, firstY + rowGap * 2 - 14, 0xFF8F98A8, false);
        context.drawText(textRenderer, "HUD", panelX + 34, firstY + rowGap * 3 - 14, 0xFF8F98A8, false);
        context.drawText(textRenderer, "INTEGRATION", panelX + 34, firstY + rowGap * 4 - 14, 0xFF8F98A8, false);
        context.drawText(textRenderer, textRenderer.trimToWidth(configMessage, panelWidth - 68),
                panelX + 34, panelY + panelHeight - 14, 0xFF858D9B, false);
    }

    private void refreshMessages() {
        int index = 0;
        for (var child : children()) {
            if (child instanceof GenButton button) {
                switch (index) {
                    case 0 -> button.setMessage(accentLabel());
                    case 1 -> button.setMessage(wallpaperLabel());
                    case 2 -> button.setMessage(toggleLabel("FALLING SAKURA", ClientConfig.sakuraPetals));
                    case 3 -> button.setMessage(toggleLabel("DYNAMIC ISLAND HUD", ClientConfig.dynamicIsland));
                    case 4 -> button.setMessage(toggleLabel("COMPANION MOD BADGES", ClientConfig.modBadges));
                    default -> { }
                }
                index++;
            }
        }
    }

    private static Text accentLabel() { return Text.literal("ACCENT  /  " + ClientConfig.accent); }
    private static Text wallpaperLabel() {
        return Text.literal("WALLPAPER  /  " + ClientConfig.wallpaper.replace('_', ' ').toUpperCase());
    }
    private static Text toggleLabel(String title, boolean enabled) {
        return Text.literal((enabled ? "●  ON   /   " : "○  OFF  /   ") + title);
    }
}
