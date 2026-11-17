package cn.wanyiwige.magneticspellbooks.util;

import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** Bounded, server-broadcast supplemental feedback; main forms live in client renderers. */
public final class MagneticCombatEffects {
    private MagneticCombatEffects() { }
    public static void arc(ServerLevel level, Vec3 start, Vec3 end, boolean red) {
        level.sendParticles(red ? ACParticleRegistry.SCARLET_SHIELD_LIGHTNING.get()
                : ACParticleRegistry.AZURE_SHIELD_LIGHTNING.get(),
                start.x, start.y, start.z, 0, end.x, end.y, end.z, 1);
    }
    public static void burst(ServerLevel level, Vec3 position, float radius, int debris, int arcs) {
        level.sendParticles(ACParticleRegistry.GALENA_DEBRIS.get(), position.x, position.y, position.z,
                Math.min(32, debris), radius * 0.35, 0.3, radius * 0.35, 0.12);
        level.sendParticles(ParticleTypes.FLASH, position.x, position.y, position.z, 1, 0, 0, 0, 0);
        for (int i = 0; i < Math.min(8, arcs); i++) {
            double angle = i * Math.PI * 2 / Math.max(1, arcs);
            arc(level, position, position.add(Math.cos(angle)*radius, (i%3-1)*0.3, Math.sin(angle)*radius), (i&1)==0);
        }
        level.playSound(null, BlockPos.containing(position), SoundEvents.GENERIC_EXPLODE,
                SoundSource.PLAYERS, 0.9F, 0.6F);
    }
    public static void charge(ServerLevel level, Vec3 position, float pitch) {
        level.playSound(null, BlockPos.containing(position), SoundEvents.BEACON_POWER_SELECT,
                SoundSource.PLAYERS, 0.4F, pitch);
    }
}

