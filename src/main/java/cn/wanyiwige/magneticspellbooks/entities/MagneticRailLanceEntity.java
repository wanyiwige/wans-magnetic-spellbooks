package cn.wanyiwige.magneticspellbooks.entities;
import cn.wanyiwige.magneticspellbooks.registry.ModSpells;
import cn.wanyiwige.magneticspellbooks.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import net.minecraft.nbt.*;
import java.util.*;

/** Segment-swept collisions prevent tunneling through entities or the first wall. */
public final class MagneticRailLanceEntity extends MagneticCombatEntity {
    private Vec3 origin=Vec3.ZERO,direction=new Vec3(0,0,1);
    private double travelled;
    private int hitIndex,allowedPierces,fadeUntil;
    private final Set<UUID> hitTargets=new HashSet<>();
    public MagneticRailLanceEntity(EntityType<?> type,Level level) { super(type,level);maximumAge=42; }
    @Override public void configure(LivingEntity owner,LivingEntity target,int spellLevel,float power) {
        super.configure(owner,null,spellLevel,power);
        origin=position();direction=owner.getLookAngle().normalize();
        CompoundTag visual=getVisual().copy();
        visual.putDouble("OriginX",origin.x);visual.putDouble("OriginY",origin.y);visual.putDouble("OriginZ",origin.z);
        visual.putDouble("DirX",direction.x);visual.putDouble("DirY",direction.y);visual.putDouble("DirZ",direction.z);
        visual.putDouble("Travel",0);setVisual(visual);
    }
    @Override public void afterSpawn(LivingEntity owner) { }
    @Override protected void serverTick(ServerLevel level,LivingEntity owner) {
        if(finished) { if(age>=fadeUntil)discard();return; }
        Vec3 start=position();
        double step=Math.min(3.2,MagneticCombatRules.range("magnetic_rail_lance",spellLevel)-travelled);
        if(step<=0) { stop(level);return; }
        Vec3 desired=start.add(direction.scale(step));
        BlockHitResult block=level.clip(new ClipContext(start,desired,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));
        Vec3 end=block.getLocation();
        List<LivingEntity> candidates=level.getEntitiesOfClass(LivingEntity.class,getBoundingBox().expandTowards(end.subtract(start)).inflate(0.4),
                e->MagneticSpellUtil.canTargetEnemy(owner,e)&&!hitTargets.contains(e.getUUID())
                    &&e.getBoundingBox().inflate(0.15).clip(start,end).isPresent())
            .stream().sorted(Comparator.comparingDouble(e->e.getBoundingBox().inflate(0.15).clip(start,end).orElse(end).distanceToSqr(start))).toList();
        for(LivingEntity target:candidates) {
            Vec3 impact=target.getBoundingBox().inflate(0.15).clip(start,end).orElse(end);
            if(block.getType()!=HitResult.Type.MISS && impact.distanceToSqr(start)>=end.distanceToSqr(start)) break;
            int actual=MagnetizationSynergyUtil.getStrength(target);
            int effective=actual;float scan=1;
            if(hitIndex==0) {
                targetUUID=target.getUUID();
                var snapshot=MagnetizationSynergyUtil.reserveForAttack(owner,target,this);
                effective=snapshot.effectiveStrength();scan=snapshot.damageMultiplier();
                allowedPierces=MagneticCombatRules.railPierces(actual,effective);
            }
            float damage=MagneticCombatRules.rail(spellLevel)*power*(actual>0?1.5F:1)
                    *MagneticCombatRules.railDecay(hitIndex)*scan;
            hitTargets.add(target.getUUID());hit(ModSpells.MAGNETIC_RAIL_LANCE.get(),owner,target,damage);
            MagneticCombatEffects.burst(level,impact,actual>0?0.8F:0.35F,4,0);
            if(actual>0) {
                CompoundTag v=getVisual().copy();v.putDouble("HitX",impact.x);v.putDouble("HitY",impact.y);v.putDouble("HitZ",impact.z);
                v.putInt("HitAge",age);setVisual(v);
            }
            if(hitIndex++>=allowedPierces) {
                travelled+=impact.distanceTo(start);setPos(impact);updateTravel();stop(level);return;
            }
        }
        travelled+=end.distanceTo(start);setPos(end);updateTravel();
        if(block.getType()!=HitResult.Type.MISS || travelled>=MagneticCombatRules.range("magnetic_rail_lance",spellLevel)) stop(level);
    }
    private void updateTravel() { CompoundTag v=getVisual().copy();v.putDouble("Travel",travelled);setVisual(v); }
    private void stop(ServerLevel level) {
        MagneticCombatEffects.burst(level,position(),0.65F,8,2);finish();fadeUntil=age+6;
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("Travelled",travelled);tag.putInt("HitIndex",hitIndex);tag.putInt("Pierces",allowedPierces);tag.putInt("FadeUntil",fadeUntil);
        tag.putDouble("OX",origin.x);tag.putDouble("OY",origin.y);tag.putDouble("OZ",origin.z);
        tag.putDouble("DX",direction.x);tag.putDouble("DY",direction.y);tag.putDouble("DZ",direction.z);
        ListTag list=new ListTag();for(UUID id:hitTargets){CompoundTag t=new CompoundTag();t.putUUID("Id",id);list.add(t);}tag.put("Hits",list);
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);travelled=tag.getDouble("Travelled");hitIndex=tag.getInt("HitIndex");
        allowedPierces=tag.getInt("Pierces");fadeUntil=tag.getInt("FadeUntil");
        origin=new Vec3(tag.getDouble("OX"),tag.getDouble("OY"),tag.getDouble("OZ"));
        direction=new Vec3(tag.getDouble("DX"),tag.getDouble("DY"),tag.getDouble("DZ"));
        hitTargets.clear();ListTag list=tag.getList("Hits",Tag.TAG_COMPOUND);for(int i=0;i<list.size();i++)hitTargets.add(list.getCompound(i).getUUID("Id"));
    }
}

