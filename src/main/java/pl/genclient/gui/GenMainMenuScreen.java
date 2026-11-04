package pl.genclient.gui;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import pl.genclient.config.ClientConfig;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Launcher-inspired GEN CLIENT home screen:
 * narrow glass panel on the left, bottom Play/Mods/Style navigation,
 * centered brand and account skin preview, with a quiet monochrome mountain backdrop.
 */
public final class GenMainMenuScreen extends GenScreen {
    private enum Page { PLAY, MODS, STYLE }

    private static final Identifier LOGO_MARK = Identifier.of("genclient", "textures/gui/logo_mark.png");
    private static final Identifier LOGO_TEXT = Identifier.of("genclient", "textures/gui/logo_text.png");
    private static final Identifier MOUNTAIN_BACKGROUND = Identifier.of(
            "genclient", "textures/gui/wallpapers/main_menu_mountains.jpg");
    private static final String SKIN_URL = "https://www.minecraft.net/msaprofile/mygames/editskin";

    private final Page page;
    private int scale;
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private int modsScroll;
    private List<ModContainer> mods = List.of();

    public GenMainMenuScreen() {
        this("PLAY");
    }

    public GenMainMenuScreen(String initialPage) {
        super(Text.literal("GEN CLIENT"), null);
        this.page = switch (initialPage == null ? "PLAY" : initialPage.toUpperCase()) {
            case "MODS" -> Page.MODS;
            case "STYLE" -> Page.STYLE;
            default -> Page.PLAY;
        };
    }

    @Override
    protected boolean showHeader() {
        return false;
    }

    private int u(int value) {
        return Math.max(1, Math.round(value * (scale / 100.0f)));
    }

    @Override
    protected void init() {
        scale = Math.max(44, Math.min(115, Math.round(height * 100.0f / 701.0f)));
        panelX = u(25);
        panelY = u(57);
        panelWidth = Math.min(u(360), width - u(50));
        panelHeight = Math.min(u(598), height - u(94));
        if (panelWidth < u(210)) panelWidth = Math.max(160, width - u(32));
        if (panelHeight < u(220)) panelHeight = Math.max(190, height - u(70));

        if (page == Page.PLAY) {
            int inset = u(20);
            int buttonX = panelX + inset;
            int buttonWidth = panelWidth - inset * 2;
            addDrawableChild(new GenButton(buttonX, panelY + u(222), buttonWidth, u(42),
                    Text.literal("PLAY"), () -> client.setScreen(new SelectWorldScreen(this)), true));
            addDrawableChild(new GenButton(buttonX, panelY + u(310), (buttonWidth - u(8)) / 2, u(26),
                    Text.literal("MULTIPLAYER"), () -> client.setScreen(new MultiplayerScreen(this))));
            addDrawableChild(new GenButton(buttonX + (buttonWidth + u(8)) / 2, panelY + u(310),
                    (buttonWidth - u(8)) / 2, u(26),
                    Text.literal("OPTIONS"), () -> client.setScreen(new OptionsScreen(this, client.options))));
        } else if (page == Page.STYLE) {
            int inset = u(20);
            int buttonX = panelX + inset;
            int buttonWidth = panelWidth - inset * 2;
            int firstY = panelY + u(154);
            int rowGap = u(48);
            addDrawableChild(new GenButton(buttonX, firstY, buttonWidth, u(34),
                    accentLabel(), () -> {
                ClientConfig.cycleAccent();
                client.setScreen(new GenMainMenuScreen("STYLE"));
            }));
            addDrawableChild(new GenButton(buttonX, firstY + rowGap, buttonWidth, u(34),
                    wallpaperLabel(), () -> {
                ClientConfig.cycleWallpaper();
                client.setScreen(new GenMainMenuScreen("STYLE"));
            }));
            addDrawableChild(new GenButton(buttonX, firstY + rowGap * 2, buttonWidth, u(34),
                    toggleLabel("SAKURA PETALS", ClientConfig.sakuraPetals), () -> {
                ClientConfig.sakuraPetals = !ClientConfig.sakuraPetals;
                ClientConfig.save();
                client.setScreen(new GenMainMenuScreen("STYLE"));
            }));
            addDrawableChild(new GenButton(buttonX, firstY + rowGap * 3, buttonWidth, u(34),
                    toggleLabel("DYNAMIC ISLAND", ClientConfig.dynamicIsland), () -> {
                ClientConfig.dynamicIsland = !ClientConfig.dynamicIsland;
                ClientConfig.save();
                client.setScreen(new GenMainMenuScreen("STYLE"));
            }));
            addDrawableChild(new GenButton(buttonX, firstY + rowGap * 4, buttonWidth, u(34),
                    toggleLabel("MOD BADGES", ClientConfig.modBadges), () -> {
                ClientConfig.modBadges = !ClientConfig.modBadges;
                ClientConfig.save();
                client.setScreen(new GenMainMenuScreen("STYLE"));
            }));
        } else {
            mods = new ArrayList<>(FabricLoader.getInstance().getAllMods());
            mods.sort(Comparator.comparing(m -> m.getMetadata().getName().toLowerCase()));
        }

        int centerX = width / 2;
        addDrawableChild(new GenButton(centerX - u(72), u(563), u(144), u(30),
                Text.literal("CHOOSE SKIN"), () -> Util.getOperatingSystem().open(SKIN_URL)));
        addDrawableChild(new GenButton(centerX - u(72), u(624), u(144), u(25),
                Text.literal("QUIT GAME"), () -> client.scheduleStop()));
    }

