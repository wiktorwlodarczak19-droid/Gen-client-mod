package pl.genclient.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.text.Text;

public final class GenPauseMenuScreen extends GenScreen {
    public GenPauseMenuScreen() {
        super(Text.literal("GEN CLIENT PAUSE"), null);
    }

    @Override
    protected void init() {
        int x = width / 2 - 115;
        int y = height / 2 - 42;
        addDrawableChild(new GenButton(x, y, 230, 26,
                Text.literal("BACK TO GAME"), () -> client.setScreen(null)));
        addDrawableChild(new GenButton(x, y + 35, 110, 24,
                Text.literal("VISUALS"), () -> client.setScreen(new VisualsScreen(this))));
        addDrawableChild(new GenButton(x + 120, y + 35, 110, 24,
                Text.literal("STYLE"), () -> client.setScreen(new StyleScreen(this))));
        addDrawableChild(new GenButton(x, y + 68, 230, 24,
                Text.literal("OPTIONS"), () -> client.setScreen(new OptionsScreen(this, client.options))));
        addDrawableChild(new GenButton(x, y + 101, 230, 24,
                Text.literal("SAVE & QUIT TO TITLE"), () -> client.disconnect(new TitleScreen())));
    }

    @Override
    protected void renderBody(DrawContext context, int mouseX, int mouseY, float delta) {
        int x = width / 2 - 170;
        int y = height / 2 - 112;
        drawPanel(context, x, y, 340, 245);
        context.drawCenteredTextWithShadow(textRenderer, "GAME PAUSED", width / 2, y + 24, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, "Take a breath. Your world is waiting.", width / 2, y + 43, 0xFF9EA5B3);
        context.fill(x + 42, y + 68, x + 298, y + 69, 0x33FFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, "GEN CLIENT  /  SAKURA DARK", width / 2, y + 218, 0xFFF4BFD3);
    }

    @Override
    public boolean shouldPause() {
        return true;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
