package pl.genclient.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import pl.genclient.config.ClientConfig;

import java.util.List;

public final class VisualsScreen extends GenScreen {
    private static final List<String> MODULES = List.of(
            "Jump Circle", "Glass Vapor", "Target ESP",
            "Hit Sparks", "Aura Glint", "Armor Glass Light"
    );

    public VisualsScreen(Screen parent) {
        super(Text.literal("GEN CLIENT VISUALS"), parent);
    }

    @Override
    protected void init() {
        int panelWidth = Math.min(690, width - 40);
        int panelX = (width - panelWidth) / 2;
        int panelY = Math.max(72, (height - 390) / 2);
        int gap = 16;
        int cardWidth = (panelWidth - 64 - gap) / 2;
        int startX = panelX + 24;
        int startY = panelY + 112;

        for (int i = 0; i < MODULES.size(); i++) {
            String module = MODULES.get(i);
            int column = i % 2;
            int row = i / 2;
            int x = startX + column * (cardWidth + gap);
            int y = startY + row * 66;
            addDrawableChild(new GenButton(x, y, cardWidth, 30, moduleState(module), () -> {
                ClientConfig.toggle(module);
                for (var child : children()) {
                    if (child instanceof GenButton button && button.getMessage().getString().endsWith(module)) {
                        button.setMessage(moduleState(module));
                        break;
                    }
                }
            }));
        }

        addDrawableChild(new GenButton(panelX + 24, panelY + 330, 180, 24,
                Text.literal("STYLE SETTINGS"), () -> client.setScreen(new StyleScreen(this))));
        addDrawableChild(new GenButton(panelX + panelWidth - 154, panelY + 330, 130, 24,
                Text.literal("CLOSE"), this::close));
    }

    @Override
    protected void renderBody(DrawContext context, int mouseX, int mouseY, float delta) {
        int panelWidth = Math.min(690, width - 40);
        int panelX = (width - panelWidth) / 2;
        int panelY = Math.max(72, (height - 390) / 2);
        drawPanel(context, panelX, panelY, panelWidth, 390);
        drawSectionTitle(context, "VISUAL MODULES", "Toggle client-side effects. Your choices are saved automatically.",
                panelX + 24, panelY + 25);

        String[] descriptions = {
                "Particle ring beneath you while airborne.",
                "Soft glass-like vapor when you move.",
                "Small clean marker for the entity under your crosshair.",
                "Cosmetic sparks when you swing at an entity.",
                "Orbiting enchantment glints around your player.",
                "Subtle light particles while wearing a chestplate."
        };
        int cardWidth = (panelWidth - 64 - 16) / 2;
        for (int i = 0; i < descriptions.length; i++) {
            int x = panelX + 24 + (i % 2) * (cardWidth + 16);
            int y = panelY + 145 + (i / 2) * 66;
            context.drawText(textRenderer, descriptions[i], x + 8, y, 0xFF9DA5B4, false);
        }
        context.drawText(textRenderer, "RIGHT SHIFT  /  OPEN OR CLOSE IN GAME", panelX + 24,
                panelY + 367, 0xFF858D9B, false);
    }

    @Override
    public boolean shouldPause() {
        return client != null && client.world != null;
    }

    private static Text moduleState(String module) {
        return Text.literal((ClientConfig.isEnabled(module) ? "●  ON   /   " : "○  OFF  /   ") + module);
    }
}