    @Override
    protected void drawBackdrop(DrawContext context) {
        // Photographic monochrome mountain artwork from the supplied launcher reference.
        context.drawTexture(MOUNTAIN_BACKGROUND, 0, 0, width, height,
                0.0f, 0.0f, 640, 360, 640, 360);
        context.fillGradient(0, 0, width, height, 0x12000000, 0x5005060A);

        if (ClientConfig.wallpaper.equals("midnight_ink")) {
            context.fill(0, 0, width, height, 0x241B2C42);
        } else if (ClientConfig.wallpaper.equals("petal_mist")) {
            context.fill(0, 0, width, height, 0x281B1123);
        }
        context.fill(0, 0, width, 1, 0x28FFFFFF);
    }

    @Override
    protected void renderBody(DrawContext context, int mouseX, int mouseY, float delta) {
        drawWindowDots(context);
        drawLeftPanel(context);
        drawNavigation(context);
        drawBrandAndSkin(context, mouseX, mouseY);
    }

    private void drawWindowDots(DrawContext context) {
        int y = u(18);
        UiDraw.roundedFill(context, u(29), y, u(9), u(9), u(5), 0xFFFF5F57);
        UiDraw.roundedFill(context, u(46), y, u(9), u(9), u(5), 0xFFFFBD2E);
        UiDraw.roundedFill(context, u(63), y, u(9), u(9), u(5), 0xFF28C840);
        context.fill(u(532), u(4), Math.max(u(533), width - u(530)), u(5), 0x22FFFFFF);
    }

    private void drawLeftPanel(DrawContext context) {
        roundedFill(context, panelX - u(2), panelY - u(2), panelWidth + u(4), panelHeight + u(4),
                u(22), 0x28000000);
        roundedPanel(context, panelX, panelY, panelWidth, panelHeight, u(22),
                0xD91B1C1E, 0x514F5054);
        roundedFill(context, panelX + u(1), panelY + u(1), panelWidth - u(2), u(1),
                u(1), 0x42FFFFFF);

        if (page == Page.PLAY) drawPlayPanel(context);
        else if (page == Page.MODS) drawModsPanel(context);
        else drawStylePanel(context);
    }

