package pl.genclient.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import pl.genclient.config.ClientConfig;

/** Crisp custom button: opaque enough for readability, with a restrained Sakura accent line. */
public final class GenButton extends ClickableWidget {
    private final Runnable action;

    public GenButton(int x, int y, int width, int height, Text message, Runnable action) {
        super(x, y, width, height, message);
        this.action = action;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean hover = isHovered();
        int x = getX();
        int y = getY();
        int accent = ClientConfig.accentColor();

        UiDraw.roundedPanel(context, x, y, getWidth(), getHeight(), 7,
                hover ? 0xE51C202B : 0xD20C1018, hover ? 0xA0FFFFFF : 0x504E5360);
        UiDraw.roundedFill(context, x + 1, y + 1, getWidth() - 2, 1, 1,
                hover ? 0xAAFFFFFF : 0x44FFFFFF);
        UiDraw.roundedFill(context, x + 1, y + 5, 2, getHeight() - 10, 1, accent);
        if (isFocused()) context.drawBorder(x - 1, y - 1, getWidth() + 2, getHeight() + 2, accent);

        int color = active ? (hover ? 0xFFFFFFFF : 0xFFE5E7ED) : 0xFF777D89;
        MinecraftClient client = MinecraftClient.getInstance();
        context.drawCenteredTextWithShadow(client.textRenderer, getMessage(), x + getWidth() / 2,
                y + (getHeight() - 8) / 2, color);
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
