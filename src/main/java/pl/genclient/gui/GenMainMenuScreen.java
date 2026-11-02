package pl.genclient.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class GenMainMenuScreen extends GenScreen {
    private static final Identifier LOGO_MARK = Identifier.of("genclient", "textures/gui/logo_mark.png");

    public GenMainMenuScreen() {
        super(Text.literal("GEN CLIENT"), null);
    }

    @Override
    protected void init() {
        int panelWidth = Math.min(690, width - 48);
        int panelX = (width - panelWidth) / 2;
        int panelY = Math.max(76, (height - 360) / 2);
        int buttonX = panelX + 42;
        int buttonWidth = 250;
        int buttonY = panelY + 174;

        addDrawableChild(new GenButton(buttonX, buttonY, buttonWidth, 28,
                Text.literal("SINGLEPLAYER"), () -> client.setScreen(new SelectWorldScreen(this))));
        addDrawableChild(new GenButton(buttonX, buttonY + 36, buttonWidth, 28,
                Text.literal("MULTIPLAYER"), () -> client.setScreen(new MultiplayerScreen(this))));
        addDrawableChild(new GenButton(buttonX, buttonY + 82, 120, 25,
                Text.literal("VISUALS"), () -> client.setScreen(new VisualsScreen(this))));
        addDrawableChild(new GenButton(buttonX + 130, buttonY + 82, 120, 25,
                Text.literal("STYLE"), () -> client.setScreen(new StyleScreen(this))));
        addDrawableChild(new GenButton(buttonX, buttonY + 117, 120, 24,
                Text.literal("OPTIONS"), () -> client.setScreen(new OptionsScreen(this, client.options))));
        addDrawableChild(new GenButton(buttonX + 130, buttonY + 117, 120, 24,
                Text.literal("QUIT"), () -> client.scheduleStop()));
    }

    @Override
    protected void renderBody(DrawContext context, int mouseX, int mouseY, float delta) {
        int panelWidth = Math.min(690, width - 48);
        int panelX = (width - panelWidth) / 2;
        int panelY = Math.max(76, (height - 360) / 2);
        drawPanel(context, panelX, panelY, panelWidth, 360);

        context.drawTextWithShadow(textRenderer, "WELCOME BACK", panelX + 42, panelY + 36, 0xFFF4BFD3);
        context.drawTextWithShadow(textRenderer, "GEN CLIENT", panelX + 42, panelY + 59, 0xFFFFFFFF);
        context.drawText(textRenderer, "A calmer, cleaner way to play.", panelX + 42, panelY + 80, 0xFFB2B7C3, false);
        context.fill(panelX + 42, panelY + 108, panelX + 292, panelY + 109, 0x44FFFFFF);
        context.drawText(textRenderer, "SAKURA DARK  /  1.21.1", panelX + 42, panelY + 132, 0xFF939BAA, false);

        int infoX = panelX + 340;
        context.drawTextWithShadow(textRenderer, "YOUR WORLD, YOUR STYLE", infoX, panelY + 50, 0xFFE8EAF0);
        context.drawText(textRenderer, "Custom interface", infoX, panelY + 78, 0xFFB4BAC6, false);
        context.drawText(textRenderer, "Visual effects", infoX, panelY + 96, 0xFFB4BAC6, false);
        context.drawText(textRenderer, "Personal color themes", infoX, panelY + 114, 0xFFB4BAC6, false);
        context.drawText(textRenderer, "Right Shift  /  Visual menu", infoX, panelY + 151, 0xFFF4BFD3, false);
        context.drawTexture(LOGO_MARK, panelX + panelWidth - 158, panelY + 178,
                112, 102, 0.0f, 0.0f, 530, 483, 530, 483);

        context.drawText(textRenderer, "GEN CLIENT IS A CLIENT-SIDE VISUAL MOD", panelX + 42,
                panelY + 330, 0xFF858D9B, false);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
