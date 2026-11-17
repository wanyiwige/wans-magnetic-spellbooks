package cn.wanyiwige.magneticspellbooks.entities;

import cn.wanyiwige.magneticspellbooks.registry.ModEntities;
import cn.wanyiwige.magneticspellbooks.util.MagneticSpellUtil;
import com.github.alexmodguy.alexscaves.server.entity.living.FerrouslimeEntity;
import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

import java.util.UUID;
import java.util.Comparator;

public final class SummonedFerrouslimeEntity extends FerrouslimeEntity {
    private UUID ownerUUID;
    private int remainingLife;
    private boolean splitting;

    public SummonedFerrouslimeEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        goalSelector.removeAllGoals(goal -> goal.getClass().getName().endsWith("FerrouslimeEntity$FormGoal"));
    }

    public void configure(LivingEntity owner, int lifetime, int heads) {
        ownerUUID = owner.getUUID();
        remainingLife = lifetime;
        setHeadCount(Math.max(1, heads));
        setHealth(getMaxHealth());
        setPersistenceRequired();
    }

    private void configure(UUID owner, int lifetime, int heads) {
        ownerUUID = owner;
        remainingLife = lifetime;
        setHeadCount(Math.max(1, heads));
        setHealth(getMaxHealth());
        setPersistenceRequired();
    }

    public LivingEntity getSummoner() {
        if (ownerUUID != null && level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(ownerUUID);
            return entity instanceof LivingEntity living ? living : null;
        }
        return null;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }
        LivingEntity owner = getSummoner();
        if (owner == null || !owner.isAlive() || --remainingLife <= 0) {
            discard();
            return;
        }

        LivingEntity orderedTarget = owner.getLastHurtMob();
        if (!MagneticSpellUtil.canTargetEnemy(owner, orderedTarget)) {
            orderedTarget = owner.getLastHurtByMob();
        }
        if (MagneticSpellUtil.canTargetEnemy(owner, orderedTarget)) {
            setTarget(orderedTarget);
        } else if (!MagneticSpellUtil.canTargetEnemy(owner, getTarget())) {
            setTarget(null);
        }

        if (getTarget() == null && distanceToSqr(owner) > 36.0D) {
            getNavigation().moveTo(owner, 1.1D);
        }
        if (tickCount % 20 == 0) {
            tryCohesionMerge();
        }
    }

    private void tryCohesionMerge() {
        SummonedFerrouslimeEntity other = level().getEntitiesOfClass(SummonedFerrouslimeEntity.class,
                        getBoundingBox().inflate(12.0D), slime -> slime != this && slime.isAlive()
                                && ownerUUID != null && ownerUUID.equals(slime.ownerUUID))
                .stream()
                .min(Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);
        if (other == null) return;
        if (distanceToSqr(other) > 2.25D) {
            if (getId() < other.getId()) getNavigation().moveTo(other, 1.0D);
            return;
        }
        if (getId() > other.getId()) return;

        setHeadCount(getHeadCount() + other.getHeadCount());
        remainingLife = Math.max(remainingLife, other.remainingLife);
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ACParticleRegistry.SCARLET_SHIELD_LIGHTNING.get(),
                    getX(), getY() + 0.5D, getZ(), 0, other.getX(), other.getY() + 0.5D, other.getZ(), 1.0D);
            serverLevel.sendParticles(ACParticleRegistry.AZURE_SHIELD_LIGHTNING.get(),
                    other.getX(), other.getY() + 0.5D, other.getZ(), 0, getX(), getY() + 0.5D, getZ(), 1.0D);
        }
        playSound(ACSoundRegistry.FERROUSLIME_COMBINE.get(), 1.0F, 1.0F);
        other.discard();
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        LivingEntity owner = getSummoner();
        return entity == owner || owner != null && owner.isAlliedTo(entity) || super.isAlliedTo(entity);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        LivingEntity owner = getSummoner();
        return owner != null && MagneticSpellUtil.canTargetEnemy(owner, target) && super.canAttack(target);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!splitting && isDeadOrDying() && getHeadCount() >= 2 && level() instanceof ServerLevel) {
            splitting = true;
            int firstHeads = getHeadCount() / 2;
            int secondHeads = getHeadCount() - firstHeads;
            spawnSummonedSplit(firstHeads, -0.35D);
            spawnSummonedSplit(secondHeads, 0.35D);
            setHeadCount(1);
        }
        super.remove(reason);
    }

    private void spawnSummonedSplit(int heads, double offset) {
        SummonedFerrouslimeEntity child = ModEntities.SUMMONED_FERROUSLIME.get().create(level());
        if (child != null && ownerUUID != null) {
            child.setPos(getX() + offset, getY(), getZ() - offset);
            child.configure(ownerUUID, Math.max(1, remainingLife / 2), heads);
            level().addFreshEntity(child);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUUID != null) tag.putUUID("SpellOwner", ownerUUID);
        tag.putInt("RemainingLife", remainingLife);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ownerUUID = tag.hasUUID("SpellOwner") ? tag.getUUID("SpellOwner") : null;
        remainingLife = tag.getInt("RemainingLife");
    }
}
