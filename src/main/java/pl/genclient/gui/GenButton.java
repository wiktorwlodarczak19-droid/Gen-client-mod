package pl.genclient.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import pl.genclient.config.ClientConfig;

/** Crisp custom control with a restrained Sakura accent and an optional pink primary style. */
public final class GenButton extends ClickableWidget {
    private final Runnable action;
    private final boolean primary;

    public GenButton(int x, int y, int width, int height, Text message, Runnable action) {
        this(x, y, width, height, message, action, false);
    }

    public GenButton(int x, int y, int width, int height, Text message, Runnable action, boolean primary) {
        super(x, y, width, height, message);
        this.action = action;
        this.primary = primary;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean hover = isHovered();
        int x = getX();
        int y = getY();
        int accent = ClientConfig.accentColor();

        if (primary) {
            int fill = hover ? brighten(accent, 14) : accent;
            UiDraw.roundedPanel(context, x, y, getWidth(), getHeight(), 8,
                    fill, hover ? 0xDFFFFFFF : 0x90FFFFFF);
            UiDraw.roundedFill(context, x + 3, y + 2, getWidth() - 6, 1, 1, 0x66FFFFFF);
        } else {
            UiDraw.roundedPanel(context, x, y, getWidth(), getHeight(), 7,
                    hover ? 0xE51C202B : 0xD20C1018,
                    hover ? 0xA0FFFFFF : 0x504E5360);
            UiDraw.roundedFill(context, x + 1, y + 1, getWidth() - 2, 1, 1,
                    hover ? 0xAAFFFFFF : 0x44FFFFFF);
            UiDraw.roundedFill(context, x + 1, y + 5, 2, getHeight() - 10, 1, accent);
        }

        if (isFocused()) context.drawBorder(x - 1, y - 1, getWidth() + 2, getHeight() + 2, accent);
        int color = !active ? 0xFF777D89
                : primary ? 0xFF171116
                : (hover ? 0xFFFFFFFF : 0xFFE5E7ED);
        MinecraftClient client = MinecraftClient.getInstance();
        context.drawCenteredTextWithShadow(client.textRenderer, getMessage(),
                x + getWidth() / 2, y + (getHeight() - 8) / 2, color);
    }

    private static int brighten(int color, int amount) {
        int a = color >>> 24;
        int r = Math.min(255, ((color >>> 16) & 255) + amount);
        int g = Math.min(255, ((color >>> 8) & 255) + amount);
        int b = Math.min(255, (color & 255) + amount);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (active) action.run();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        appendDefaultNarrations(builder);
    }
}
