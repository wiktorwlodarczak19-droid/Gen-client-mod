package pl.genclient.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.DustParticleEffect;
import org.joml.Vector3f;
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

        ClientEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (entity instanceof LivingEntity living && !living.isAlive()
                    && ClientConfig.isEnabled("Kill Effect")) {
                spawnKillEffect(world, living);
            }
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
            int count = (ClientConfig.lowFxMode ? 7 : 10) * ClientConfig.effectIntensity("Jump Circle");
            DustParticleEffect dust = dust("Jump Circle", 0.82f);
            for (int i = 0; i < count; i++) {
                double angle = (Math.PI * 2.0 * i / count) + ticks * 0.025;
                double x = player.getX() + Math.cos(angle) * radius;
                double z = player.getZ() + Math.sin(angle) * radius;
                world.addParticle(dust, x, player.getY() + 0.06, z, 0.0, 0.012, 0.0);
            }
        }

        if (ClientConfig.isEnabled("Glass Vapor") && player.isOnGround() && speed > 0.075 && ticks % 5 == 0) {
            double vaporX = player.getX() - velocity.x * 1.8;
            double vaporZ = player.getZ() - velocity.z * 1.8;
            DustParticleEffect vapor = dust("Glass Vapor", 0.72f);
            world.addParticle(ParticleTypes.CLOUD, vaporX, player.getY() + 0.12, vaporZ,
                    0.0, 0.018, 0.0);
            int trails = ClientConfig.lowFxMode ? 1 : ClientConfig.effectIntensity("Glass Vapor");
            for (int i = 0; i < trails; i++) {
                double offset = 0.18 + i * 0.12;
                world.addParticle(vapor, vaporX + velocity.z * offset,
                        player.getY() + 0.20 + i * 0.035, vaporZ - velocity.x * offset, 0.0, 0.012, 0.0);
                world.addParticle(vapor, vaporX - velocity.z * offset,
                        player.getY() + 0.20 + i * 0.035, vaporZ + velocity.x * offset, 0.0, 0.012, 0.0);
            }
        }

        if (ClientConfig.isEnabled("Aura Glint Light 3D") && ticks % 4 == 0) {
            double angle = ticks * 0.16;
            for (int i = 0; i < 3; i++) {
                double a = angle + (Math.PI * 2.0 * i / 3.0);
                world.addParticle(dust("Aura Glint Light 3D", 0.9f),
                        player.getX() + Math.cos(a) * 0.72,
                        player.getY() + 0.45 + Math.sin(angle * 0.7 + i) * 0.28,
                        player.getZ() + Math.sin(a) * 0.72, 0.0, 0.03, 0.0);
            }
        }

        if (ClientConfig.isEnabled("Armor Glass Light 3D")
                && !player.getEquippedStack(EquipmentSlot.CHEST).isEmpty() && ticks % 6 == 0) {
            double angle = ticks * 0.12;
            world.addParticle(dust("Armor Glass Light 3D", 0.75f),
                    player.getX() + Math.cos(angle) * 0.34,
                    player.getY() + 1.18 + Math.sin(angle * 1.4) * 0.12,
                    player.getZ() + Math.sin(angle) * 0.34, 0.0, 0.018, 0.0);
        }

        if (ClientConfig.isEnabled("Custom Sky") && ticks % 7 == 0) {
            Vec3d look = player.getRotationVec(1.0f);
            Vec3d right = new Vec3d(-look.z, 0.0, look.x);
            if (right.lengthSquared() > 0.0001) right = right.normalize();
            for (int i = 0; i < 2; i++) {
                double angle = ticks * 0.019 + i * Math.PI;
                double distance = 12.0 + i * 5.0;
                double x = player.getX() + Math.cos(angle) * distance + right.x * 3.0;
                double z = player.getZ() + Math.sin(angle) * distance + right.z * 3.0;
                double y = player.getY() + 14.0 + i * 4.0 + Math.sin(angle * 1.7) * 1.5;
                world.addParticle(dust("Custom Sky", 0.58f), x, y, z, 0.0, 0.0, 0.0);
            }
        }

        if (ClientConfig.sakuraPetals && ticks % (ClientConfig.lowFxMode ? 9 : 5) == 0) {
            int count = ClientConfig.lowFxMode ? 1 : 3;
            DustParticleEffect petal = dust("Sky Petals", 0.7f);
            for (int i = 0; i < count; i++) {
                double angle = ticks * 0.11 + i * (Math.PI * 2.0 / count);
                world.addParticle(petal, player.getX() + Math.cos(angle),
                        player.getY() + 1.0 + Math.sin(angle * 1.7) * 0.35,
                        player.getZ() + Math.sin(angle), 0.0, 0.018, 0.0);
            }
        }
        if (ClientConfig.isEnabled("Glint Hand") && !player.getMainHandStack().isEmpty() && ticks % 5 == 0) {
            Vec3d look = player.getRotationVec(1.0f).normalize();
            Vec3d side = new Vec3d(-look.z, 0.0, look.x);
            if (side.lengthSquared() > 0.0001) side = side.normalize();
            Vec3d handPosition = player.getEyePos()
                    .add(look.multiply(0.58))
                    .add(side.multiply(0.34))
                    .add(0.0, -0.24, 0.0);
            world.addParticle(dust("Glint Hand", 0.85f),
                    handPosition.x, handPosition.y, handPosition.z, 0.0, 0.025, 0.0);
        }
    }

    private static void spawnHitSparks(ClientWorld world, Entity target) {
        double centerY = target.getY() + Math.max(0.25, target.getHeight() * 0.55);
        int count = ClientConfig.lowFxMode ? 4 : 8 * ClientConfig.effectIntensity("Hit Sparks");
        DustParticleEffect spark = dust("Hit Sparks", 0.78f);
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2.0 * i / count;
            world.addParticle(spark, target.getX() + Math.cos(angle) * 0.32,
                    centerY + Math.sin(angle) * 0.24, target.getZ() + Math.sin(angle) * 0.32,
                    Math.cos(angle) * 0.04, 0.025, Math.sin(angle) * 0.04);
        }
    }

    private static void spawnKillEffect(ClientWorld world, LivingEntity target) {
        double centerY = target.getY() + Math.max(0.35, target.getHeight() * 0.52);
        int count = ClientConfig.lowFxMode ? 9 : 18 * ClientConfig.effectIntensity("Kill Effect");
        DustParticleEffect burst = dust("Kill Effect", 0.95f);
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2.0 * i / count;
            double radius = 0.35 + (i % 3) * 0.08;
            world.addParticle(burst, target.getX() + Math.cos(angle) * radius,
                    centerY + (i % 4) * 0.08, target.getZ() + Math.sin(angle) * radius,
                    Math.cos(angle) * 0.015, 0.035, Math.sin(angle) * 0.015);
        }
        world.addParticle(ParticleTypes.FLASH, target.getX(), centerY, target.getZ(), 0.0, 0.0, 0.0);
    }
    private static DustParticleEffect dust(String module, float size) {
        int color = ClientConfig.effectColor(module);
        float red = ((color >> 16) & 0xFF) / 255.0f;
        float green = ((color >> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;
        float scale = Math.max(0.25f, Math.min(1.6f, size * (0.65f + ClientConfig.effectIntensity(module) * 0.18f)));
        return new DustParticleEffect(new Vector3f(red, green, blue), scale);
    }
}
