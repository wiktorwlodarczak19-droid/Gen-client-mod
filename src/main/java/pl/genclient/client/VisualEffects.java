package pl.genclient.client;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import pl.genclient.config.ClientConfig;

/** Purely cosmetic client-side effects. Nothing here changes combat, reach, or entity state. */
public final class VisualEffects {
    private static int ticks;

    private VisualEffects() {}

    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (player == client.player && world instanceof ClientWorld clientWorld
                    && ClientConfig.isEnabled("Hit Sparks")) {
                spawnHitSparks(clientWorld, entity);
            }
            return ActionResult.PASS;
        });
    }

    public static void tick(MinecraftClient client) {
        ticks++;
        ClientPlayerEntity player = client.player;
        ClientWorld world = client.world;
        if (player == null || world == null || player.isSpectator()) return;

        Vec3d velocity = player.getVelocity();
        double speed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);

        if (ClientConfig.isEnabled("Jump Circle") && !player.isOnGround() && ticks % 2 == 0) {
            double radius = 0.58;
            for (int i = 0; i < 14; i++) {
                double angle = (Math.PI * 2.0 * i / 14.0) + ticks * 0.025;
                double x = player.getX() + Math.cos(angle) * radius;
                double z = player.getZ() + Math.sin(angle) * radius;
                world.addParticle(ParticleTypes.END_ROD, x, player.getY() + 0.06, z,
                        0.0, 0.012, 0.0);
            }
        }

        if (ClientConfig.isEnabled("Glass Vapor") && player.isOnGround() && speed > 0.075 && ticks % 5 == 0) {
            double vaporX = player.getX() - velocity.x * 1.8;
            double vaporZ = player.getZ() - velocity.z * 1.8;
            world.addParticle(ParticleTypes.CLOUD, vaporX, player.getY() + 0.12, vaporZ,
                    0.0, 0.018, 0.0);
            world.addParticle(ParticleTypes.END_ROD, vaporX + velocity.z * 0.35,
                    player.getY() + 0.22, vaporZ - velocity.x * 0.35, 0.0, 0.012, 0.0);
            world.addParticle(ParticleTypes.END_ROD, vaporX - velocity.z * 0.35,
                    player.getY() + 0.22, vaporZ + velocity.x * 0.35, 0.0, 0.012, 0.0);
        }

        if (ClientConfig.isEnabled("Aura Glint") && ticks % 4 == 0) {
            double angle = ticks * 0.16;
            for (int i = 0; i < 3; i++) {
                double a = angle + (Math.PI * 2.0 * i / 3.0);
                world.addParticle(ParticleTypes.ENCHANT,
                        player.getX() + Math.cos(a) * 0.72,
                        player.getY() + 0.45 + Math.sin(angle * 0.7 + i) * 0.28,
                        player.getZ() + Math.sin(a) * 0.72,
                        0.0, 0.03, 0.0);
            }
        }

        if (ClientConfig.isEnabled("Armor Glass Light")
                && !player.getEquippedStack(EquipmentSlot.CHEST).isEmpty() && ticks % 6 == 0) {
            double angle = ticks * 0.12;
            world.addParticle(ParticleTypes.END_ROD,
                    player.getX() + Math.cos(angle) * 0.34,
                    player.getY() + 1.18 + Math.sin(angle * 1.4) * 0.12,
                    player.getZ() + Math.sin(angle) * 0.34,
                    0.0, 0.018, 0.0);
        }
    }

    private static void spawnHitSparks(ClientWorld world, Entity target) {
        double centerY = target.getY() + Math.max(0.25, target.getHeight() * 0.55);
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI * 2.0 * i / 8.0;
            world.addParticle(ParticleTypes.CRIT,
                    target.getX() + Math.cos(angle) * 0.32,
                    centerY + Math.sin(angle) * 0.24,
                    target.getZ() + Math.sin(angle) * 0.32,
                    Math.cos(angle) * 0.04,
                    0.025,
                    Math.sin(angle) * 0.04);
        }
    }
}
