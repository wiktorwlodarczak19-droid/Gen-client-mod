package pl.genclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class GenClient implements ClientModInitializer {
    private KeyBinding openVisualsKey;

    @Override
    public void onInitializeClient() {
        openVisualsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.genclient.open_visuals", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT, "category.genclient"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openVisualsKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new VisualsScreen());
            }
        });
    }
}
