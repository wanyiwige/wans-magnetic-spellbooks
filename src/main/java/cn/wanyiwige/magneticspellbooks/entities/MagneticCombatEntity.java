package cn.wanyiwige.magneticspellbooks.entities;

import cn.wanyiwige.magneticspellbooks.util.MagneticCombatSupport;
import cn.wanyiwige.magneticspellbooks.util.MagneticSpellUtil;
import cn.wanyiwige.magneticspellbooks.util.MagnetizationSynergyUtil;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import java.util.UUID;

/** Common synchronization and lifetime only; every spell owns its damage timeline. */
public abstract class MagneticCombatEntity extends Entity {
    private static final EntityDataAccessor<Integer> AGE = SynchedEntityData.defineId(MagneticCombatEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<CompoundTag> VISUAL = SynchedEntityData.defineId(MagneticCombatEntity.class, EntityDataSerializers.COMPOUND_TAG);
    protected UUID ownerUUID, targetUUID;
    protected int age, spellLevel = 1, maximumAge = 100;
    protected float power = 1;
    protected Vec3 initialTarget = Vec3.ZERO;
    protected boolean finished;
    public MagneticCombatEntity(EntityType<?> type, Level level) {
        super(type, level); noPhysics = true; setNoGravity(true);
    }
    public void configure(LivingEntity owner, LivingEntity target, int spellLevel, float power) {
        ownerUUID = owner.getUUID(); targetUUID = target == null ? null : target.getUUID();
        this.spellLevel = spellLevel; this.power = power;
        initialTarget = target == null ? position() : MagneticCombatSupport.center(target);
        CompoundTag visual = getVisual().copy(); visual.putInt("Level", spellLevel);
        visual.putInt("OwnerId", owner.getId()); visual.putInt("TargetId", target == null ? -1 : target.getId());
        setVisual(visual);
    }
    public void afterSpawn(LivingEntity owner) {
        LivingEntity target = primary();
        if (target != null) MagnetizationSynergyUtil.reserveForAttack(owner, target, this);
    }
    public CompoundTag getVisual() { return entityData.get(VISUAL); }
    protected void setVisual(CompoundTag tag) { entityData.set(VISUAL, tag); }
    public float renderAge(float partial) { return entityData.get(AGE) + partial; }
    public UUID getOwnerUUID() { return ownerUUID; }
    public int getSpellLevel() { return getVisual().getInt("Level"); }
    public boolean isFinished() { return getVisual().getBoolean("Finished"); }
    protected LivingEntity living(UUID id) {
        if (id == null || !(level() instanceof ServerLevel server)) return null;
        Entity entity = server.getEntity(id);
        return entity instanceof LivingEntity living && living.isAlive() ? living : null;
    }
    protected LivingEntity primary() { return living(targetUUID); }
    protected boolean validPrimary(LivingEntity owner, LivingEntity target) {
        return MagneticSpellUtil.canTargetEnemy(owner, target)
                && MagneticCombatSupport.visible(level(), owner.getEyePosition(), target);
    }
    protected float reward(LivingEntity owner, LivingEntity target) {
        return MagnetizationSynergyUtil.reserveForAttack(owner, target, this).damageMultiplier();
    }
    protected int effectiveStrength(LivingEntity owner, LivingEntity target) {
        return MagnetizationSynergyUtil.reserveForAttack(owner, target, this).effectiveStrength();
    }
    protected void hit(AbstractSpell spell, LivingEntity owner, LivingEntity target, float damage) {
        // Iron's API takes the direct spell entity first and the responsible caster second.
        if (damage > 0 && MagneticSpellUtil.canTargetEnemy(owner, target)) target.hurt(spell.getDamageSource(this, owner), damage);
    }
    protected void finish() {
        finished = true;
        CompoundTag visual = getVisual().copy(); visual.putBoolean("Finished", true);
        visual.putInt("FinishAge", age); setVisual(visual);
    }
    @Override public void tick() {
        super.tick(); if (level().isClientSide) return;
        age++; entityData.set(AGE, age);
        LivingEntity owner = living(ownerUUID);
        if (owner == null || age > maximumAge || !(level() instanceof ServerLevel server)) { discard(); return; }
        LivingEntity target = primary();
        if (target != null && getVisual().getInt("TargetId") != target.getId()) {
            CompoundTag visual = getVisual().copy(); visual.putInt("TargetId", target.getId()); setVisual(visual);
        }
        serverTick(server, owner);
    }
    protected abstract void serverTick(ServerLevel level, LivingEntity owner);
    @Override protected void defineSynchedData() { entityData.define(AGE, 0); entityData.define(VISUAL, new CompoundTag()); }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) tag.putUUID("Owner", ownerUUID);
        if (targetUUID != null) tag.putUUID("Target", targetUUID);
        tag.putInt("Age", age); tag.putInt("Level", spellLevel); tag.putFloat("Power", power);
        tag.putInt("MaxAge", maximumAge); tag.putBoolean("Finished", finished);
        tag.putDouble("InitialX", initialTarget.x); tag.putDouble("InitialY", initialTarget.y); tag.putDouble("InitialZ", initialTarget.z);
        tag.put("Visual", getVisual().copy());
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        ownerUUID = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        targetUUID = tag.hasUUID("Target") ? tag.getUUID("Target") : null;
        age = tag.getInt("Age"); spellLevel = tag.getInt("Level"); power = tag.getFloat("Power");
        maximumAge = tag.getInt("MaxAge"); finished = tag.getBoolean("Finished");
        initialTarget = new Vec3(tag.getDouble("InitialX"), tag.getDouble("InitialY"), tag.getDouble("InitialZ"));
        entityData.set(AGE, age); setVisual(tag.getCompound("Visual"));
    }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}