    private void drawPlayPanel(DrawContext context) {
        int x = panelX + u(20);
        int right = panelX + panelWidth - u(20);
        context.drawTextWithShadow(textRenderer, "Play", x, panelY + u(23), 0xFFF7F7F8);
        context.drawText(textRenderer, "Pick a world and start playing.", x, panelY + u(54), 0xFF9DA1A8, false);

        context.drawText(textRenderer, "Minecraft version", x, panelY + u(83), 0xFFE3E4E7, false);
        roundedPanel(context, x, panelY + u(99), right - x, u(30), u(8), 0xD30D0E10, 0x3EFFFFFF);
        context.drawText(textRenderer, "1.21.1   /   FABRIC", x + u(9), panelY + u(110), 0xFFF2F2F3, false);

        context.drawText(textRenderer, "Nickname", x, panelY + u(145), 0xFFE3E4E7, false);
        roundedPanel(context, x, panelY + u(161), right - x, u(30), u(8), 0xD30D0E10, 0x3EFFFFFF);
        String username = client == null ? "Player" : client.getSession().getUsername();
        context.drawText(textRenderer, trim(username, right - x - u(18)),
                x + u(9), panelY + u(172), 0xFFF2F2F3, false);

        roundedFill(context, x, panelY + u(271), right - x, u(14), u(4), 0x553D3F42);
        context.drawText(textRenderer, "READY", x, panelY + u(293), 0xFF9DA1A8, false);

        int statsY = panelY + panelHeight - u(132);
        context.drawText(textRenderer, "Time played", x, statsY, 0xFFE4E5E8, false);
        context.drawTextWithShadow(textRenderer, ClientConfig.weeklyPlaytimeLabel(),
                x, statsY + u(28), 0xFFF3F3F4);
        drawWeeklyChart(context, x, statsY + u(57), right - x, u(60));
    }

    private void drawWeeklyChart(DrawContext context, int x, int y, int chartWidth, int chartHeight) {
        LocalDate today = LocalDate.now();
        long max = 1L;
        long[] values = new long[7];
        for (int i = 0; i < 7; i++) {
            LocalDate day = today.minusDays(6L - i);
            values[i] = ClientConfig.playtimeFor(day);
            max = Math.max(max, values[i]);
        }
        int base = y + chartHeight - u(13);
        int slot = Math.max(1, chartWidth / 7);
        String[] dayNames = {"M", "T", "W", "T", "F", "S", "S"};
        for (int i = 0; i < 7; i++) {
            int barHeight = values[i] == 0 ? u(2) : Math.max(u(5), (int) ((chartHeight - u(20)) * values[i] / max));
            int barWidth = Math.max(u(5), slot / 2);
            int bx = x + i * slot + (slot - barWidth) / 2;
            roundedFill(context, bx, base - barHeight, barWidth, barHeight, u(3),
                    values[i] == 0 ? 0x338D929B : ClientConfig.accentColor());
            int dayIndex = dayNamesIndex(today.minusDays(6L - i).getDayOfWeek());
            context.drawCenteredTextWithShadow(textRenderer, dayNames[dayIndex],
                    x + i * slot + slot / 2, base + u(3), 0xFF858A93);
        }
    }

    private static int dayNamesIndex(DayOfWeek day) {
        return day.getValue() - 1;
    }

    private void drawModsPanel(DrawContext context) {
        int x = panelX + u(20);
        int right = panelX + panelWidth - u(20);
        context.drawTextWithShadow(textRenderer, "Mods", x, panelY + u(23), 0xFFF7F7F8);
        context.drawText(textRenderer, "Installed in this Minecraft profile.", x,
                panelY + u(54), 0xFF9DA1A8, false);
        context.drawText(textRenderer, mods.size() + " mods detected", x,
                panelY + u(83), ClientConfig.accentColor(), false);

        int rowTop = panelY + u(112);
        int rowHeight = u(55);
        int visible = Math.max(1, (panelHeight - u(152)) / Math.max(1, rowHeight));
        if (mods.isEmpty()) {
            context.drawText(textRenderer, "No Fabric mods were detected.", x, rowTop, 0xFFE5E6E9, false);
            return;
        }
        for (int i = 0; i < visible; i++) {
            int index = modsScroll + i;
            if (index >= mods.size()) break;
            ModContainer mod = mods.get(index);
            int y = rowTop + i * rowHeight;
            roundedPanel(context, x, y, right - x, u(45), u(8), 0x9F101216, 0x25FFFFFF);
            String name = mod.getMetadata().getName();
            String id = mod.getMetadata().getId();
            String version = mod.getMetadata().getVersion().getFriendlyString();
            context.drawTextWithShadow(textRenderer, trim(name, right - x - u(14)),
                    x + u(8), y + u(8), 0xFFEDEEF0);
            context.drawText(textRenderer, trim(id + "  /  " + version, right - x - u(14)),
                    x + u(8), y + u(25), 0xFF9298A2, false);
        }
        if (mods.size() > visible) {
            String hint = "SCROLL  /  " + (modsScroll + 1) + "-" + Math.min(modsScroll + visible, mods.size())
                    + " OF " + mods.size();
            context.drawText(textRenderer, hint, x, panelY + panelHeight - u(28), 0xFF858A93, false);
        }
    }

