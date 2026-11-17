package cn.wanyiwige.magneticspellbooks.entities;

import cn.wanyiwige.magneticspellbooks.registry.ModEffects;
import cn.wanyiwige.magneticspellbooks.registry.ModSpells;
import cn.wanyiwige.magneticspellbooks.util.MagneticSpellUtil;
import cn.wanyiwige.magneticspellbooks.util.MagnetizationSynergyUtil;
import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.Comparator;
import java.util.List;

/** A flying Tesla bulb that anchors on impact, charges with sparse white arcs, then detonates once. */
public final class TeslaArcNodeEntity extends Projectile {
    private static final EntityDataAccessor<Boolean> CHARGING = SynchedEntityData.defineId(
            TeslaArcNodeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int MAX_FLIGHT_TICKS = 80;
    private static final int CHARGE_ARC_COOLDOWN = 15;

    private int remainingChargeTicks;
    private int totalChargeTicks;
    private float radius;
    private float spellDamage;
    private float explosionPower;
    private int maximumTargets;
    private int chargeArcCooldown;

    public TeslaArcNodeEntity(EntityType<? extends TeslaArcNodeEntity> type, Level level) {
        super(type, level);
    }

    public void configure(LivingEntity owner, int chargeTicks, float radius, float damage,
                          int targets, float explosionPower) {
        setOwner(owner);
        totalChargeTicks = Math.max(20, chargeTicks);
        remainingChargeTicks = totalChargeTicks;
        this.radius = radius;
        spellDamage = damage;
        maximumTargets = Mth.clamp(targets, 1, 2);
        this.explosionPower = explosionPower;
    }

    public boolean isCharging() {
        return entityData.get(CHARGING);
    }

    @Override
    public void tick() {
        super.tick();
        if (isCharging()) {
            setDeltaMovement(Vec3.ZERO);
            if (!level().isClientSide && level() instanceof ServerLevel serverLevel) {
                tickCharging(serverLevel);
            }
            return;
        }

        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hit.getType() != HitResult.Type.MISS) {
            onHit(hit);
            if (isCharging()) {
                return;
            }
        }

        checkInsideBlocks();
        Vec3 velocity = getDeltaMovement();
        setPos(getX() + velocity.x, getY() + velocity.y, getZ() + velocity.z);
        ProjectileUtil.rotateTowardsMovement(this, 0.2F);
        setDeltaMovement(velocity.scale(0.99D).add(0.0D, -0.025D, 0.0D));

        if (!level().isClientSide && tickCount >= MAX_FLIGHT_TICKS) {
            beginCharging(position());
        }
    }

    private void tickCharging(ServerLevel level) {
        if (--remainingChargeTicks <= 0) {
            detonate(level);
            return;
        }
        if (chargeArcCooldown > 0) {
            chargeArcCooldown--;
        } else {
            chargeArcCooldown = CHARGE_ARC_COOLDOWN;
            if (random.nextFloat() < 0.4F) {
                emitChargeArc(level);
            }
        }
    }

    private void emitChargeArc(ServerLevel level) {
        Vec3 start = position().add(0.0D, 0.7D, 0.0D);
        double angle = (totalChargeTicks - remainingChargeTicks) * 1.37D + getId();
        double distance = 1.15D + random.nextDouble() * 0.85D;
        Vec3 end = start.add(Math.cos(angle) * distance,
                -0.25D + random.nextDouble() * 1.5D,
                Math.sin(angle) * distance);
        sendEndpointParticle(level, ACParticleRegistry.TESLA_BULB_LIGHTNING.get(), start, end);
    }

