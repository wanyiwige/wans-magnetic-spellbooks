package cn.wanyiwige.magneticspellbooks.entities;
import cn.wanyiwige.magneticspellbooks.registry.ModSpells;
import cn.wanyiwige.magneticspellbooks.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;

public final class FerrousSandstormEntity extends MagneticCombatEntity {
    public FerrousSandstormEntity(EntityType<?> type,Level level) { super(type,level);maximumAge=90; }
    @Override public void configure(LivingEntity owner,LivingEntity target,int spellLevel,float power) {
        super.configure(owner,target,spellLevel,power);
        CompoundTag visual=getVisual().copy();visual.putFloat("Radius",3+spellLevel);setVisual(visual);
    }
    @Override protected void serverTick(ServerLevel level,LivingEntity owner) {
        if(finished) return;
        float radius=3+spellLevel;
        if(age==1) MagneticCombatEffects.charge(level,position(),0.5F);
        if(age%10==1 && age<=71) {
            int deflectId=-1;
            for(LivingEntity target:level.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(radius,4,radius),
                    e->MagneticSpellUtil.canTargetEnemy(owner,e) && e.position().subtract(position()).multiply(1,0,1).lengthSqr()<=radius*radius
                        && Math.abs(e.getY()-getY())<=4 && MagneticCombatSupport.visible(level,position().add(0,0.3,0),e))) {
                int actual=MagnetizationSynergyUtil.getStrength(target);
                if(actual>0 && deflectId<0)deflectId=target.getId();
                int effective=actual;
                boolean isPrimary=targetUUID!=null && target.getUUID().equals(targetUUID);
                if(isPrimary && actual>0) {
                    var snapshot=MagnetizationSynergyUtil.reserveForAttack(owner,target,this);
                    if(snapshot.scanBonus()) effective=Math.min(5,actual+1);
                }
                float damage=MagneticCombatRules.stormPulse(spellLevel)*power*MagneticCombatRules.stormMultiplier(effective);
                if(isPrimary) damage*=reward(owner,target);
                hit(ModSpells.FERROUS_SANDSTORM.get(),owner,target,damage);
            }
            CompoundTag visual=getVisual().copy();visual.putInt("DeflectId",deflectId);setVisual(visual);
            double angle=age*0.1;
            MagneticCombatEffects.arc(level,position().add(Math.cos(angle)*radius,1,Math.sin(angle)*radius),position().add(0,3,0),true);
            MagneticCombatEffects.arc(level,position().add(-Math.cos(angle)*radius,2,-Math.sin(angle)*radius),position().add(0,0.2,0),false);
        }
        // Eight pulses at 1,11,...71; final at 81 follows ten ticks later.
        if(age==81) {
            for(LivingEntity target:level.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(radius,4,radius),
                    e->MagneticSpellUtil.canTargetEnemy(owner,e) && e.position().subtract(position()).multiply(1,0,1).lengthSqr()<=radius*radius
                        && Math.abs(e.getY()-getY())<=4 && MagneticCombatSupport.visible(level,position().add(0,0.3,0),e))) {
                float damage=MagneticCombatRules.stormFinish(spellLevel)*power;
                if(targetUUID!=null && target.getUUID().equals(targetUUID)) damage*=reward(owner,target);
                hit(ModSpells.FERROUS_SANDSTORM.get(),owner,target,damage);
            }
            MagneticCombatEffects.burst(level,position().add(0,1,0),radius,32,8);finish();
        }
    }
}

