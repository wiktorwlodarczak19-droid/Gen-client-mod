package pl.genclient.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import pl.genclient.client.SpotifyService;
import pl.genclient.config.ClientConfig;

import java.util.List;

/**
 * Main Right-Shift control center for GEN CLIENT 0.3.0.
 * The interface is intentionally independent from Minecraft's vanilla pixel-style menus.
 */
public final class GenHubScreen extends GenScreen {
    private enum Section {
        MODS("MODS"), VISUAL("VISUAL"), GENLAND("GENLAND"), ADDONS("ADDONS"), PLUGS("PLUGS");

        final String label;
        Section(String label) { this.label = label; }
    }

    private static final List<String> VISUALS = List.of(
            "Jump Circle", "Glass Vapor", "Target ESP", "Kill Effect", "Hit Sparks",
            "Custom Sky", "Glint Hand", "Aura Glint Light 3D", "Armor Glass Light 3D"
    );
    private static final List<String> ADDONS = List.of("Kill Effect", "Hit Sparks");
    private final Section section;
    private final String selectedModule;
    private int panelX, panelY, panelWidth, panelHeight;
    private int listX, listY, rowWidth, rowGap;

    public GenHubScreen(Screen parent) {
        this(parent, Section.VISUAL, null);
    }

    private GenHubScreen(Screen parent, Section section, String selectedModule) {
        super(Text.literal("GEN CLIENT CONTROL CENTER"), parent);
        this.section = section;
        this.selectedModule = selectedModule;
    }

    @Override
    protected boolean showHeader() { return false; }

