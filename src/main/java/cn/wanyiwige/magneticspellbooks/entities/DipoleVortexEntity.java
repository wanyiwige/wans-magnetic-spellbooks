package cn.wanyiwige.magneticspellbooks.entities;

import cn.wanyiwige.magneticspellbooks.registry.ModSpells;
import cn.wanyiwige.magneticspellbooks.util.MagneticSpellUtil;
import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

public final class DipoleVortexEntity extends Entity {
    private static final EntityDataAccessor<Float> FIELD_RADIUS = SynchedEntityData.defineId(
            DipoleVortexEntity.class, EntityDataSerializers.FLOAT);
    private static final int DAMAGE_INTERVAL = 10;
    private static final int MAX_TARGET_ARCS = 2;
    private UUID ownerUUID;
    private int remainingLife;
    private float radius;
    private float force;
    private float pulseDamage;

    public DipoleVortexEntity(EntityType<?> type, Level level) {
        super(type, level);
        setNoGravity(true);
        noPhysics = true;
    }

    public void configure(LivingEntity owner, int lifetime, float radius, float force, float damage) {
        ownerUUID = owner.getUUID();
        remainingLife = lifetime;
        this.radius = radius;
        entityData.set(FIELD_RADIUS, radius);
        this.force = force;
        pulseDamage = damage;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (!(level() instanceof ServerLevel serverLevel) || --remainingLife <= 0) {
            discard();
            return;
        }
        Entity ownerEntity = ownerUUID == null ? null : serverLevel.getEntity(ownerUUID);
        if (!(ownerEntity instanceof LivingEntity owner) || !owner.isAlive()) {
            discard();
            return;
        }

        Vec3[] poles = getPolePositions(0.0F);
        if (tickCount % DAMAGE_INTERVAL == 0) {
            controlTargets(serverLevel, owner, poles[0], poles[1]);
        }
        if (tickCount % 12 == 0) {
            showField(serverLevel, poles[0], poles[1]);
        }
    }

    public Vec3[] getPolePositions(float partialTick) {
        float fieldRadius = getFieldRadius();
        double angle = (tickCount + partialTick) * 0.11D;
        // Damage covers the whole field; the cores themselves stay on a compact orbit.
        double poleRadius = Math.min(3.0D, fieldRadius * 0.27D);
        Vec3 offset = new Vec3(Math.cos(angle) * poleRadius, 0.75D, Math.sin(angle) * poleRadius);
        return new Vec3[]{position().add(offset), position().add(-offset.x, offset.y, -offset.z)};
    }

    public float getFieldRadius() {
        return entityData.get(FIELD_RADIUS);
    }

    private void controlTargets(ServerLevel level, LivingEntity owner, Vec3 scarlet, Vec3 azure) {
        double radiusSqr = radius * radius;
        int targetArcs = 0;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius),
                entity -> MagneticSpellUtil.canMagnetize(owner, entity) && entity.distanceToSqr(this) <= radiusSqr)) {
            target.addEffect(new MobEffectInstance(ACEffectRegistry.MAGNETIZING.get(), 30, 0, false, false, false));
            Vec3 targetCenter = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
            Vec3 redPull = scarlet.subtract(targetCenter);
            Vec3 bluePush = targetCenter.subtract(azure);
            double redWeight = 1.0D - Math.min(1.0D, redPull.length() / radius);
            double blueWeight = 1.0D - Math.min(1.0D, bluePush.length() / radius);
            Vec3 combined = safeNormalize(redPull).scale(0.65D + redWeight)
                    .add(safeNormalize(bluePush).scale(0.65D + blueWeight));
            MagneticSpellUtil.addMagneticImpulse(target, combined, force);
            target.hurt(ModSpells.DIPOLE_VORTEX.get().getDamageSource(this, owner), pulseDamage);

            if (targetArcs++ < MAX_TARGET_ARCS) {
                if (redWeight >= blueWeight) {
                    sendEndpointParticle(level, ACParticleRegistry.SCARLET_SHIELD_LIGHTNING.get(), scarlet, targetCenter);
                } else {
                    sendEndpointParticle(level, ACParticleRegistry.AZURE_SHIELD_LIGHTNING.get(), azure, targetCenter);
                }
            }
        }
    }

    private static Vec3 safeNormalize(Vec3 vector) {
        return vector.lengthSqr() < 1.0E-6D ? Vec3.ZERO : vector.normalize();
    }

    private void showField(ServerLevel level, Vec3 scarlet, Vec3 azure) {
        showPoleRing(level, scarlet, ACParticleRegistry.SCARLET_SHIELD_LIGHTNING.get(), true);
        showPoleRing(level, azure, ACParticleRegistry.AZURE_SHIELD_LIGHTNING.get(), false);
        // These particles also expect an absolute orbit center, not a relative velocity.
        sendEndpointParticle(level, ACParticleRegistry.SCARLET_MAGNETIC_ORBIT.get(), scarlet, scarlet);
        sendEndpointParticle(level, ACParticleRegistry.AZURE_MAGNETIC_ORBIT.get(), azure, azure);
    }

    private void showPoleRing(ServerLevel level, Vec3 pole, ParticleOptions particle, boolean reverse) {
        double ringRadius = 1.0D + radius * 0.035D;
        double direction = reverse ? -1.0D : 1.0D;
        double baseAngle = tickCount * 0.13D * direction;
        Vec3 start = pole.add(Math.cos(baseAngle) * ringRadius,
                Math.sin(baseAngle * 2.0D) * 0.32D, Math.sin(baseAngle) * ringRadius);
        double nextAngle = baseAngle + direction * Math.PI / 3.0D;
        Vec3 end = pole.add(Math.cos(nextAngle) * ringRadius,
                Math.sin(nextAngle * 2.0D) * 0.32D, Math.sin(nextAngle) * ringRadius);
        sendEndpointParticle(level, particle, start, end);
    }

    private static void sendEndpointParticle(ServerLevel level, ParticleOptions particle, Vec3 start, Vec3 end) {
        // Alex's Caves shield lightning/orbit particles interpret these fields as a WORLD endpoint.
        Vec3 endpoint = end;
        level.sendParticles(particle, start.x, start.y, start.z, 0,
                endpoint.x, endpoint.y, endpoint.z, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(FIELD_RADIUS, 5.0F);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        ownerUUID = tag.hasUUID("SpellOwner") ? tag.getUUID("SpellOwner") : null;
        remainingLife = tag.getInt("RemainingLife");
        radius = tag.getFloat("Radius");
        entityData.set(FIELD_RADIUS, radius);
        force = tag.getFloat("Force");
        pulseDamage = tag.getFloat("PulseDamage");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) tag.putUUID("SpellOwner", ownerUUID);
        tag.putInt("RemainingLife", remainingLife);
        tag.putFloat("Radius", radius);
        tag.putFloat("Force", force);
        tag.putFloat("PulseDamage", pulseDamage);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
