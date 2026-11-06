package pl.genclient.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import pl.genclient.config.ClientConfig;
import pl.genclient.gui.GenMainMenuScreen;
import pl.genclient.gui.GenScreen;
import pl.genclient.gui.GenHubScreen;

public final class GenClient implements ClientModInitializer {
    public static final String MOD_ID = "genclient";
    private static KeyBinding openVisualsKey;

    @Override
    public void onInitializeClient() {
        openVisualsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.genclient.open_visuals",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.genclient"
        ));

        GenHud.register();
        VisualEffects.register();
        SpotifyService.initialize();
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            ClientConfig.save();
            SpotifyService.shutdown();
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            VisualEffects.tick(client);

            // Count only real, unpaused world time. The value is persisted once per minute.
            if (client.player != null && client.world != null && !client.isPaused()) {
                playtimeTicks++;
                if (playtimeTicks >= 20) {
                    playtimeTicks = 0;
                    ClientConfig.recordPlaySecond();
                }
            } else {
                playtimeTicks = 0;
            }

            while (openVisualsKey.wasPressed()) {
                if (client.currentScreen == null || client.currentScreen instanceof GenMainMenuScreen) {
                    client.setScreen(new GenHubScreen(client.currentScreen));
                } else if (client.currentScreen instanceof GenScreen screen) {
                    screen.close();
                }
            }
        });
    }

    private int playtimeTicks;
}