    @Override
    protected void init() {
        panelWidth = Math.min(760, width - 36);
        panelHeight = Math.min(500, height - 36);
        panelX = (width - panelWidth) / 2;
        panelY = (height - panelHeight) / 2;

        int navY = panelY + 58;
        int navGap = 7;
        int navWidth = (panelWidth - 48 - navGap * 4) / 5;
        Section[] sections = Section.values();
        for (int i = 0; i < sections.length; i++) {
            Section target = sections[i];
            int x = panelX + 24 + i * (navWidth + navGap);
            addDrawableChild(new GenButton(x, navY, navWidth, 27,
                    Text.literal((target == section ? "◆  " : "") + target.label),
                    () -> client.setScreen(new GenHubScreen(parent, target, null)),
                    target == section));
        }

        if (selectedModule != null) {
            int settingsY = panelY + 208;
            int settingsGap = 8;
            int settingsWidth = (panelWidth - 48 - settingsGap) / 2;
            if ("FPS Counter".equals(selectedModule)) {
                addDrawableChild(new GenButton(panelX + 24, panelY + panelHeight - 76, 206, 25,
                        Text.literal(ClientConfig.fpsCounterLabel ? "FORMAT: FPS: 100" : "FORMAT: 100 FPS"),
                        () -> {
                            ClientConfig.fpsCounterLabel = !ClientConfig.fpsCounterLabel;
                            ClientConfig.save();
                            client.setScreen(new GenHubScreen(parent, section, selectedModule));
                        }));
            }
            if ("Dynamic Island".equals(selectedModule)) {
                settingsY = panelY + 208;
                addDrawableChild(new GenButton(panelX + 24, settingsY, settingsWidth, 25,
                        Text.literal("ACCENT: " + ClientConfig.islandAccent),
                        () -> {
                            ClientConfig.cycleIslandAccent();
                            client.setScreen(new GenHubScreen(parent, section, selectedModule));
                        }));
                addDrawableChild(new GenButton(panelX + 24 + settingsWidth + settingsGap, settingsY,
                        settingsWidth, 25,
                        Text.literal(ClientConfig.islandVisualizerAnimated
                                ? "VISUALIZER: ANIMATED" : "VISUALIZER: STATIC"),
                        () -> {
                            ClientConfig.islandVisualizerAnimated = !ClientConfig.islandVisualizerAnimated;
                            ClientConfig.save();
                            client.setScreen(new GenHubScreen(parent, section, selectedModule));
                        }));
            }
            if ("Custom Sky".equals(selectedModule)) {
                addDrawableChild(new GenButton(panelX + 24, settingsY, settingsWidth, 27,
                        Text.literal("SKY TINT: " + ClientConfig.skyTint),
                        () -> { ClientConfig.cycleSkyTint(); refreshSettings(); }));
                addDrawableChild(new GenButton(panelX + 24 + settingsWidth + settingsGap, settingsY, settingsWidth, 27,
                        Text.literal("STRENGTH: " + ClientConfig.skyTintStrength + "%"),
                        () -> { ClientConfig.adjustSkyTintStrength(10); refreshSettings(); }));
                addDrawableChild(new GenButton(panelX + 24, settingsY + 35, settingsWidth, 27,
                        Text.literal("STRENGTH - 10%"),
                        () -> { ClientConfig.adjustSkyTintStrength(-10); refreshSettings(); }));
            } else if (VISUALS.contains(selectedModule) || "Sky Petals".equals(selectedModule)) {
                addDrawableChild(new GenButton(panelX + 24, settingsY, settingsWidth, 27,
                        Text.literal("COLOR: " + ClientConfig.effectColorName(selectedModule)),
                        () -> { ClientConfig.cycleEffectColor(selectedModule); refreshSettings(); }));
                addDrawableChild(new GenButton(panelX + 24 + settingsWidth + settingsGap, settingsY, settingsWidth, 27,
                        Text.literal("INTENSITY: " + ClientConfig.effectIntensity(selectedModule)),
                        () -> { ClientConfig.cycleEffectIntensity(selectedModule); refreshSettings(); }));
            }
            if ("Low FX Mode".equals(selectedModule)) {
                addDrawableChild(new GenButton(panelX + 24, settingsY, settingsWidth, 27,
                        Text.literal(ClientConfig.lowFxMode ? "LOW FX: ON" : "LOW FX: OFF"),
                        () -> { ClientConfig.lowFxMode = !ClientConfig.lowFxMode; ClientConfig.save(); refreshSettings(); }));
            }
            if ("Island Text".equals(selectedModule)) {
                TextFieldWidget field = new TextFieldWidget(textRenderer, panelX + 24, settingsY,
                        panelWidth - 48, 26, Text.literal("Island text"));
                field.setMaxLength(90);
                field.setText(ClientConfig.islandMessage);
                field.setPlaceholder(Text.literal("Wpisz własną linijkę tekstu…"));
                addDrawableChild(field);
                addDrawableChild(new GenButton(panelX + 24, settingsY + 35, settingsWidth, 27,
                        Text.literal("SAVE TEXT"), () -> {
                            ClientConfig.setIslandMessage(field.getText());
                            ClientConfig.islandMessageEnabled = !ClientConfig.islandMessage.isBlank();
                            ClientConfig.save();
                            refreshSettings();
                        }, true));
                addDrawableChild(new GenButton(panelX + 24 + settingsWidth + settingsGap, settingsY + 35, settingsWidth, 27,
                        Text.literal(ClientConfig.islandMessageEnabled ? "TEXT: ON" : "TEXT: OFF"),
                        () -> { ClientConfig.islandMessageEnabled = !ClientConfig.islandMessageEnabled; ClientConfig.save(); refreshSettings(); }));
            }
            if ("Spotify Player".equals(selectedModule)) {
                addDrawableChild(new GenButton(panelX + 24, settingsY, settingsWidth, 27,
                        Text.literal("OPEN SPOTIFY SETUP"), () -> client.setScreen(new SpotifyScreen(this)), true));
            }
            addDrawableChild(new GenButton(panelX + 24, panelY + panelHeight - 43, 112, 25,
                    Text.literal("BACK"), () -> client.setScreen(new GenHubScreen(parent, section, null))));
            addDrawableChild(new GenButton(panelX + panelWidth - 154, panelY + panelHeight - 43, 130, 25,
                    moduleState(selectedModule), () -> {
                        if ("Spotify Player".equals(selectedModule)) client.setScreen(new SpotifyScreen(this));
                        else { toggle(selectedModule); refreshSettings(); }
                    }));
            return;
        }

        listX = panelX + 24;
        listY = panelY + 112;
        rowWidth = panelWidth - 48;
        int gridGap = 10;
        int cardWidth = (rowWidth - gridGap) / 2;
        rowGap = 50;

        List<String> modules = modulesFor(section);
        for (int i = 0; i < modules.size(); i++) {
            String module = modules.get(i);
            int col = i % 2;
            int row = i / 2;
            int x = listX + col * (cardWidth + gridGap);
            int y = listY + row * rowGap;
            if (y + 42 > panelY + panelHeight - 55) break;
            addDrawableChild(new GenButton(x + cardWidth - 90, y + 7, 54, 27,
                    Text.literal(enabled(module) ? "ON" : "OFF"), () -> {
                        if ("Spotify Player".equals(module) && !SpotifyService.isConnected())
                            client.setScreen(new SpotifyScreen(this));
                        else { toggle(module); refreshList(); }
                    }));
            addDrawableChild(new GenButton(x + cardWidth - 31, y + 7, 27, 27,
                    Text.literal("⚙"), () -> {
                        if ("Spotify Player".equals(module)) client.setScreen(new SpotifyScreen(this));
                        else client.setScreen(new GenHubScreen(parent, section, module));
                    }));
        }
        addDrawableChild(new GenButton(panelX + panelWidth - 154, panelY + panelHeight - 43, 130, 25,
                Text.literal("CLOSE"), this::close));
    }

