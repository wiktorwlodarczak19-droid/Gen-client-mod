package pl.genclient.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import pl.genclient.config.ClientConfig;

public final class StyleScreen extends GenScreen {
    public StyleScreen(Screen parent) {
        super(Text.literal("GEN CLIENT STYLE"), parent);
    }

    @Override
    protected void init() {
        int panelWidth = Math.min(560, width - 40);
        int panelX = (width - panelWidth) / 2;
        int panelY = Math.max(60, (height - 360) / 2);
        int x = panelX + 34;
        int y = panelY + 95;
        int buttonWidth = panelWidth - 68;

        addDrawableChild(new GenButton(x, y, buttonWidth, 26, accentLabel(), () -> {
            ClientConfig.cycleAccent();
            refreshMessages();
        }));
        addDrawableChild(new GenButton(x, y + 39, buttonWidth, 24, wallpaperLabel(), () -> {
            String next = switch (ClientConfig.wallpaper) {
                case "sakura_dark" -> "midnight_ink";
                case "midnight_ink" -> "petal_mist";
                default -> "sakura_dark";
            };
            ClientConfig.setWallpaper(next);
            refreshMessages();
        }));
        addDrawableChild(new GenButton(x, y + 78, buttonWidth, 24,
                toggleLabel("FALLING SAKURA", ClientConfig.sakuraPetals), () -> {
            ClientConfig.sakuraPetals = !ClientConfig.sakuraPetals;
            ClientConfig.save();
            refreshMessages();
        }));
        addDrawableChild(new GenButton(x, y + 117, buttonWidth, 24,
                toggleLabel("DYNAMIC ISLAND HUD", ClientConfig.dynamicIsland), () -> {
            ClientConfig.dynamicIsland = !ClientConfig.dynamicIsland;
            ClientConfig.save();
            refreshMessages();
        }));
        addDrawableChild(new GenButton(x, y + 156, buttonWidth, 24,
                toggleLabel("COMPANION MOD BADGES", ClientConfig.modBadges), () -> {
            ClientConfig.modBadges = !ClientConfig.modBadges;
            ClientConfig.save();
            refreshMessages();
        }));
        addDrawableChild(new GenButton(panelX + panelWidth - 144, panelY + 308, 110, 24,
                Text.literal("BACK"), this::close));
    }

    @Override
    protected void renderBody(DrawContext context, int mouseX, int mouseY, float delta) {
        int panelWidth = Math.min(560, width - 40);
        int panelX = (width - panelWidth) / 2;
        int panelY = Math.max(60, (height - 360) / 2);
        drawPanel(context, panelX, panelY, panelWidth, 360);
        drawSectionTitle(context, "STYLE & PERSONALIZATION", "Sakura Dark / Pearl / Ice / Lavender / Mint.",
                panelX + 34, panelY + 26);
        context.drawText(textRenderer, "ACCENT COLOR", panelX + 34, panelY + 88, 0xFF8F98A8, false);
        context.drawText(textRenderer, "WALLPAPER", panelX + 34, panelY + 127, 0xFF8F98A8, false);
        context.drawText(textRenderer, "ATMOSPHERE", panelX + 34, panelY + 166, 0xFF8F98A8, false);
        context.drawText(textRenderer, "HUD", panelX + 34, panelY + 205, 0xFF8F98A8, false);
        context.drawText(textRenderer, "INTEGRATION", panelX + 34, panelY + 244, 0xFF8F98A8, false);
        context.drawText(textRenderer, "Settings are stored in config/genclient.json", panelX + 34,
                panelY + 340, 0xFF858D9B, false);
    }

    @Override
    public boolean shouldPause() {
        return client != null && client.world != null;
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
    private static Text wallpaperLabel() { return Text.literal("WALLPAPER  /  " + ClientConfig.wallpaper.replace('_', ' ').toUpperCase()); }
    private static Text toggleLabel(String title, boolean enabled) {
        return Text.literal((enabled ? "●  ON   /   " : "○  OFF  /   ") + title);
    }
}
