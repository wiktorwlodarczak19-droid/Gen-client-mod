package pl.genclient.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.genclient.gui.GenMainMenuScreen;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private void genclient$openCustomMainMenu(CallbackInfo ci) {
        MinecraftClient.getInstance().setScreen(new GenMainMenuScreen());
    }
}