    private static List<String> modulesFor(Section section) {
        return switch (section) {
            case VISUAL -> VISUALS;
            case ADDONS -> List.of("Kill Effect", "Hit Sparks", "Companion Badges");
            case GENLAND -> List.of("Dynamic Island", "Sky Petals");
            case MODS -> List.of("FPS Counter", "Low FX Mode");
            case PLUGS -> List.of("Spotify Player", "Island Text");
        };
    }

    private static boolean isAvailableToggle(String module) {
        return VISUALS.contains(module) || ADDONS.contains(module);
    }

    private static boolean enabled(String module) {
        return switch (module) {
            case "Dynamic Island" -> ClientConfig.dynamicIsland;
            case "FPS Counter" -> ClientConfig.fpsCounter;
            case "Low FX Mode" -> ClientConfig.lowFxMode;
            case "Sky Petals" -> ClientConfig.sakuraPetals;
            case "Spotify Player" -> SpotifyService.isConnected();
            case "Island Text" -> ClientConfig.islandMessageEnabled;
            case "Companion Badges" -> ClientConfig.modBadges;
            default -> ClientConfig.isEnabled(module);
        };
    }

    private static void toggle(String module) {
        switch (module) {
            case "Dynamic Island" -> {
                ClientConfig.dynamicIsland = !ClientConfig.dynamicIsland;
                ClientConfig.save();
            }
            case "FPS Counter" -> {
                ClientConfig.fpsCounter = !ClientConfig.fpsCounter;
                ClientConfig.save();
            }
            case "Sky Petals" -> {
                ClientConfig.sakuraPetals = !ClientConfig.sakuraPetals;
                ClientConfig.save();
            }
            case "Low FX Mode" -> { ClientConfig.lowFxMode = !ClientConfig.lowFxMode; ClientConfig.save(); }
            case "Island Text" -> { ClientConfig.islandMessageEnabled = !ClientConfig.islandMessageEnabled; ClientConfig.save(); }
            case "Companion Badges" -> { ClientConfig.modBadges = !ClientConfig.modBadges; ClientConfig.save(); }
            case "Spotify Player" -> SpotifyService.disconnect();
            default -> ClientConfig.toggle(module);
        }
    }

    private static Text moduleState(String module) {
        return Text.literal((enabled(module) ? "●  ON" : "○  OFF"));
    }
    private void refreshSettings() {
        if (client != null) client.setScreen(new GenHubScreen(parent, section, selectedModule));
    }
    private void refreshList() {
        if (client != null) client.setScreen(new GenHubScreen(parent, section, null));
    }

