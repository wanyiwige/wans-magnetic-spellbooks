package cn.wanyiwige.magneticspellbooks.entities;
import cn.wanyiwige.magneticspellbooks.registry.ModSpells;
import cn.wanyiwige.magneticspellbooks.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;

public final class PolarityCollapseEntity extends MagneticCombatEntity {
    public PolarityCollapseEntity(EntityType<?> type, Level level) { super(type, level); maximumAge = 28; }
    @Override public void afterSpawn(LivingEntity owner) {
        super.afterSpawn(owner);
        LivingEntity target = primary();
        if (target != null) MagnetizationSynergyUtil.consumeOneLevel(target);
    }
    @Override protected void serverTick(ServerLevel level, LivingEntity owner) {
        if (finished) return;
        LivingEntity target = primary();
        if (!validPrimary(owner, target) || MagneticCombatSupport.center(target).distanceToSqr(initialTarget) > 36
                || owner.distanceToSqr(target) > Math.pow(MagneticCombatRules.range("polarity_collapse", spellLevel), 2)) {
            discard(); return;
        }
        setPos(MagneticCombatSupport.center(target));
        if (age == 1 || age == 10) MagneticCombatEffects.charge(level, position(), 0.8F + age * 0.04F);
        if (age == 10) {
            MagneticCombatEffects.arc(level, position().add(-1,0,0), position(), true);
            MagneticCombatEffects.arc(level, position().add(1,0,0), position(), false);
        }
        if (age == 20) {
            var snapshot = MagnetizationSynergyUtil.reserveForAttack(owner, target, this);
            float damage = MagneticCombatRules.collapse(spellLevel, snapshot.effectiveStrength()) * power;
            hit(ModSpells.POLARITY_COLLAPSE.get(), owner, target, damage * snapshot.damageMultiplier());
            float radius = 3 + 0.5F * (spellLevel - 1);
            float areaDamage = MagneticCombatRules.collapse(spellLevel, snapshot.actualStrength()) * power * 0.4F;
            for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius),
                    entity -> entity != target && MagneticSpellUtil.canTargetEnemy(owner, entity)
                        && entity.distanceToSqr(this) <= radius * radius
                        && MagneticCombatSupport.visible(level, position(), entity))) {
                hit(ModSpells.POLARITY_COLLAPSE.get(), owner, other, areaDamage);
            }
            CompoundTag visual = getVisual().copy(); visual.putFloat("Radius", radius); setVisual(visual);
            MagneticCombatEffects.burst(level, position(), radius, 32, 8);
            finish();
        }
    }
}

