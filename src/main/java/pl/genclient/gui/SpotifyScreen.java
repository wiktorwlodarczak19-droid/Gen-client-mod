package pl.genclient.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import pl.genclient.client.SpotifyService;

/** Spotify account setup with a real mouse-wheel scroll area on small GUI scales. */
public final class SpotifyScreen extends GenScreen {
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private int viewportX;
    private int viewportY;
    private int viewportWidth;
    private int viewportBottom;
    private int scrollOffset;
    private int maxScroll;

    private TextFieldWidget clientIdField;
    private GenButton saveButton;
    private GenButton connectButton;
    private GenButton disconnectButton;
    private GenButton previousButton;
    private GenButton playPauseButton;
    private GenButton nextButton;

    private int baseFieldY;
    private int baseActionY;
    private int baseControlsY;

    public SpotifyScreen(Screen parent) {
        super(Text.literal("GEN CLIENT / SPOTIFY"), parent);
    }

    @Override
    protected void init() {
        panelWidth = Math.min(590, Math.max(220, width - 32));
        panelHeight = Math.min(360, Math.max(190, height - 64));
        panelX = (width - panelWidth) / 2;
        panelY = Math.max(28, (height - panelHeight) / 2);

        viewportX = panelX + 22;
        viewportY = panelY + 80;
        viewportWidth = panelWidth - 44;
        viewportBottom = panelY + panelHeight - 48;

        int contentX = panelX + 30;
        int contentWidth = panelWidth - 60;
        int third = Math.max(52, (contentWidth - 16) / 3);
        baseFieldY = panelY + 105;
        baseActionY = baseFieldY + 42;
        baseControlsY = baseActionY + 39;

        clientIdField = new TextFieldWidget(textRenderer, contentX, baseFieldY,
                contentWidth, 24, Text.literal("Spotify Client ID"));
        clientIdField.setMaxLength(128);
        clientIdField.setText(SpotifyService.getClientId());
        clientIdField.setPlaceholder(Text.literal("Wklej Spotify Client ID"));
        addDrawableChild(clientIdField);

        saveButton = new GenButton(contentX, baseActionY, third, 25,
                Text.literal("SAVE ID"), () -> SpotifyService.setClientId(clientIdField.getText()));
        connectButton = new GenButton(contentX + third + 8, baseActionY, third, 25,
                Text.literal("CONNECT"), () -> {
                    SpotifyService.setClientId(clientIdField.getText());
                    SpotifyService.connect();
                }, true);
        disconnectButton = new GenButton(contentX + (third + 8) * 2, baseActionY, third, 25,
                Text.literal("DISCONNECT"), SpotifyService::disconnect);
        addDrawableChild(saveButton);
        addDrawableChild(connectButton);
        addDrawableChild(disconnectButton);

        int controlWidth = Math.max(52, (contentWidth - 16) / 3);
        previousButton = new GenButton(contentX, baseControlsY, controlWidth, 27,
                Text.literal("PREVIOUS"), SpotifyService::previous);
        playPauseButton = new GenButton(contentX + controlWidth + 8, baseControlsY, controlWidth, 27,
                Text.literal("PLAY / PAUSE"), SpotifyService::togglePlayback, true);
        nextButton = new GenButton(contentX + (controlWidth + 8) * 2, baseControlsY, controlWidth, 27,
                Text.literal("NEXT"), SpotifyService::next);
        addDrawableChild(previousButton);
        addDrawableChild(playPauseButton);
        addDrawableChild(nextButton);

        updateScrollLimits();
        updateWidgetPositions();
    }

    private void updateScrollLimits() {
        int baseDetailsY = baseControlsY + 40;
        int contentBottom = baseDetailsY + 110;
        maxScroll = Math.max(0, contentBottom - (viewportBottom - 8));
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
    }

    private void updateWidgetPositions() {
        if (clientIdField == null) return;
        int fieldY = baseFieldY - scrollOffset;
        int actionY = baseActionY - scrollOffset;
        int controlsY = baseControlsY - scrollOffset;

        clientIdField.setY(fieldY);
        saveButton.setY(actionY);
        connectButton.setY(actionY);
        disconnectButton.setY(actionY);
        previousButton.setY(controlsY);
        playPauseButton.setY(controlsY);
        nextButton.setY(controlsY);

        clientIdField.active = intersectsViewport(fieldY, clientIdField.getHeight());
        saveButton.active = intersectsViewport(actionY, saveButton.getHeight());
        connectButton.active = intersectsViewport(actionY, connectButton.getHeight());
        disconnectButton.active = intersectsViewport(actionY, disconnectButton.getHeight());
        previousButton.active = intersectsViewport(controlsY, previousButton.getHeight());
        playPauseButton.active = intersectsViewport(controlsY, playPauseButton.getHeight());
        nextButton.active = intersectsViewport(controlsY, nextButton.getHeight());
    }

    private boolean intersectsViewport(int y, int widgetHeight) {
        return y + widgetHeight > viewportY && y < viewportBottom;
    }

