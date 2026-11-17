package cn.wanyiwige.magneticspellbooks.entities;

import cn.wanyiwige.magneticspellbooks.registry.ModSpells;
import cn.wanyiwige.magneticspellbooks.util.MagneticSpellUtil;
import cn.wanyiwige.magneticspellbooks.util.MagnetizationSynergyUtil;
import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

public final class HeavyweightSpellEntity extends Entity {
    private UUID ownerUUID;
    private UUID trackedTargetUUID;
    private float spellDamage;
    private float impactRadius;

    public HeavyweightSpellEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    public void configure(LivingEntity owner, LivingEntity trackedTarget, float damage, float radius) {
        ownerUUID = owner.getUUID();
        trackedTargetUUID = trackedTarget == null ? null : trackedTarget.getUUID();
        spellDamage = damage;
        impactRadius = radius;
        setDeltaMovement(0.0D, -0.32D, 0.0D);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }
        if (!(level() instanceof ServerLevel serverLevel) || tickCount > 100) {
            discard();
            return;
        }

        if (trackedTargetUUID != null && tickCount <= 20) {
            Entity tracked = serverLevel.getEntity(trackedTargetUUID);
            if (tracked instanceof LivingEntity living && living.isAlive()
                    && living.hasEffect(ACEffectRegistry.MAGNETIZING.get())) {
                Vec3 correction = living.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
                if (correction.lengthSqr() > 0.04D) {
                    Vec3 horizontal = correction.normalize().scale(0.08D);
                    setDeltaMovement(horizontal.x, getDeltaMovement().y, horizontal.z);
                }
            }
        }

        Vec3 movement = getDeltaMovement().add(0.0D, -0.055D, 0.0D);
        setDeltaMovement(movement.x * 0.98D, Math.max(movement.y, -1.15D), movement.z * 0.98D);
        move(MoverType.SELF, getDeltaMovement());
        setXRot(getXRot() + 24.0F);

        if (onGround() || verticalCollision || tickCount >= 80) {
            impact(serverLevel);
        }
    }

    private void impact(ServerLevel level) {
        Entity ownerEntity = ownerUUID == null ? null : level.getEntity(ownerUUID);
        if (ownerEntity instanceof LivingEntity owner && owner.isAlive()) {
            double radiusSqr = impactRadius * impactRadius;
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                    getBoundingBox().inflate(impactRadius), entity ->
                            MagneticSpellUtil.canTargetEnemy(owner, entity) && entity.distanceToSqr(this) <= radiusSqr)) {
                boolean magnetized = target.hasEffect(ACEffectRegistry.MAGNETIZING.get());
                float damage = magnetized ? spellDamage * 1.5F : spellDamage;
                // Only the explicitly tracked primary gets the scan bonus.
                if (trackedTargetUUID != null && target.getUUID().equals(trackedTargetUUID)) {
                    damage = MagnetizationSynergyUtil.applyPrimaryReward(damage, owner, target, this);
                }
                target.hurt(ModSpells.BOUNDROID_DROP.get().getDamageSource(this, owner), damage);
                Vec3 knock = target.position().subtract(position()).multiply(1.0D, 0.35D, 1.0D);
                if (knock.lengthSqr() > 1.0E-4D) {
                    target.setDeltaMovement(target.getDeltaMovement().add(knock.normalize().scale(0.35D)));
                    target.hurtMarked = true;
                }
            }
        }

        level.playSound(null, blockPosition(), ACSoundRegistry.BOUNDROID_SLAM.get(),
                SoundSource.PLAYERS, 1.5F, 0.9F + random.nextFloat() * 0.15F);
        level.sendParticles(ACParticleRegistry.GALENA_DEBRIS.get(), getX(), getY() + 0.2D, getZ(),
                24, impactRadius * 0.45D, 0.25D, impactRadius * 0.45D, 0.08D);
        spawnShockwave(level);
        discard();
    }

    private void spawnShockwave(ServerLevel level) {
        int points = 28;
        double radius = Math.min(3.0D, Math.max(1.6D, impactRadius * 0.8D));
        for (int i = 0; i < points; i++) {
            double angle = Math.PI * 2.0D * i / points;
            double directionX = Math.cos(angle);
            double directionZ = Math.sin(angle);
            double particleX = getX() + directionX * radius;
            double particleZ = getZ() + directionZ * radius;
            level.sendParticles(ParticleTypes.CLOUD, particleX, getY() + 0.12D, particleZ,
                    0, directionX, 0.035D, directionZ, 0.18D);
            if ((i & 1) == 0) {
                level.sendParticles(ACParticleRegistry.GALENA_DEBRIS.get(), particleX, getY() + 0.16D, particleZ,
                        1, 0.08D, 0.04D, 0.08D, 0.02D);
            }
        }
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        ownerUUID = tag.hasUUID("SpellOwner") ? tag.getUUID("SpellOwner") : null;
        trackedTargetUUID = tag.hasUUID("TrackedTarget") ? tag.getUUID("TrackedTarget") : null;
        spellDamage = tag.getFloat("SpellDamage");
        impactRadius = tag.getFloat("ImpactRadius");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) tag.putUUID("SpellOwner", ownerUUID);
        if (trackedTargetUUID != null) tag.putUUID("TrackedTarget", trackedTargetUUID);
        tag.putFloat("SpellDamage", spellDamage);
        tag.putFloat("ImpactRadius", impactRadius);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
