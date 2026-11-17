package cn.wanyiwige.magneticspellbooks.util;

import cn.wanyiwige.magneticspellbooks.registry.ModEffects;
import cn.wanyiwige.magneticspellbooks.entities.MagneticMageEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.Comparator;
import java.util.List;

public final class MagneticCombatSupport {
    private MagneticCombatSupport() { }
    public static Vec3 center(LivingEntity entity) { return entity.position().add(0, entity.getBbHeight() * 0.5, 0); }
    public static boolean visible(Level level, Vec3 origin, LivingEntity target) {
        return level.clip(new ClipContext(origin, target.getEyePosition(),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, target)).getType() == HitResult.Type.MISS;
    }
    public static LivingEntity select(Level level, LivingEntity caster, double range, boolean magneticOnly) {
        if (caster instanceof MagneticMageEntity) {
            return MagneticSpellUtil.findLivingTarget(level, caster, range,
                    target -> !magneticOnly || MagnetizationSynergyUtil.getStrength(target) > 0);
        }
        LivingEntity marked = level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(range),
                target -> MagneticSpellUtil.canTargetEnemy(caster, target)
                    && target.distanceToSqr(caster) <= range * range && caster.hasLineOfSight(target)
                    && (!magneticOnly || MagnetizationSynergyUtil.getStrength(target) > 0)
                    && target.hasEffect(ModEffects.NOTOR_SCANNED.get())).stream()
            .min(Comparator.comparing((LivingEntity target) -> !MagnetizationSynergyUtil.hasScanReward(caster, target))
                    .thenComparingDouble(caster::distanceToSqr)).orElse(null);
        return marked != null ? marked : MagneticSpellUtil.findLivingTarget(level, caster, range,
                target -> MagneticSpellUtil.canTargetEnemy(caster, target)
                    && (!magneticOnly || MagnetizationSynergyUtil.getStrength(target) > 0));
    }
    public static List<LivingEntity> chainTargets(Level level, LivingEntity caster, LivingEntity primary, int spellLevel) {
        if (primary == null) return List.of();
        double radius = 8 + 2 * (MagneticCombatRules.level(spellLevel, 3) - 1);
        return level.getEntitiesOfClass(LivingEntity.class, primary.getBoundingBox().inflate(radius),
                target -> target != primary && MagneticSpellUtil.canTargetEnemy(caster, target)
                    && target.distanceToSqr(primary) <= radius * radius
                    && primary.hasLineOfSight(target))
            .stream().sorted(Comparator.comparing((LivingEntity target) -> MagnetizationSynergyUtil.getStrength(target) == 0)
                    .thenComparingDouble(primary::distanceToSqr))
            .limit(MagneticCombatRules.chainTargets(spellLevel) - 1).toList();
    }
}

