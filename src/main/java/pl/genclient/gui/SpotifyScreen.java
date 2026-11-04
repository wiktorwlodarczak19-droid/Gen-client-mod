package pl.genclient.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import pl.genclient.client.SpotifyService;

/** Spotify account setup and playback controls. */
public final class SpotifyScreen extends GenScreen {
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private TextFieldWidget clientIdField;

    public SpotifyScreen(Screen parent) {
        super(Text.literal("GEN CLIENT / SPOTIFY"), parent);
    }

    @Override
    protected void init() {
        panelWidth = Math.min(590, width - 40);
        panelHeight = Math.min(344, height - 88);
        panelHeight = Math.max(286, panelHeight);
        panelX = (width - panelWidth) / 2;
        panelY = Math.max(52, (height - panelHeight) / 2);

        int contentX = panelX + 30;
        int contentWidth = panelWidth - 60;
        int fieldY = panelY + 103;

        clientIdField = new TextFieldWidget(textRenderer, contentX, fieldY, contentWidth, 24,
                Text.literal("Spotify Client ID"));
        clientIdField.setMaxLength(128);
        clientIdField.setText(SpotifyService.getClientId());
        clientIdField.setPlaceholder(Text.literal("Wklej Client ID aplikacji Spotify"));
        addDrawableChild(clientIdField);

        int actionY = fieldY + 36;
        int third = (contentWidth - 16) / 3;
        addDrawableChild(new GenButton(contentX, actionY, third, 25,
                Text.literal("SAVE CLIENT ID"), () -> SpotifyService.setClientId(clientIdField.getText())));
        addDrawableChild(new GenButton(contentX + third + 8, actionY, third, 25,
                Text.literal("CONNECT SPOTIFY"), () -> {
            SpotifyService.setClientId(clientIdField.getText());
            SpotifyService.connect();
        }, true));
        addDrawableChild(new GenButton(contentX + (third + 8) * 2, actionY, third, 25,
                Text.literal("DISCONNECT"), SpotifyService::disconnect));

        int controlsY = actionY + 39;
        int controlWidth = (contentWidth - 16) / 3;
        addDrawableChild(new GenButton(contentX, controlsY, controlWidth, 27,
                Text.literal("PREVIOUS"), SpotifyService::previous));
        addDrawableChild(new GenButton(contentX + controlWidth + 8, controlsY, controlWidth, 27,
                Text.literal("PLAY  /  PAUSE"), SpotifyService::togglePlayback, true));
        addDrawableChild(new GenButton(contentX + (controlWidth + 8) * 2, controlsY, controlWidth, 27,
                Text.literal("NEXT"), SpotifyService::next));

        addDrawableChild(new GenButton(panelX + panelWidth - 112, panelY + panelHeight - 38,
                82, 24, Text.literal("BACK"), this::close));
    }

    @Override
    protected void renderBody(DrawContext context, int mouseX, int mouseY, float delta) {
        drawPanel(context, panelX, panelY, panelWidth, panelHeight);
        drawSectionTitle(context, "SPOTIFY CONNECT",
                "Połącz konto i steruj aktywnym urządzeniem bez wychodzenia z gry.",
                panelX + 30, panelY + 25);

        context.drawText(textRenderer, "SPOTIFY CLIENT ID", panelX + 30, panelY + 83,
                0xFF9BA2B1, false);
        context.drawText(textRenderer,
                "Dashboard → Create app → Settings → Client ID",
                panelX + 30, panelY + 137, 0xFF858D9B, false);

        int detailsY = panelY + 217;
        context.drawText(textRenderer, "REDIRECT URI", panelX + 30, detailsY, 0xFFF4BFD3, false);
        context.drawText(textRenderer, "http://127.0.0.1",
                panelX + 30, detailsY + 13, 0xFFE5E7ED, false);
        context.drawText(textRenderer,
                "Wpisz ten adres w ustawieniach aplikacji Spotify Developer.",
                panelX + 30, detailsY + 28, 0xFF9BA2B1, false);

        SpotifyService.TrackSnapshot track = SpotifyService.getSnapshot();
        String nowPlaying = track.hasTrack()
                ? track.title() + "  —  " + track.artists()
                : SpotifyService.getStatus();
        context.drawText(textRenderer, textRenderer.trimToWidth(nowPlaying, panelWidth - 60),
                panelX + 30, panelY + panelHeight - 57, 0xFFE6E8EE, false);
        context.drawText(textRenderer, textRenderer.trimToWidth(SpotifyService.getStatus(), panelWidth - 60),
                panelX + 30, panelY + panelHeight - 25, 0xFF9BA2B1, false);
        if (track.hasTrack()) {
            context.drawText(textRenderer, track.progressLabel()
                            + (track.isPlaying() ? "  /  PLAYING" : "  /  PAUSED"),
                    panelX + 30, panelY + panelHeight - 42, 0xFF9BA2B1, false);
        }
    }
}
