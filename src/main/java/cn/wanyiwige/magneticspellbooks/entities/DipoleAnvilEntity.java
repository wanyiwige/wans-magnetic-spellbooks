package cn.wanyiwige.magneticspellbooks.entities;
import cn.wanyiwige.magneticspellbooks.registry.ModSpells;
import cn.wanyiwige.magneticspellbooks.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import net.minecraft.nbt.CompoundTag;

public final class DipoleAnvilEntity extends MagneticCombatEntity {
    public DipoleAnvilEntity(EntityType<?> type, Level level) { super(type, level); maximumAge = 40; }
    @Override protected void serverTick(ServerLevel level, LivingEntity owner) {
        if (finished) return;
        LivingEntity target = primary();
        if (!validPrimary(owner, target)
                || owner.distanceToSqr(target) > Math.pow(MagneticCombatRules.range("dipole_anvil", spellLevel), 2)) {
            discard(); return;
        }
        setPos(MagneticCombatSupport.center(target));
        if (age == 1) {
            var snapshot = MagnetizationSynergyUtil.reserveForAttack(owner, target, this);
            CompoundTag visual = getVisual().copy(); visual.putBoolean("Magnetic", snapshot.actualStrength() > 0);
            Vec3 above = position().add(0,2,0), below = position().add(0,-2,0);
            double up = level.clip(new ClipContext(position(), above, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,this)).getLocation().distanceTo(position());
            double down = level.clip(new ClipContext(position(), below, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,this)).getLocation().distanceTo(position());
            visual.putFloat("Scale", (float)Math.max(0.25,Math.min(1,Math.min(up,down))));
            setVisual(visual);
            MagneticCombatEffects.charge(level, position(), 0.6F);
        }
        boolean magnetic = MagnetizationSynergyUtil.reserveForAttack(owner,target,this).actualStrength() > 0;
        if (magnetic && (age == 1 || age == 11 || age == 21)) {
            hit(ModSpells.DIPOLE_ANVIL.get(), owner, target, MagneticCombatRules.anvilPulse(spellLevel) * power * reward(owner,target));
            MagneticCombatEffects.burst(level, position(), 0.7F, 8, 2);
        }
        if (age == 31) {
            float radius = MagneticCombatRules.anvilRadius(effectiveStrength(owner,target));
            float unscannedRadius = MagneticCombatRules.anvilRadius(MagnetizationSynergyUtil.reserveForAttack(owner,target,this).actualStrength());
            float damage = MagneticCombatRules.anvilFinish(spellLevel) * power;
            hit(ModSpells.DIPOLE_ANVIL.get(), owner, target, damage * reward(owner,target));
            for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(unscannedRadius),
                    entity -> entity != target && MagneticSpellUtil.canTargetEnemy(owner,entity)
                        && entity.distanceToSqr(this) <= unscannedRadius*unscannedRadius
                        && MagneticCombatSupport.visible(level,position(),entity))) {
                hit(ModSpells.DIPOLE_ANVIL.get(),owner,other,damage*0.4F);
            }
            CompoundTag visual = getVisual().copy(); visual.putFloat("Radius",radius); setVisual(visual);
            MagneticCombatEffects.burst(level,position(),radius,32,8); finish();
        }
    }
}

