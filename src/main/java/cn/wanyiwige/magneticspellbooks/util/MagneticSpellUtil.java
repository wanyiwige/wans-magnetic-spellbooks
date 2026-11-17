package cn.wanyiwige.magneticspellbooks.util;

import cn.wanyiwige.magneticspellbooks.registry.ModEffects;
import com.github.alexmodguy.alexscaves.server.entity.util.MagnetUtil;
import com.github.alexmodguy.alexscaves.server.misc.ACTagRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.function.Predicate;

public final class MagneticSpellUtil {
    private MagneticSpellUtil() {
    }

    public static boolean canManipulate(LivingEntity caster, LivingEntity target) {
        if (target == caster || !target.isAlive() || target.getType().is(ModTags.RESISTS_MAGNETS)) {
            return false;
        }
        if (caster.isAlliedTo(target)) {
            return false;
        }
        if (caster instanceof Player player && target instanceof Player other && !player.canHarmPlayer(other)) {
            return false;
        }
        return MagnetUtil.isPulledByMagnets(target);
    }

    public static boolean isFerromagnetic(ItemEntity item) {
        return item.isAlive() && !item.getItem().isEmpty() && item.getItem().is(ACTagRegistry.MAGNETIC_ITEMS);
    }

    public static void addMagneticImpulse(LivingEntity target, Vec3 direction, double force) {
        if (direction.lengthSqr() < 1.0E-4D) {
            return;
        }
        Vec3 impulse = direction.normalize().scale(force);
        Vec3 current = MagnetUtil.getEntityMagneticDelta(target);
        MagnetUtil.setEntityMagneticDelta(target, current.scale(0.85D).add(impulse));
        target.hasImpulse = true;
    }

    public static void addMagneticImpulse(ItemEntity item, Vec3 direction, double force) {
        if (direction.lengthSqr() < 1.0E-4D) {
            return;
        }
        Vec3 impulse = direction.normalize().scale(force);
        Vec3 current = MagnetUtil.getEntityMagneticDelta(item);
        MagnetUtil.setEntityMagneticDelta(item, current.scale(0.85D).add(impulse));
        item.hasImpulse = true;
    }

    /**
     * Applies a directional force to a living target. Entities that Alex's Caves
     * considers "pullable by magnets" receive a sustained magnetic-delta impulse;
     * ordinary entities receive a direct velocity impulse so attraction/repulsion
     * still works against non-magnetic targets.
     */
    public static void applyMagneticForce(LivingEntity target, Vec3 direction, double force) {
        if (direction.lengthSqr() < 1.0E-4D) {
            return;
        }
        Vec3 impulse = direction.normalize().scale(force);
        if (MagnetUtil.isPulledByMagnets(target)) {
            Vec3 current = MagnetUtil.getEntityMagneticDelta(target);
            MagnetUtil.setEntityMagneticDelta(target, current.scale(0.85D).add(impulse));
        } else {
            target.setDeltaMovement(target.getDeltaMovement().add(impulse.scale(0.5D)));
        }
        target.hasImpulse = true;
    }

    public static LivingEntity findLivingTarget(Level level, LivingEntity caster, double range,
                                                 Predicate<LivingEntity> predicate) {
        if (caster instanceof cn.wanyiwige.magneticspellbooks.entities.MagneticMageEntity mage) {
            LivingEntity target = mage.getSpellTarget();
            return canTargetEnemy(caster, target) && caster.distanceToSqr(target) <= range * range
                    && caster.hasLineOfSight(target) && predicate.test(target) ? target : null;
        }
        Vec3 eye = caster.getEyePosition();
        Vec3 end = eye.add(caster.getLookAngle().scale(range));
        return level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().expandTowards(
                        caster.getLookAngle().scale(range)).inflate(1.0D), entity ->
                        entity != caster && entity.isAlive() && caster.hasLineOfSight(entity) && predicate.test(entity))
                .stream()
                .filter(entity -> entity.getBoundingBox().inflate(0.4D).clip(eye, end).isPresent())
                .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(caster)))
                .orElse(null);
    }

    public static boolean canTargetEnemy(LivingEntity caster, LivingEntity target) {
        if (target == null || target == caster || !target.isAlive() || caster.isAlliedTo(target)) {
            return false;
        }
        // Neutral NPC area spells must not turn every bystander into an enemy.
        if (caster instanceof cn.wanyiwige.magneticspellbooks.entities.MagneticMageEntity mage) {
            return target == mage.getSpellTarget() && mage.canAttack(target);
        }
        return !(caster instanceof Player player && target instanceof Player other && !player.canHarmPlayer(other));
    }

    public static boolean canMagnetize(LivingEntity caster, LivingEntity target) {
        return canTargetEnemy(caster, target) && !target.getType().is(ModTags.RESISTS_MAGNETS);
    }

    public static LivingEntity findBarrageTarget(Level level, LivingEntity caster, double range) {
        double rangeSqr = range * range;
        LivingEntity marked = level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(range),
                        entity -> canTargetEnemy(caster, entity)
                                && entity.distanceToSqr(caster) <= rangeSqr
                                && caster.hasLineOfSight(entity)
                                && entity.hasEffect(ModEffects.NOTOR_SCANNED.get()))
                .stream()
                .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(caster)))
                .orElse(null);
        if (marked != null) {
            return marked;
        }

        Vec3 look = caster.getLookAngle().normalize();
        return level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(range),
                        entity -> canTargetEnemy(caster, entity)
                                && entity.distanceToSqr(caster) <= rangeSqr
                                && caster.hasLineOfSight(entity))
                .stream()
                .filter(entity -> {
                    Vec3 direction = entity.getEyePosition().subtract(caster.getEyePosition());
                    return direction.lengthSqr() > 1.0E-4D && look.dot(direction.normalize()) >= 0.35D;
                })
                .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(caster)))
                .orElse(null);
    }
}
