package cn.wanyiwige.magneticspellbooks.client;

import cn.wanyiwige.magneticspellbooks.util.MagneticSpellUtil;
import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Client-only reuse of Alex's Caves red/blue magnetic particle effects. */
@OnlyIn(Dist.CLIENT)
public final class MagneticSpellEffects {
    private MagneticSpellEffects() {
    }

    public static void spawnMagnetizeArea(Level level, LivingEntity caster, int spellLevel) {
        double radius = 4.0D + spellLevel;
        double radiusSqr = radius * radius;
        Vec3 source = caster.getEyePosition().add(caster.getLookAngle().scale(0.3D));
        int affected = 0;
        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                caster.getBoundingBox().inflate(radius),
                entity -> MagneticSpellUtil.canMagnetize(caster, entity)
                        && entity.distanceToSqr(caster) <= radiusSqr)) {
            Vec3 center = target.getEyePosition().add(0.0D, -target.getBbHeight() * 0.25D, 0.0D);
            spawnArcPair(level, source, center);
            spawnMagnetizedStatus(level, target);
            if (++affected >= 24) {
                break;
            }
        }

        for (int i = 0; i < 12; i++) {
            double angle = Mth.TWO_PI * i / 12.0D;
            Vec3 edge = caster.position().add(Math.cos(angle) * radius, 0.2D, Math.sin(angle) * radius);
            level.addParticle((i & 1) == 0
                            ? ACParticleRegistry.SCARLET_SHIELD_LIGHTNING.get()
                            : ACParticleRegistry.AZURE_SHIELD_LIGHTNING.get(),
                    edge.x, edge.y, edge.z, caster.getX(), caster.getY() + 0.5D, caster.getZ());
        }
    }

    public static void spawnMagnetizedStatus(Level level, LivingEntity target) {
        Vec3 center = target.getEyePosition().add(0.0D, -target.getBbHeight() * 0.3D, 0.0D);
        double angle = target.tickCount * 0.24D;
        double offset = Math.max(0.65D, target.getBbWidth() * 0.7D);
        Vec3 scarlet = center.add(Math.cos(angle) * offset, 0.1D, Math.sin(angle) * offset);
        Vec3 azure = center.add(-Math.cos(angle) * offset, -0.1D, -Math.sin(angle) * offset);
        level.addParticle(ACParticleRegistry.SCARLET_MAGNETIC_ORBIT.get(),
                scarlet.x, scarlet.y, scarlet.z, center.x, center.y, center.z);
        level.addParticle(ACParticleRegistry.AZURE_MAGNETIC_ORBIT.get(),
                azure.x, azure.y, azure.z, center.x, center.y, center.z);
        if (target.tickCount % 20 == 0) {
            level.addParticle(ACParticleRegistry.MAGNET_LIGHTNING.get(),
                    center.x, center.y, center.z, 0.0D, 0.0D, 0.0D);
        }
    }

    public static void spawnBipolarArcs(Level level, LivingEntity caster, int spellLevel) {
        Vec3 start = caster.getEyePosition().add(caster.getLookAngle().scale(0.35D));
        double range = 10.0D + spellLevel * 1.5D;
        LivingEntity target = MagneticSpellUtil.findLivingTarget(level, caster, range, entity -> true);

        if (target != null) {
            spawnArcPair(level, start, target.getEyePosition());
            return;
        }

        double radius = Mth.clamp(2.8D + spellLevel * 0.25D, 3.0D, 5.5D);
        double rotation = caster.getRandom().nextDouble() * Mth.TWO_PI;
        for (int i = 0; i < 3; i++) {
            double angle = rotation + Mth.TWO_PI * i / 3.0D;
            Vec3 end = caster.position().add(
                    Math.cos(angle) * radius,
                    0.7D + caster.getRandom().nextDouble() * 1.2D,
                    Math.sin(angle) * radius);
            spawnArcPair(level, start, end);
        }
    }

    public static void spawnManipulationArc(Level level, LivingEntity caster, int spellLevel, boolean attraction) {
        double range = 8.0D + spellLevel * 0.8D;
        LivingEntity target = MagneticSpellUtil.findLivingTarget(level, caster, range,
                entity -> MagneticSpellUtil.canManipulate(caster, entity));
        if (target == null) {
            spawnBipolarArcs(level, caster, spellLevel);
            return;
        }

        Vec3 casterPoint = caster.getEyePosition().add(caster.getLookAngle().scale(0.35D));
        Vec3 targetPoint = target.getEyePosition().add(0.0D, -target.getBbHeight() * 0.25D, 0.0D);
        spawnArcPair(level, attraction ? targetPoint : casterPoint, attraction ? casterPoint : targetPoint);

        level.addParticle(attraction
                        ? ACParticleRegistry.SCARLET_MAGNETIC_ORBIT.get()
                        : ACParticleRegistry.AZURE_MAGNETIC_ORBIT.get(),
                targetPoint.x + 0.8D, targetPoint.y, targetPoint.z,
                targetPoint.x, targetPoint.y, targetPoint.z);
    }

    public static void spawnLevitationField(Level level, LivingEntity caster) {
        Vec3 feet = caster.position().add(0.0D, 0.15D, 0.0D);
        Vec3 upper = caster.position().add(0.0D, caster.getBbHeight() + 0.35D, 0.0D);
        level.addParticle(ACParticleRegistry.AZURE_MAGNETIC_ORBIT.get(),
                feet.x + 0.9D, feet.y, feet.z, feet.x, feet.y, feet.z);
        level.addParticle(ACParticleRegistry.SCARLET_MAGNETIC_ORBIT.get(),
                upper.x + 0.9D, upper.y, upper.z, upper.x, upper.y, upper.z);
        spawnArcPair(level, feet, upper);
    }

    public static void spawnNotorScan(Level level, LivingEntity caster, int spellLevel) {
        double range = 14.0D + spellLevel * 3.0D;
        LivingEntity target = MagneticSpellUtil.findLivingTarget(level, caster, range,
                entity -> MagneticSpellUtil.canTargetEnemy(caster, entity));
        Vec3 start = caster.getEyePosition().add(caster.getLookAngle().scale(0.35D));
        Vec3 end = target == null ? start.add(caster.getLookAngle().scale(range)) : target.getEyePosition();
        spawnArcPair(level, start, end);
        Vec3 side = caster.getLookAngle().cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (side.lengthSqr() > 1.0E-4D) {
            side = side.normalize().scale(0.16D);
            spawnFlowPair(level, start.add(side), end.add(side));
            spawnFlowPair(level, start.subtract(side), end.subtract(side));
        }
        if (target != null) {
            spawnNotorStatus(level, target);
        }
    }

    public static void spawnNotorStatus(Level level, LivingEntity target) {
        Vec3 center = target.getEyePosition().add(0.0D, 0.3D, 0.0D);
        double radius = Math.max(0.75D, target.getBbWidth() * 0.8D);
        for (int i = 0; i < 6; i++) {
            double angle = Mth.TWO_PI * i / 6.0D + target.tickCount * 0.08D;
            Vec3 point = center.add(Math.cos(angle) * radius, Math.sin(angle * 2.0D) * 0.18D,
                    Math.sin(angle) * radius);
            level.addParticle((i & 1) == 0
                            ? ACParticleRegistry.SCARLET_MAGNETIC_ORBIT.get()
                            : ACParticleRegistry.AZURE_MAGNETIC_ORBIT.get(),
                    point.x, point.y, point.z, center.x, center.y, center.z);
        }
        level.addParticle(ACParticleRegistry.MAGNET_LIGHTNING.get(),
                center.x, center.y, center.z, 0.0D, 0.0D, 0.0D);
    }

    public static void spawnArmament(Level level, LivingEntity caster, int spellLevel) {
        LivingEntity target = MagneticSpellUtil.findBarrageTarget(level, caster, 12.0D + spellLevel);
        Vec3 start = caster.getEyePosition().add(caster.getLookAngle().scale(0.45D));
        Vec3 end = target == null ? start.add(caster.getLookAngle().scale(5.0D)) : target.getEyePosition();
        spawnArcPair(level, start, end);
        spawnArcPair(level, start.add(0.0D, 0.2D, 0.0D), end);
    }

    public static void spawnBoundroidLock(Level level, LivingEntity caster, int spellLevel) {
        double range = 12.0D + spellLevel;
        LivingEntity target = MagneticSpellUtil.findBarrageTarget(level, caster, range);
        Vec3 landing;
        if (target != null) {
            landing = target.position();
        } else {
            Vec3 start = caster.getEyePosition();
            BlockHitResult hit = level.clip(new ClipContext(start, start.add(caster.getLookAngle().scale(range)),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
            landing = hit.getType() == HitResult.Type.MISS ? hit.getLocation() : hit.getLocation();
        }
        Vec3 center = landing.add(0.0D, 0.15D, 0.0D);
        level.addParticle(ACParticleRegistry.SCARLET_MAGNETIC_ORBIT.get(),
                center.x + 1.25D, center.y, center.z, center.x, center.y, center.z);
        level.addParticle(ACParticleRegistry.AZURE_MAGNETIC_ORBIT.get(),
                center.x - 1.25D, center.y, center.z, center.x, center.y, center.z);
        spawnArcPair(level, center.add(0.0D, 6.0D, 0.0D), center);
    }

    public static void spawnFerrouslimeCohesion(Level level, LivingEntity caster, int spellLevel) {
        int count = 2 + (spellLevel - 1) / 2;
        Vec3 previous = null;
        Vec3 first = null;
        for (int i = 0; i < count; i++) {
            double angle = Mth.TWO_PI * i / count;
            Vec3 point = caster.position().add(Math.cos(angle) * 1.6D, 0.45D, Math.sin(angle) * 1.6D);
            level.addParticle(ACParticleRegistry.FERROUSLIME.get(), point.x, point.y, point.z, 0.0D, 0.04D, 0.0D);
            if (first == null) first = point;
            if (previous != null) spawnFlowPair(level, previous, point);
            previous = point;
        }
        if (previous != null && first != null && count > 2) spawnFlowPair(level, previous, first);
    }

    public static void spawnTeslaNode(Level level, LivingEntity caster, int spellLevel) {
        Vec3 start = caster.getEyePosition();
        Vec3 center = rayEndpoint(level, caster, 12.0D + spellLevel).add(0.0D, 0.65D, 0.0D);
        spawnArcPair(level, start, center);
        level.addParticle(ACParticleRegistry.MAGNET_LIGHTNING.get(),
                center.x, center.y, center.z, 0.0D, 0.0D, 0.0D);
    }

    public static void spawnDipoleVortex(Level level, LivingEntity caster, int spellLevel) {
        Vec3 center = rayEndpoint(level, caster, 10.0D + spellLevel * 2.0D).add(0.0D, 0.8D, 0.0D);
        double radius = (4.0D + spellLevel) * 0.45D;
        Vec3 scarlet = center.add(radius, 0.0D, 0.0D);
        Vec3 azure = center.add(-radius, 0.0D, 0.0D);
        spawnArcPair(level, scarlet, azure);
        level.addParticle(ACParticleRegistry.SCARLET_MAGNETIC_ORBIT.get(),
                scarlet.x, scarlet.y, scarlet.z, center.x, center.y, center.z);
        level.addParticle(ACParticleRegistry.AZURE_MAGNETIC_ORBIT.get(),
                azure.x, azure.y, azure.z, center.x, center.y, center.z);
    }

    private static Vec3 rayEndpoint(Level level, LivingEntity caster, double range) {
        Vec3 start = caster.getEyePosition();
        BlockHitResult hit = level.clip(new ClipContext(start, start.add(caster.getLookAngle().scale(range)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        return hit.getLocation();
    }

    private static void spawnArcPair(Level level, Vec3 start, Vec3 end) {
        // Alex's Caves magnetic particles interpret the velocity fields as an absolute endpoint.
        spawnFlowPair(level, start, end);
        level.addParticle(ACParticleRegistry.SCARLET_SHIELD_LIGHTNING.get(),
                end.x, end.y, end.z, start.x, start.y, start.z);
        level.addParticle(ACParticleRegistry.AZURE_SHIELD_LIGHTNING.get(),
                start.x, start.y, start.z, end.x, end.y, end.z);
    }

    private static void spawnFlowPair(Level level, Vec3 start, Vec3 end) {
        level.addParticle(ACParticleRegistry.SCARLET_MAGNETIC_FLOW.get(),
                end.x, end.y, end.z, start.x, start.y, start.z);
        level.addParticle(ACParticleRegistry.AZURE_MAGNETIC_FLOW.get(),
                start.x, start.y, start.z, end.x, end.y, end.z);
    }
}
