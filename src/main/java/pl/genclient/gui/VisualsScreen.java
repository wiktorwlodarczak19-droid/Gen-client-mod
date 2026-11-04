package pl.genclient.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import pl.genclient.config.ClientConfig;

import java.util.List;

public final class VisualsScreen extends GenScreen {
    private static final List<String> MODULES = List.of(
            "Jump Circle", "Glass Vapor",
            "Target ESP", "Kill Effect",
            "Hit Sparks", "Custom Sky",
            "Glint Hand", "Aura Glint Light 3D",
            "Armor Glass Light 3D"
    );

    public VisualsScreen(Screen parent) {
        super(Text.literal("GEN CLIENT VISUALS"), parent);
    }

    private int panelWidth;
    private int panelHeight;
    private int panelX;
    private int panelY;
    private int cardWidth;
    private int startX;
    private int startY;
    private int rowGap;

    @Override
    protected void init() {
        panelWidth = Math.min(690, width - 40);
        panelHeight = Math.min(480, height - 72);
        panelHeight = Math.max(220, panelHeight);
        panelX = (width - panelWidth) / 2;
        panelY = Math.max(36, (height - panelHeight) / 2);

        int gap = 16;
        cardWidth = (panelWidth - 64 - gap) / 2;
        int rows = (MODULES.size() + 1) / 2;
        int footerY = panelHeight - 42;
        int contentTop = Math.min(116, Math.max(78, panelHeight / 4));
        startX = panelX + 24;
        startY = panelY + contentTop;
        rowGap = rows <= 1 ? 0 : Math.max(30,
                Math.min(58, (footerY - contentTop - 72) / (rows - 1)));

        for (int i = 0; i < MODULES.size(); i++) {
            String module = MODULES.get(i);
            int column = i % 2;
            int row = i / 2;
            int x = startX + column * (cardWidth + gap);
            int y = startY + row * rowGap;
            addDrawableChild(new GenButton(x, y, cardWidth, 27, moduleState(module), () -> {
                ClientConfig.toggle(module);
                for (var child : children()) {
                    if (child instanceof GenButton button && button.getMessage().getString().endsWith(module)) {
                        button.setMessage(moduleState(module));
                        break;
                    }
                }
            }));
        }

        int footerTop = panelY + footerY;
        addDrawableChild(new GenButton(panelX + 24, footerTop, 180, 24,
                Text.literal("STYLE SETTINGS"), () -> client.setScreen(new StyleScreen(this))));
        addDrawableChild(new GenButton(panelX + panelWidth - 154, footerTop, 130, 24,
                Text.literal("CLOSE"), this::close));
    }

    @Override
    protected void renderBody(DrawContext context, int mouseX, int mouseY, float delta) {
        drawPanel(context, panelX, panelY, panelWidth, panelHeight);
        drawSectionTitle(context, "VISUAL MODULES",
                "Cosmetic client-side effects. Changes are saved automatically.",
                panelX + 24, panelY + 25);

        String[] descriptions = {
                "Particle ring beneath you while airborne.",
                "Soft trailing vapor when you move.",
                "Small marker for the entity under your crosshair.",
                "Cosmetic burst when a nearby entity dies.",
                "Small sparks when you hit an entity.",
                "Decorative star particles around the sky.",
                "Enchanting sparkles around your held item.",
                "Orbiting 3D-style light around your player.",
                "Subtle light while wearing a chestplate."
        };
        for (int i = 0; i < descriptions.length; i++) {
            int x = startX + (i % 2) * (cardWidth + 16);
            int y = startY + (i / 2) * rowGap + 31;
            context.drawText(textRenderer, textRenderer.trimToWidth(descriptions[i], cardWidth - 12),
                    x + 6, y, 0xFF9DA5B4, false);
        }
        context.drawText(textRenderer, "RIGHT SHIFT  /  OPEN OR CLOSE IN GAME",
                panelX + 24, panelY + panelHeight - 14, 0xFF858D9B, false);
    }

    @Override
    public boolean shouldPause() {
        return client != null && client.world != null;
    }

    private static Text moduleState(String module) {
        return Text.literal((ClientConfig.isEnabled(module) ? "●  ON   /   " : "○  OFF  /   ") + module);
    }
}