    @Override
    protected void renderBody(DrawContext context, int mouseX, int mouseY, float delta) {
        drawPanel(context, panelX, panelY, panelWidth, panelHeight);
        context.drawTextWithShadow(textRenderer, "GEN CLIENT", panelX + 24, panelY + 18, 0xFFFFFFFF);
        context.drawText(textRenderer, "CONTROL CENTER  /  1.21.1 FABRIC",
                panelX + 24, panelY + 35, 0xFF9299A8, false);

        if (selectedModule != null) {
            drawSectionTitle(context, selectedModule.toUpperCase() + " SETTINGS",
                    "Module preferences are stored locally and survive restarts.",
                    panelX + 24, panelY + 112);
            String detail = switch (selectedModule) {
                case "Custom Sky" -> "Actual world sky tint with a selectable colour palette and blend strength.";
                case "Spotify Player" -> "Official Spotify OAuth PKCE: title, artist, playback progress and playback controls.";
                case "FPS Counter" -> "Displays the actual Minecraft frame rate beside the local clock. The value is never fabricated or increased.";
                case "Low FX Mode" -> "Reduces GEN CLIENT particle counts instead of inventing FPS.";
                case "Island Text" -> "Manual capsule text. It is not automatically synchronized with music.";
                case "Companion Badges" -> "Badges for detected Flashback and Mod Menu integrations.";
                case "Dynamic Island" -> "Clock and FPS at the left, rounded media capsule with current track metadata at the right.";
                case "Sky Petals" -> "Toggle the decorative sky particles.";
                default -> "Enable or disable this cosmetic client-side effect.";
            };
            context.drawText(textRenderer, textRenderer.trimToWidth(detail, panelWidth - 48),
                    panelX + 24, panelY + 151, 0xFFB8BFCC, false);
            context.drawText(textRenderer, "STATUS", panelX + 24, panelY + 195, 0xFF8E97A7, false);
            context.drawTextWithShadow(textRenderer, enabled(selectedModule) ? "ENABLED" : "DISABLED",
                    panelX + 24, panelY + 211, enabled(selectedModule) ? ClientConfig.accentColor() : 0xFF9AA1AD);
            return;
        }

        List<String> modules = modulesFor(section);
        int gridGap = 10;
        int cardWidth = (rowWidth - gridGap) / 2;
        int cardHeight = 42;
        for (int i = 0; i < modules.size(); i++) {
            String module = modules.get(i);
            int col = i % 2;
            int row = i / 2;
            int x = listX + col * (cardWidth + gridGap);
            int y = listY + row * rowGap;
            if (y + cardHeight > panelY + panelHeight - 55) break;

            UiDraw.roundedPanel(context, x, y, cardWidth, cardHeight, 9,
                    0xC90C1018, 0x604E5360);
            UiDraw.roundedFill(context, x + 1, y + 7, 2, 28, 1, ClientConfig.accentColor());
            UiDraw.roundedPanel(context, x + 9, y + 9, 24, 24, 7,
                    0xD91A1D29, 0x805B606D);
            String glyph = module.equals("FPS Counter") ? "F"
                    : module.equals("Spotify Player") ? "♪"
                    : module.substring(0, 1);
            context.drawCenteredTextWithShadow(textRenderer, glyph, x + 21, y + 17,
                    ClientConfig.accentColor());
            int labelWidth = Math.max(32, cardWidth - 150);
            context.drawTextWithShadow(textRenderer,
                    textRenderer.trimToWidth(module, labelWidth),
                    x + 42, y + 7, 0xFFF0F1F5);
            String status = enabled(module) ? "ENABLED" : "DISABLED";
            context.drawText(textRenderer, status, x + 42, y + 22,
                    enabled(module) ? 0xFFB9C7C2 : 0xFF858D9B, false);
        }

        String hint = switch (section) {
            case MODS -> "MODS  /  Prawdziwy licznik FPS i ograniczanie kosztu efektów.";
            case VISUAL -> "VISUAL  /  ON/OFF włącza efekt, zębatka otwiera osobne ustawienia.";
            case GENLAND -> "GENLAND  /  Personalizacja Dynamic Island i atmosfery.";
            case ADDONS -> "ADDONS  /  Dodatkowe efekty i odznaki.";
            case PLUGS -> "PLUGS  /  Spotify oraz własny tekst kapsuły.";
        };
        context.drawText(textRenderer, hint, panelX + 24, panelY + panelHeight - 27,
                0xFF858D9B, false);
    }

    @Override
    public boolean shouldPause() { return false; }
}