    @Override
    protected void renderBody(DrawContext context, int mouseX, int mouseY, float delta) {
        drawPanel(context, panelX, panelY, panelWidth, panelHeight);
        drawSectionTitle(context, "SPOTIFY CONNECT",
                "Połącz konto i steruj odtwarzaniem bez wychodzenia z gry.",
                panelX + 30, panelY + 24);

        int backX = panelX + panelWidth - 112;
        int backY = panelY + panelHeight - 35;
        UiDraw.roundedPanel(context, backX, backY, 82, 24, 7, 0xD20C1018, 0x504E5360);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("BACK"),
                backX + 41, backY + 8, 0xFFE5E7ED);

        context.enableScissor(viewportX, viewportY, viewportX + viewportWidth, viewportBottom);
        int fieldY = baseFieldY - scrollOffset;
        int actionY = baseActionY - scrollOffset;
        int controlsY = baseControlsY - scrollOffset;
        int detailsY = controlsY + 40;

        context.drawText(textRenderer, "SPOTIFY CLIENT ID",
                panelX + 30, fieldY - 17, 0xFFF4BFD3, false);
        context.drawText(textRenderer,
                "Spotify Developer Dashboard → Create app → Settings → Client ID",
                panelX + 30, fieldY + 28, 0xFF858D9B, false);

        context.drawText(textRenderer, "PLAYBACK CONTROLS",
                panelX + 30, controlsY + 31, 0xFF9BA2B1, false);
        context.drawText(textRenderer, "REDIRECT URI",
                panelX + 30, detailsY, 0xFFF4BFD3, false);
        context.drawText(textRenderer, "http://127.0.0.1:8888",
                panelX + 30, detailsY + 14, 0xFFE5E7ED, false);
        context.drawText(textRenderer,
                "Dodaj ten adres w Spotify Developer → Settings → Redirect URIs i kliknij Save.",
                panelX + 30, detailsY + 29, 0xFF9BA2B1, false);

        SpotifyService.TrackSnapshot track = SpotifyService.getSnapshot();
        String nowPlaying = track.hasTrack()
                ? track.title() + "  —  " + track.artists()
                : SpotifyService.getStatus();
        context.drawText(textRenderer, textRenderer.trimToWidth(nowPlaying, panelWidth - 72),
                panelX + 30, detailsY + 51, 0xFFE6E8EE, false);
        context.drawText(textRenderer,
                textRenderer.trimToWidth(SpotifyService.getStatus(), panelWidth - 72),
                panelX + 30, detailsY + 65, 0xFF9BA2B1, false);
        if (track.hasTrack()) {
            context.drawText(textRenderer, track.progressLabel()
                            + (track.isPlaying() ? "  /  PLAYING" : "  /  PAUSED"),
                    panelX + 30, detailsY + 79, 0xFF9BA2B1, false);
        }
        context.disableScissor();

        // A compact scrollbar remains visible only when there is more content below.
        if (maxScroll > 0) {
            int trackX = panelX + panelWidth - 13;
            int trackY = viewportY + 2;
            int trackHeight = viewportBottom - viewportY - 4;
            int thumbHeight = Math.max(22, trackHeight * trackHeight / (trackHeight + maxScroll));
            int travel = trackHeight - thumbHeight;
            int thumbY = trackY + (maxScroll == 0 ? 0 : travel * scrollOffset / maxScroll);
            UiDraw.roundedFill(context, trackX, trackY, 3, trackHeight, 2, 0x554B4A59);
            UiDraw.roundedFill(context, trackX, thumbY, 3, thumbHeight, 2, 0xFFC4B5FD);
        }
    }

    @Override
    protected void renderWidgets(DrawContext context, int mouseX, int mouseY, float delta) {
        context.enableScissor(viewportX, viewportY, viewportX + viewportWidth, viewportBottom);
        super.renderWidgets(context, mouseX, mouseY, delta);
        context.disableScissor();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= viewportX && mouseX <= viewportX + viewportWidth
                && mouseY >= viewportY && mouseY <= viewportBottom && maxScroll > 0) {
            int next = Math.max(0, Math.min(maxScroll, scrollOffset - (int) Math.signum(verticalAmount) * 28));
            if (next != scrollOffset) {
                scrollOffset = next;
                updateWidgetPositions();
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int backX = panelX + panelWidth - 112;
        int backY = panelY + panelHeight - 35;
        if (button == 0 && mouseX >= backX && mouseX <= backX + 82
                && mouseY >= backY && mouseY <= backY + 24) {
            close();
            return true;
        }
        if (mouseX >= panelX && mouseX <= panelX + panelWidth
                && mouseY >= panelY && mouseY <= panelY + panelHeight
                && !(mouseX >= viewportX && mouseX <= viewportX + viewportWidth
                && mouseY >= viewportY && mouseY <= viewportBottom)) {
            return true;
        }
        if (mouseX >= viewportX && mouseX <= viewportX + viewportWidth
                && mouseY >= viewportY && mouseY <= viewportBottom) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }
}
