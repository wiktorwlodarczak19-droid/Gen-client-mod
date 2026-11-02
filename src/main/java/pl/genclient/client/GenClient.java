package pl.genclient.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import pl.genclient.gui.GenScreen;
import pl.genclient.gui.VisualsScreen;

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
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            VisualEffects.tick(client);
            while (openVisualsKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new VisualsScreen(null));
                } else if (client.currentScreen instanceof GenScreen genScreen) {
                    genScreen.close();
                }
            }
        });
    }
}
