package cn.wanyiwige.magneticspellbooks.entities;
import cn.wanyiwige.magneticspellbooks.registry.ModSpells;
import cn.wanyiwige.magneticspellbooks.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import net.minecraft.nbt.CompoundTag;
import java.util.UUID;

public final class PolarityReversalTrackerEntity extends MagneticCombatEntity {
    private Vec3 previous=Vec3.ZERO;
    private boolean magnetic;
    private boolean pendingCollision;
    private UUID collisionTarget;
    private Vec3 collisionPoint=Vec3.ZERO;
    public PolarityReversalTrackerEntity(EntityType<?> type,Level level) { super(type,level);maximumAge=27; }
    @Override public void afterSpawn(LivingEntity owner) {
        super.afterSpawn(owner);LivingEntity target=primary();
        if(target!=null) {
            previous=MagneticCombatSupport.center(target);
            magnetic=MagnetizationSynergyUtil.reserveForAttack(owner,target,this).actualStrength()>0;
            hit(ModSpells.POLARITY_REVERSAL.get(),owner,target,MagneticCombatRules.reversal(spellLevel)*power*reward(owner,target));
            CompoundTag v=getVisual().copy();v.putBoolean("Magnetic",magnetic);setVisual(v);
        }
    }
    private void force(LivingEntity target,Vec3 direction,double magnitude) {
        if(target.getType().is(ModTags.RESISTS_MAGNETS)) return;
        double resistance=Math.max(0,Math.min(1,target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)));
        MagneticSpellUtil.applyMagneticForce(target,direction,magnitude*(1-resistance));
    }
    @Override protected void serverTick(ServerLevel level,LivingEntity owner) {
        if(finished)return;
        LivingEntity target=primary();
        if(!MagneticSpellUtil.canTargetEnemy(owner,target)) {discard();return;}
        Vec3 current=MagneticCombatSupport.center(target);setPos(current);
        if(age<=3) force(target,owner.position().subtract(target.position()),0.16);
        if(age==4) {
            Vec3 direction=target.position().subtract(owner.position()).normalize();
            force(target,direction,0.8+0.1*effectiveStrength(owner,target));
            MagneticCombatEffects.arc(level,owner.getEyePosition(),current,true);
        }
        if(pendingCollision && age>=10) {
            float damage=MagneticCombatRules.collision(spellLevel)*power;
            hit(ModSpells.POLARITY_REVERSAL.get(),owner,target,damage*reward(owner,target));
            LivingEntity other=living(collisionTarget);
            if(MagneticSpellUtil.canTargetEnemy(owner,other) && MagneticCombatSupport.visible(level,collisionPoint,other)) {
                hit(ModSpells.POLARITY_REVERSAL.get(),owner,other,damage*0.5F);
                MagneticCombatEffects.arc(level,collisionPoint,MagneticCombatSupport.center(other),false);
            }
            MagneticCombatEffects.burst(level,collisionPoint,1,12,4);finish();return;
        }
        if(magnetic && !pendingCollision && age>=5 && age<=20) {
            BlockHitResult block=level.clip(new ClipContext(previous,current,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,target));
            boolean wall=block.getType()!=HitResult.Type.MISS
                    || (target.horizontalCollision && current.distanceToSqr(initialTarget)>0.01);
            LivingEntity collided=level.getEntitiesOfClass(LivingEntity.class,target.getBoundingBox().expandTowards(previous.subtract(current)).inflate(0.1),
                    other->other!=target && MagneticSpellUtil.canTargetEnemy(owner,other)
                        && (other.getBoundingBox().intersects(target.getBoundingBox())
                            ||other.getBoundingBox().inflate(target.getBbWidth()*0.5).clip(previous,current).isPresent()))
                .stream().filter(other->MagneticCombatSupport.visible(level,current,other)).findFirst().orElse(null);
            if(wall||collided!=null) {
                pendingCollision=true;collisionPoint=current;
                collisionTarget=!wall&&collided!=null?collided.getUUID():null;
                // Defer the second hit until tick 10; do not reset global invulnerability.
            }
        }
        previous=current;
        if(age==20 && !pendingCollision)finish();
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);tag.putBoolean("Magnetic",magnetic);
        tag.putBoolean("PendingCollision",pendingCollision);
        if(collisionTarget!=null)tag.putUUID("CollisionTarget",collisionTarget);
        tag.putDouble("CX",collisionPoint.x);tag.putDouble("CY",collisionPoint.y);tag.putDouble("CZ",collisionPoint.z);
        tag.putDouble("PX",previous.x);tag.putDouble("PY",previous.y);tag.putDouble("PZ",previous.z);
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);magnetic=tag.getBoolean("Magnetic");
        pendingCollision=tag.getBoolean("PendingCollision");
        collisionTarget=tag.hasUUID("CollisionTarget")?tag.getUUID("CollisionTarget"):null;
        collisionPoint=new Vec3(tag.getDouble("CX"),tag.getDouble("CY"),tag.getDouble("CZ"));
        previous=new Vec3(tag.getDouble("PX"),tag.getDouble("PY"),tag.getDouble("PZ"));
    }
}

