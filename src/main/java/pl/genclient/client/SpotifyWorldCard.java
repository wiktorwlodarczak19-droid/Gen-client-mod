package pl.genclient.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import pl.genclient.config.ClientConfig;
import org.joml.Matrix4f;

/** Smooth camera-facing Spotify now-playing card rendered as world-space text. */
public final class SpotifyWorldCard {
    private static final int CARD_BACKGROUND = 0x98090C13;

    private SpotifyWorldCard() {}

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(SpotifyWorldCard::render);
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.options.hudHidden
                || !ClientConfig.dynamicIsland) return;

        SpotifyService.TrackSnapshot track = SpotifyService.getSnapshot();
        if (!track.hasTrack()) return;

        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();
        if (matrices == null || consumers == null) return;

        Vec3d look = client.player.getRotationVec(1.0F);
        Vec3d side = new Vec3d(-look.z, 0.0, look.x);
        if (side.lengthSquared() > 0.0001) side = side.normalize();

        // Float just above the player's right shoulder, without spawning Minecraft particles.
        Vec3d anchor = client.player.getPos()
                .add(side.multiply(0.72))
                .add(0.0, client.player.getHeight() + 0.28, 0.0);
        Vec3d camera = context.camera().getPos();

        matrices.push();
        matrices.translate(anchor.x - camera.x, anchor.y - camera.y, anchor.z - camera.z);
        matrices.multiply(context.camera().getRotation());
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
        matrices.scale(-0.0125F, -0.0125F, 0.0125F);

        TextRenderer renderer = client.textRenderer;
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        drawCentered(renderer, consumers, matrix, "GEN  /  SPOTIFY", -21.0F, 0xFFB9C1D0, 0x00000000);
        String title = renderer.trimToWidth(track.title(), 178);
        String artist = renderer.trimToWidth(track.artists(), 178);
        drawCentered(renderer, consumers, matrix, title, -9.0F, 0xFFFFFFFF, CARD_BACKGROUND);
        drawCentered(renderer, consumers, matrix, artist, 4.0F, 0xFFB8C0CE, CARD_BACKGROUND);
        if (!track.isPlaying()) {
            drawCentered(renderer, consumers, matrix, "PAUSED", 17.0F, 0xFFF4BFD3, 0x00000000);
        }

        matrices.pop();
    }

    private static void drawCentered(TextRenderer renderer, VertexConsumerProvider consumers,
                                     Matrix4f matrix, String text, float y, int color, int background) {
        float x = -renderer.getWidth(text) / 2.0F;
        renderer.draw(text, x, y, color, true, matrix, consumers,
                TextRenderer.TextLayerType.NORMAL, background, LightmapTextureManager.MAX_LIGHT_COORDINATE);
    }
}
