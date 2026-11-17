package cn.wanyiwige.magneticspellbooks.compat;

import cn.wanyiwige.magneticspellbooks.util.MagneticSpellUtil;
import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Behaviour and VFX replacements for the original traveloptics spell instances. */
public final class TravelOpticsSpellCompat {
    private TravelOpticsSpellCompat() {
    }

    public static void castEmPulse(AbstractSpell spell, Level level, int spellLevel, LivingEntity caster,
                                   CastSource castSource, MagicData magicData) {
        spell.playSound(spell.getCastFinishSound(), caster);
        if (level.isClientSide) {
            return;
        }

        double radius = 2.0D + spellLevel * 2.0D;
        double radiusSqr = radius * radius;
        int duration = Math.max(1, (int) (spell.getSpellPower(spellLevel, caster) * 20.0F));
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                caster.getBoundingBox().inflate(radius), entity ->
                        MagneticSpellUtil.canTargetEnemy(caster, entity)
                                && entity.distanceToSqr(caster) < radiusSqr)) {
            target.addEffect(new MobEffectInstance(ACEffectRegistry.STUNNED.get(), duration));
            spawnArcPair(level, caster.getEyePosition(), target.getEyePosition());
            spawnPolarityOrbits(level, target.getEyePosition(), 0.75D);
        }
        spawnBipolarRing(level, caster.position().add(0.0D, 0.15D, 0.0D), radius, 12, caster);
    }

    public static void spawnBipolarRing(Level level, Vec3 center, double radius, int count, LivingEntity randomSource) {
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2.0D * i / count;
            Vec3 edge = center.add(Math.cos(angle) * radius, 0.1D + randomSource.getRandom().nextDouble() * 0.35D,
                    Math.sin(angle) * radius);
            if ((i & 1) == 0) {
                spawnArc(level, ACParticleRegistry.SCARLET_SHIELD_LIGHTNING.get(), center, edge);
            } else {
                spawnArc(level, ACParticleRegistry.AZURE_SHIELD_LIGHTNING.get(), edge, center);
            }
        }
    }

    public static void spawnArcPair(Level level, Vec3 start, Vec3 end) {
        spawnArc(level, ACParticleRegistry.SCARLET_MAGNETIC_FLOW.get(), start, end);
        spawnArc(level, ACParticleRegistry.AZURE_MAGNETIC_FLOW.get(), end, start);
    }

    public static void spawnPolarityOrbits(Level level, Vec3 center, double offset) {
        MagicManager.spawnParticles(level, ACParticleRegistry.SCARLET_MAGNETIC_ORBIT.get(),
                center.x + offset, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D, true);
        MagicManager.spawnParticles(level, ACParticleRegistry.AZURE_MAGNETIC_ORBIT.get(),
                center.x - offset, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D, true);
    }

    public static void spawnGalenaDischarge(LivingEntity target, int stacks, boolean azure) {
        Vec3 center = target.getEyePosition().add(0.0D, -target.getBbHeight() * 0.25D, 0.0D);
        ParticleOptions particle = azure
                ? ACParticleRegistry.AZURE_SHIELD_LIGHTNING.get()
                : ACParticleRegistry.SCARLET_SHIELD_LIGHTNING.get();
        int count = Math.min(6 + stacks, 20);
        for (int i = 0; i < count; i++) {
            Vec3 edge = center.add(
                    (target.getRandom().nextDouble() - 0.5D) * target.getBbWidth() * 2.0D,
                    (target.getRandom().nextDouble() - 0.5D) * target.getBbHeight(),
                    (target.getRandom().nextDouble() - 0.5D) * target.getBbWidth() * 2.0D);
            spawnArc(target.level(), particle, center, edge);
        }
        spawnPolarityOrbits(target.level(), center, 0.8D);
    }

    private static void spawnArc(Level level, ParticleOptions particle, Vec3 start, Vec3 end) {
        // Alex's magnetic lightning/flow particles use packet velocity fields as the other endpoint.
        MagicManager.spawnParticles(level, particle, start.x, start.y, start.z,
                0, end.x, end.y, end.z, 1.0D, true);
    }
}
