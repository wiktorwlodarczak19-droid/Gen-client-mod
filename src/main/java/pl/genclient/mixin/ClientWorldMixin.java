package pl.genclient.mixin;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.genclient.config.ClientConfig;

/** Applies a real client-side tint to the world sky colour. */
@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {
    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void genclient$tintSky(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
        if (!ClientConfig.isEnabled("Custom Sky")) return;
        Vec3d original = cir.getReturnValue();
        if (original == null) return;
        int argb = ClientConfig.skyTintColor();
        Vec3d tint = new Vec3d(((argb >> 16) & 255) / 255.0,
                ((argb >> 8) & 255) / 255.0, (argb & 255) / 255.0);
        double amount = ClientConfig.skyTintAmount();
        cir.setReturnValue(original.multiply(1.0 - amount).add(tint.multiply(amount)));
    }
}