    private void drawStylePanel(DrawContext context) {
        int x = panelX + u(20);
        context.drawTextWithShadow(textRenderer, "Style", x, panelY + u(23), 0xFFF7F7F8);
        context.drawText(textRenderer, "Personalize your GEN CLIENT look.", x,
                panelY + u(54), 0xFF9DA1A8, false);
        context.drawText(textRenderer, "Accent: " + ClientConfig.accent, x, panelY + u(113),
                ClientConfig.accentColor(), false);
        context.drawText(textRenderer, "Wallpaper: " + ClientConfig.wallpaper.replace('_', ' ').toUpperCase(),
                x, panelY + u(161), 0xFFE2E3E6, false);
        context.drawText(textRenderer, "Atmosphere", x, panelY + u(209), 0xFF9DA1A8, false);
        context.drawText(textRenderer, "HUD", x, panelY + u(257), 0xFF9DA1A8, false);
        context.drawText(textRenderer, "Integration", x, panelY + u(305), 0xFF9DA1A8, false);
        context.drawText(textRenderer, "Saved to config/genclient.json", x,
                panelY + panelHeight - u(25), 0xFF858A93, false);
    }

    private void drawNavigation(DrawContext context) {
        int navY = height - u(19);
        int navX = panelX + u(68);
        int tabWidth = u(75);
        int gap = u(6);
        String[] tabs = {"PLAY", "MODS", "STYLE"};
        String active = page.name();
        context.fill(navX, navY - u(9), navX + tabWidth * 3 + gap * 2, navY - u(7), 0x44575B63);
        for (int i = 0; i < tabs.length; i++) {
            int tx = navX + i * (tabWidth + gap);
            boolean selected = tabs[i].equals(active);
            if (selected) {
                roundedFill(context, tx, navY - u(9), tabWidth, u(3), u(2), ClientConfig.accentColor());
            }
            context.drawCenteredTextWithShadow(textRenderer, tabs[i],
                    tx + tabWidth / 2, navY, selected ? 0xFFF0F0F2 : 0xFF747981);
        }
    }

    private void drawBrandAndSkin(DrawContext context, int mouseX, int mouseY) {
        int centerX = width / 2;
        int logoWidth = u(132);
        int logoHeight = u(120);
        context.drawTexture(LOGO_MARK, centerX - logoWidth / 2, u(92), logoWidth, logoHeight,
                0.0f, 0.0f, 530, 483, 530, 483);
        context.drawTexture(LOGO_TEXT, centerX - u(87), u(224), u(174), u(17),
                0.0f, 0.0f, 687, 65, 687, 65);

        int avatarTop = u(292);
        drawAccountSkin(context, centerX, avatarTop);
        String username = client == null ? "Player" : client.getSession().getUsername();
        context.drawCenteredTextWithShadow(textRenderer, username,
                centerX, avatarTop + u(211), 0xD8F0F0F2);
    }