    private void detonate(ServerLevel level) {
        Entity ownerEntity = getOwner();
        if (ownerEntity instanceof LivingEntity owner && owner.isAlive()) {
            strikeTargets(level, owner);
        }
        if (ownerEntity instanceof MagneticMageEntity) {
            // Damage already comes from the filtered arc above. Vanilla explosion
            // damage ignores our neutral-target filter and would hurt bystanders.
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                    getX(), getY() + 0.45D, getZ(), 1, 0, 0, 0, 0);
            level.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,
                    net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 1.0F);
        } else {
            level.explode(ownerEntity, getX(), getY() + 0.45D, getZ(), explosionPower,
                    false, Level.ExplosionInteraction.NONE);
        }
        discard();
    }

    private void strikeTargets(ServerLevel level, LivingEntity owner) {
        double radiusSqr = radius * radius;
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius),
                        entity -> MagneticSpellUtil.canTargetEnemy(owner, entity)
                                && entity.distanceToSqr(this) <= radiusSqr
                                && hasLineOfSight(level, entity))
                .stream()
                .sorted(Comparator
                        .comparing((LivingEntity entity) -> !entity.hasEffect(ModEffects.NOTOR_SCANNED.get())
                                && !entity.hasEffect(com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry.MAGNETIZING.get()))
                        .thenComparingDouble(entity -> entity.distanceToSqr(this)))
                .limit(maximumTargets)
                .toList();

        Vec3 arcStart = position().add(0.0D, 0.75D, 0.0D);
        boolean primary = true;
        for (LivingEntity target : targets) {
            // Bind at detonation: the flying bulb has no primary target until now.
            float damage = primary
                    ? MagnetizationSynergyUtil.applyPrimaryReward(spellDamage, owner, target, this)
                    : spellDamage;
            primary = false;
            target.hurt(ModSpells.TESLA_ARC_NODE.get().getDamageSource(this, owner), damage);
            Vec3 end = target.getEyePosition();
            sendEndpointParticle(level, ACParticleRegistry.TESLA_BULB_LIGHTNING.get(), arcStart, end);
            arcStart = end;
        }
    }

    private boolean hasLineOfSight(ServerLevel level, LivingEntity target) {
        return level.clip(new ClipContext(position().add(0.0D, 0.75D, 0.0D), target.getEyePosition(),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity)) {
            return false;
        }
        Entity ownerEntity = getOwner();
        return !(ownerEntity instanceof LivingEntity owner && entity instanceof LivingEntity living
                && !MagneticSpellUtil.canTargetEnemy(owner, living));
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (!level().isClientSide) {
            beginCharging(hit.getLocation());
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        if (!level().isClientSide) {
            Vec3 anchored = hit.getLocation().add(Vec3.atLowerCornerOf(hit.getDirection().getNormal()).scale(0.08D));
            beginCharging(anchored);
        }
    }

    private void beginCharging(Vec3 anchoredPosition) {
        if (isCharging()) {
            return;
        }
        entityData.set(CHARGING, true);
        setNoGravity(true);
        noPhysics = true;
        setDeltaMovement(Vec3.ZERO);
        setPos(anchoredPosition.x, anchoredPosition.y, anchoredPosition.z);
        if (remainingChargeTicks <= 0) {
            remainingChargeTicks = Math.max(20, totalChargeTicks);
        }
    }

    private static void sendEndpointParticle(ServerLevel level, ParticleOptions particle, Vec3 start, Vec3 end) {
        // TeslaBulbBlockEntity sends the endpoint as a relative vector, not world coordinates.
        Vec3 offset = end.subtract(start);
        level.sendParticles(particle, start.x, start.y, start.z, 0,
                offset.x, offset.y, offset.z, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(CHARGING, false);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(CHARGING, tag.getBoolean("Charging"));
        remainingChargeTicks = tag.getInt("RemainingChargeTicks");
        totalChargeTicks = tag.getInt("TotalChargeTicks");
        radius = tag.getFloat("Radius");
        spellDamage = tag.getFloat("SpellDamage");
        explosionPower = tag.getFloat("ExplosionPower");
        maximumTargets = Mth.clamp(tag.getInt("MaximumTargets"), 1, 2);
        chargeArcCooldown = tag.getInt("ChargeArcCooldown");
        if (isCharging()) {
            setNoGravity(true);
            noPhysics = true;
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Charging", isCharging());
        tag.putInt("RemainingChargeTicks", remainingChargeTicks);
        tag.putInt("TotalChargeTicks", totalChargeTicks);
        tag.putFloat("Radius", radius);
        tag.putFloat("SpellDamage", spellDamage);
        tag.putFloat("ExplosionPower", explosionPower);
        tag.putInt("MaximumTargets", maximumTargets);
        tag.putInt("ChargeArcCooldown", chargeArcCooldown);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
