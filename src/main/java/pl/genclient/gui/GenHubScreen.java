package pl.genclient.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
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
            if ("FPS Counter".equals(selectedModule)) {
                addDrawableChild(new GenButton(panelX + 24, panelY + panelHeight - 76, 206, 25,
                        Text.literal(ClientConfig.fpsCounterLabel ? "FORMAT: FPS: 100" : "FORMAT: 100 FPS"),
                        () -> {
                            ClientConfig.fpsCounterLabel = !ClientConfig.fpsCounterLabel;
                            ClientConfig.save();
                            client.setScreen(new GenHubScreen(parent, section, selectedModule));
                        }));
            }
            addDrawableChild(new GenButton(panelX + 24, panelY + panelHeight - 43, 112, 25,
                    Text.literal("BACK"), () -> client.setScreen(new GenHubScreen(parent, section, null))));
            addDrawableChild(new GenButton(panelX + panelWidth - 154, panelY + panelHeight - 43, 130, 25,
                    moduleState(selectedModule), () -> {
                        toggle(selectedModule);
                        client.setScreen(new GenHubScreen(parent, section, selectedModule));
                    }));
            return;
        }

        listX = panelX + 24;
        listY = panelY + 112;
        rowWidth = panelWidth - 48;
        rowGap = 43;

        List<String> modules = modulesFor(section);
        for (int i = 0; i < modules.size(); i++) {
            String module = modules.get(i);
            int y = listY + i * rowGap;
            if (y + 30 > panelY + panelHeight - 55) break;

            addDrawableChild(new GenButton(listX + rowWidth - 102, y, 74, 28,
                    Text.literal(enabled(module) ? "ON" : "OFF"), () -> {
                        toggle(module);
                        client.setScreen(new GenHubScreen(parent, section, null));
                    }));
            addDrawableChild(new GenButton(listX + rowWidth - 24, y, 24, 28,
                    Text.literal("⚙"), () -> client.setScreen(
                            new GenHubScreen(parent, section, module))));
        }
        addDrawableChild(new GenButton(panelX + panelWidth - 154, panelY + panelHeight - 43, 130, 25,
                Text.literal("CLOSE"), this::close));
    }

    private static List<String> modulesFor(Section section) {
        return switch (section) {
            case VISUAL -> VISUALS;
            case ADDONS -> ADDONS;
            case GENLAND -> List.of("Dynamic Island", "Sky Petals");
            case MODS -> List.of("FPS Counter");
            case PLUGS -> List.of();
        };
    }

    private static boolean isAvailableToggle(String module) {
        return VISUALS.contains(module) || ADDONS.contains(module);
    }

    private static boolean enabled(String module) {
        return switch (module) {
            case "Dynamic Island" -> ClientConfig.dynamicIsland;
            case "FPS Counter" -> ClientConfig.fpsCounter;
            case "Sky Petals" -> ClientConfig.sakuraPetals;
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
            default -> ClientConfig.toggle(module);
        }
    }

    private static Text moduleState(String module) {
        return Text.literal((enabled(module) ? "●  ON" : "○  OFF"));
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
                case "Custom Sky" -> "Sky rendering controls are being added here: sky tint, sun tint and celestial style.";
                case "Spotify Lyrics" -> "Spotify playback and synchronized lyrics require a local Spotify connection. This panel is the setup entry point.";
                case "FPS Counter" -> "Displays the actual Minecraft frame rate beside the local clock. The value is never fabricated or increased.";
                case "Dynamic Island" -> "Show or hide the top island with the local clock, real FPS and music information.";
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
        if (section == Section.PLUGS) {
            context.drawTextWithShadow(textRenderer, "PLUGINS & INTEGRATIONS",
                    listX, listY - 1, 0xFFFFFFFF);
            context.drawText(textRenderer, "Spotify Lyrics  /  planned connection",
                    listX, listY + 27, 0xFFB9C0CC, false);
            context.drawText(textRenderer, "Live song title, artist and synchronized lyrics will appear in Dynamic Island.",
                    listX, listY + 43, 0xFF8E97A7, false);
            return;
        }

        if (section == Section.MODS) {
            context.drawTextWithShadow(textRenderer, "CLIENT MODS",
                    listX, listY - 1, 0xFFFFFFFF);
            context.drawText(textRenderer, "FPS Counter is already shown by the island. More performance tools will be added only when they make measurable changes.",
                    listX, listY + 28, 0xFFB9C0CC, false);
            return;
        }

        for (int i = 0; i < modules.size(); i++) {
            String module = modules.get(i);
            int y = listY + i * rowGap;
            if (y + 30 > panelY + panelHeight - 55) break;
            UiDraw.roundedPanel(context, listX, y, rowWidth - 112, 30, 8,
                    0xC90C1018, 0x504E5360);
            UiDraw.roundedFill(context, listX + 1, y + 6, 2, 18, 1, ClientConfig.accentColor());
            UiDraw.roundedPanel(context, listX + 8, y + 4, 22, 22, 6, 0xD91A1D29, 0x805B606D);
            String glyph = module.equals("FPS Counter") ? "F" : module.substring(0, 1);
            context.drawCenteredTextWithShadow(textRenderer, glyph, listX + 19, y + 11, ClientConfig.accentColor());
            context.drawTextWithShadow(textRenderer, module, listX + 38, y + 5, 0xFFF0F1F5);
            context.drawText(textRenderer, section == Section.MODS
                            ? "LIVE CLIENT METRIC  /  ACTUAL VALUE"
                            : "COSMETIC MODULE  /  CONFIGURABLE",
                    listX + 38, y + 17, 0xFF858D9B, false);
        }

        context.drawText(textRenderer, "Choose ON/OFF to toggle a module, or ⚙ to open its settings.",
                panelX + 24, panelY + panelHeight - 27, 0xFF858D9B, false);
    }

    @Override
    public boolean shouldPause() { return false; }
}