    private void drawAccountSkin(DrawContext context, int centerX, int top) {
        Identifier skin;
        try {
            SkinTextures textures = client.getSkinProvider().getSkinTextures(client.getGameProfile());
            skin = textures.texture();
        } catch (Exception ignored) {
            skin = DefaultSkinHelper.getSkinTextures(client.getGameProfile()).texture();
        }

        int pixel = u(7);
        int head = pixel * 8;
        int bodyWidth = pixel * 8;
        int armWidth = pixel * 4;
        int bodyHeight = pixel * 12;
        int headX = centerX - head / 2;
        int bodyX = centerX - bodyWidth / 2;
        int bodyY = top + head;
        int armY = bodyY;
        int legY = bodyY + bodyHeight;
        int leftArmX = bodyX - armWidth;
        int rightArmX = bodyX + bodyWidth;
        int leftLegX = centerX - pixel * 4;
        int rightLegX = centerX;

        roundedFill(context, centerX - u(48), legY + bodyHeight + u(3), u(96), u(9),
                u(7), 0x50000000);
        roundedFill(context, headX + u(5), top + u(4), head - u(3), head, u(5), 0x25000000);

        // Slightly shaded side faces give the pixel skin a block-like, 3D silhouette.
        context.fill(headX + head - u(3), top + u(3), headX + head + u(2), top + head - u(2), 0x55333333);
        context.fill(bodyX + bodyWidth - u(3), bodyY, bodyX + bodyWidth + u(2),
                bodyY + bodyHeight, 0x55303030);
        context.fill(leftArmX + armWidth - u(2), armY, leftArmX + armWidth + u(2),
                armY + bodyHeight, 0x55303030);
        context.fill(rightArmX + armWidth - u(2), armY, rightArmX + armWidth + u(2),
                armY + bodyHeight, 0x55303030);

        // Head, outer hat layer, torso, jacket, arms and legs from the live account skin atlas.
        drawSkinPart(context, skin, headX, top, head, head, 8, 8, 8, 8);
        drawSkinPart(context, skin, headX, top, head, head, 40, 8, 8, 8);
        drawSkinPart(context, skin, bodyX, bodyY, bodyWidth, bodyHeight, 20, 20, 8, 12);
        drawSkinPart(context, skin, bodyX, bodyY, bodyWidth, bodyHeight, 20, 36, 8, 12);

        drawSkinPart(context, skin, leftArmX, armY, armWidth, bodyHeight, 36, 52, 4, 12);
        drawSkinPart(context, skin, leftArmX, armY, armWidth, bodyHeight, 52, 52, 4, 12);
        drawSkinPart(context, skin, rightArmX, armY, armWidth, bodyHeight, 44, 20, 4, 12);
        drawSkinPart(context, skin, rightArmX, armY, armWidth, bodyHeight, 44, 36, 4, 12);

        drawSkinPart(context, skin, leftLegX, legY, armWidth, bodyHeight, 20, 52, 4, 12);
        drawSkinPart(context, skin, leftLegX, legY, armWidth, bodyHeight, 4, 52, 4, 12);
        drawSkinPart(context, skin, rightLegX, legY, armWidth, bodyHeight, 4, 20, 4, 12);
        drawSkinPart(context, skin, rightLegX, legY, armWidth, bodyHeight, 4, 36, 4, 12);

        // Fine silhouette outline, kept subtle so the skin remains the focus.
        context.fill(headX - 1, top, headX, top + head, 0x3AFFFFFF);
        context.fill(bodyX - 1, bodyY, bodyX, bodyY + bodyHeight, 0x30FFFFFF);
    }

    private void drawSkinPart(DrawContext context, Identifier skin, int x, int y,
                              int width, int height, int u, int v, int sourceWidth, int sourceHeight) {
        context.drawTexture(skin, x, y, width, height, u, v,
                sourceWidth, sourceHeight, 64, 64);
    }

    private void drawNavigationHitboxes(DrawContext context) {
        // Kept separate from render drawing so hitboxes share the exact same scaled geometry.
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int navY = height - u(19) - u(8);
            int navX = panelX + u(68);
            int tabWidth = u(75);
            int gap = u(6);
            if (mouseY >= navY && mouseY <= navY + u(24)) {
                String[] tabs = {"PLAY", "MODS", "STYLE"};
                for (int i = 0; i < tabs.length; i++) {
                    int x = navX + i * (tabWidth + gap);
                    if (mouseX >= x && mouseX <= x + tabWidth) {
                        client.setScreen(new GenMainMenuScreen(tabs[i]));
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (page == Page.MODS && mouseX >= panelX && mouseX <= panelX + panelWidth
                && mouseY >= panelY && mouseY <= panelY + panelHeight) {
            int rowHeight = u(55);
            int visible = Math.max(1, (panelHeight - u(152)) / Math.max(1, rowHeight));
            int maxScroll = Math.max(0, mods.size() - visible);
            modsScroll = Math.max(0, Math.min(maxScroll, modsScroll - (int) Math.signum(verticalAmount)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private String trim(String value, int maxWidth) {
        if (value == null) return "";
        return textRenderer.trimToWidth(value, Math.max(0, maxWidth));
    }

    private static Text accentLabel() {
        return Text.literal("ACCENT  /  " + ClientConfig.accent);
    }

    private static Text wallpaperLabel() {
        return Text.literal("WALLPAPER  /  " + ClientConfig.wallpaper.replace('_', ' ').toUpperCase());
    }

    private static Text toggleLabel(String name, boolean enabled) {
        return Text.literal((enabled ? "●  ON   /   " : "○  OFF  /   ") + name);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
