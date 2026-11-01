package pl.genclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class VisualsScreen extends Screen {
    private static final int PANEL = 0xE9161924, LIGHT = 0xFF242A3A;
    private static final int ACCENT = 0xFFB58CFF, MUTED = 0xFF9AA3B8;
    private int left, top;
    private final boolean[] enabled = {true, true, false, false, true};
    private final String[] modules = {"Jump Circle", "Glass Vapor", "Target ESP", "Kill Effect", "Armor Glass Light 3D"};
    private String tab = "Visuals";

    public VisualsScreen() { super(Text.literal("GEN CLIENT")); }
    @Override protected void init() { left = (width - 430) / 2; top = (height - 270) / 2; }

    @Override public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        ctx.fill(0, 0, width, height, 0x99080A10);
        ctx.fill(left, top, left + 430, top + 270, PANEL);
        ctx.fill(left, top, left + 430, top + 2, ACCENT);
        ctx.drawTextWithShadow(textRenderer, "GEN CLIENT", left + 22, top + 18, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "VISUAL CONTROL CENTER", left + 22, top + 34, MUTED);
        String[] tabs = {"Visuals", "HUD", "Style"};
        for (int i = 0; i < tabs.length; i++) {
            int x = left + 22 + i * 88;
            boolean selected = tab.equals(tabs[i]);
            ctx.fill(x, top + 62, x + 78, top + 84, selected ? ACCENT : LIGHT);
            ctx.drawCenteredTextWithShadow(textRenderer, tabs[i], x + 39, top + 69, selected ? 0xFF11131B : 0xFFE8EAF2);
        }
        ctx.drawTextWithShadow(textRenderer, tab.equals("Visuals") ? "VISUAL MODULES" : "COMING SOON", left + 22, top + 103, MUTED);
        if (tab.equals("Visuals")) {
            for (int i = 0; i < modules.length; i++) {
                int y = top + 124 + i * 23;
                ctx.fill(left + 22, y - 4, left + 408, y + 15, LIGHT);
                ctx.drawTextWithShadow(textRenderer, modules[i], left + 32, y, 0xFFE8EAF2);
                int color = enabled[i] ? 0xFF8DE3C1 : 0xFF737B8E;
                ctx.fill(left + 372, y - 1, left + 394, y + 10, color);
                ctx.drawTextWithShadow(textRenderer, enabled[i] ? "ON" : "OFF", left + 399, y, color);
            }
        } else ctx.drawTextWithShadow(textRenderer, "More customization is on the way.", left + 22, top + 132, 0xFFE8EAF2);
        ctx.drawTextWithShadow(textRenderer, "RIGHT SHIFT  -  CLOSE", left + 22, top + 244, MUTED);
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            String[] tabs = {"Visuals", "HUD", "Style"};
            for (int i = 0; i < tabs.length; i++) {
                int x = left + 22 + i * 88;
                if (mouseX >= x && mouseX <= x + 78 && mouseY >= top + 62 && mouseY <= top + 84) {
                    tab = tabs[i]; return true;
                }
            }
            if (tab.equals("Visuals")) {
                int index = (int) ((mouseY - (top + 120)) / 23);
                if (index >= 0 && index < enabled.length && mouseX >= left + 22 && mouseX <= left + 408) {
                    enabled[index] = !enabled[index]; return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override public boolean shouldPause() { return false; }
}
