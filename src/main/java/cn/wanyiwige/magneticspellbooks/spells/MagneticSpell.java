package cn.wanyiwige.magneticspellbooks.spells;

import cn.wanyiwige.magneticspellbooks.AlexCavesSpellsbook;
import cn.wanyiwige.magneticspellbooks.client.MagneticSpellEffects;
import cn.wanyiwige.magneticspellbooks.entities.HeavyweightSpellEntity;
import cn.wanyiwige.magneticspellbooks.entities.SpellMagneticWeaponEntity;
import cn.wanyiwige.magneticspellbooks.entities.SummonedFerrouslimeEntity;
import cn.wanyiwige.magneticspellbooks.entities.TeslaArcNodeEntity;
import cn.wanyiwige.magneticspellbooks.entities.DipoleVortexEntity;
import cn.wanyiwige.magneticspellbooks.registry.ModEffects;
import cn.wanyiwige.magneticspellbooks.registry.ModEntities;
import cn.wanyiwige.magneticspellbooks.registry.ModSchools;
import cn.wanyiwige.magneticspellbooks.util.MagneticSpellUtil;
import cn.wanyiwige.magneticspellbooks.util.MagnetizationSynergyUtil;
import cn.wanyiwige.magneticspellbooks.util.TargetingFeedback;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACTagRegistry;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Shared, server-authoritative first implementation for the Magnetic School.
 * Individual registry entries still have their own id, config, rarity, level
 * cap and cast behaviour.  This keeps the initial spell roster maintainable
 * while later entity/VFX work can be split into specialised spell classes.
 */
public class MagneticSpell extends AbstractSpell {
    public enum Behaviour {
        MAGNETIZE, ATTRACT, REPEL, ARMAMENT, LEVITATION, SCAN, BOUNDROID_DROP,
        FERROUSLIME_COHESION, TESLA_ARC_NODE, DIPOLE_VORTEX
    }

    private final ResourceLocation id;
    private final DefaultConfig defaultConfig;
    private final CastType castType;
    private final Behaviour behaviour;

    public MagneticSpell(String name, SpellRarity rarity, int maxLevel, CastType castType,
                         double cooldownSeconds, int baseManaCost, int manaCostPerLevel,
                         int castTime, Behaviour behaviour) {
        this.id = ResourceLocation.fromNamespaceAndPath(AlexCavesSpellsbook.MOD_ID, name);
        this.defaultConfig = new DefaultConfig()
                .setMinRarity(rarity)
                .setSchoolResource(ModSchools.MAGNETIC_RESOURCE)
                .setMaxLevel(maxLevel)
                .setCooldownSeconds(cooldownSeconds)
                .setAllowCrafting(true)
                .build();
        this.castType = castType;
        this.behaviour = behaviour;
        this.baseManaCost = baseManaCost;
        this.manaCostPerLevel = manaCostPerLevel;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = castTime;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return id;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return castType;
    }

    @Override
    public SpellRarity getRarity(int spellLevel) {
        if (behaviour == Behaviour.SCAN) {
            if (spellLevel == 1) return SpellRarity.UNCOMMON;
            if (spellLevel <= 3) return SpellRarity.RARE;
            if (spellLevel == 4) return SpellRarity.EPIC;
            return SpellRarity.LEGENDARY;
        }
        if (getMaxLevel() >= 10) {
            if (spellLevel <= 2) return SpellRarity.COMMON;
            if (spellLevel <= 4) return SpellRarity.UNCOMMON;
            if (spellLevel <= 6) return SpellRarity.RARE;
            if (spellLevel == 7) return SpellRarity.EPIC;
            return SpellRarity.LEGENDARY;
        }
        return super.getRarity(spellLevel);
    }

    @Override
    public int getMinLevelForRarity(SpellRarity rarity) {
        if (behaviour == Behaviour.SCAN) {
            if (rarity == SpellRarity.UNCOMMON) return 1;
            if (rarity == SpellRarity.RARE) return 2;
            if (rarity == SpellRarity.EPIC) return 4;
            if (rarity == SpellRarity.LEGENDARY) return 5;
            return 0;
        }
        if (getMaxLevel() >= 10) {
            return switch (rarity) {
                case COMMON -> 1;
                case UNCOMMON -> 3;
                case RARE -> 5;
                case EPIC -> 7;
                case LEGENDARY -> 8;
            };
        }
        return super.getMinLevelForRarity(rarity);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        if (behaviour != Behaviour.SCAN) return super.getUniqueInfo(spellLevel, caster);
        return List.of(
                Component.translatable("ui.wans_magnetic_spellbooks.notor_scan.range", 14 + spellLevel * 3),
                Component.translatable("ui.wans_magnetic_spellbooks.notor_scan.duration", 6 + spellLevel * 3),
                Component.translatable("ui.wans_magnetic_spellbooks.notor_scan.magnetize"),
                Component.translatable("ui.wans_magnetic_spellbooks.notor_scan.reward"));
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource castSource, MagicData magicData) {
        super.onCast(level, spellLevel, caster, castSource, magicData);
        if (level.isClientSide) {
            return;
        }

        switch (behaviour) {
            case MAGNETIZE -> magnetizeArea(level, caster, spellLevel);
            case ATTRACT -> attract(level, caster, spellLevel);
            case REPEL -> repel(level, caster, spellLevel);
            case ARMAMENT -> castArmament(level, caster, spellLevel);
            case LEVITATION -> caster.addEffect(new MobEffectInstance(ModEffects.MAGNETIC_LEVITATION.get(),
                    spellLevel * 1200, Math.max(0, spellLevel - 1), false, false, false));
            case SCAN -> castNotorScan(level, caster, spellLevel);
            case BOUNDROID_DROP -> castBoundroidDrop(level, caster, spellLevel);
            case FERROUSLIME_COHESION -> castFerrouslimeCohesion(level, caster, spellLevel);
            case TESLA_ARC_NODE -> castTeslaArcNode(level, caster, spellLevel);
            case DIPOLE_VORTEX -> castDipoleVortex(level, caster, spellLevel);
        }
    }

    private void attract(Level level, LivingEntity caster, int spellLevel) {
        double range = 8.0D + spellLevel * 0.8D;
        LivingEntity target = MagneticSpellUtil.findLivingTarget(level, caster, range,
                entity -> MagneticSpellUtil.canTargetEnemy(caster, entity));
        if (!TargetingFeedback.requireTarget(caster, target != null)) return;
        if (target != null) {
            MagneticSpellUtil.applyMagneticForce(target,
                    caster.position().add(0.0D, caster.getBbHeight() * 0.5D, 0.0D).subtract(target.position()),
                    1.0D + spellLevel * 0.12D);
        }
    }

    private void repel(Level level, LivingEntity caster, int spellLevel) {
        double range = 8.0D + spellLevel * 0.8D;
        LivingEntity target = MagneticSpellUtil.findLivingTarget(level, caster, range,
                entity -> MagneticSpellUtil.canTargetEnemy(caster, entity));
        if (!TargetingFeedback.requireTarget(caster, target != null)) return;
        if (target != null) {
            MagneticSpellUtil.applyMagneticForce(target,
                    target.position().subtract(caster.position()),
                    1.1D + spellLevel * 0.13D);
        }
    }

    @Override
    public void onClientCast(Level level, int spellLevel, LivingEntity caster, ICastData castData) {
        super.onClientCast(level, spellLevel, caster, castData);
        switch (behaviour) {
            case MAGNETIZE -> MagneticSpellEffects.spawnMagnetizeArea(level, caster, spellLevel);
            case ATTRACT -> MagneticSpellEffects.spawnManipulationArc(level, caster, spellLevel, true);
            case REPEL -> MagneticSpellEffects.spawnManipulationArc(level, caster, spellLevel, false);
            case ARMAMENT -> MagneticSpellEffects.spawnArmament(level, caster, spellLevel);
            case LEVITATION -> MagneticSpellEffects.spawnLevitationField(level, caster);
            case SCAN -> MagneticSpellEffects.spawnNotorScan(level, caster, spellLevel);
            case BOUNDROID_DROP -> MagneticSpellEffects.spawnBoundroidLock(level, caster, spellLevel);
            case FERROUSLIME_COHESION -> MagneticSpellEffects.spawnFerrouslimeCohesion(level, caster, spellLevel);
            case TESLA_ARC_NODE -> MagneticSpellEffects.spawnTeslaNode(level, caster, spellLevel);
            case DIPOLE_VORTEX -> MagneticSpellEffects.spawnDipoleVortex(level, caster, spellLevel);
            default -> MagneticSpellEffects.spawnBipolarArcs(level, caster, spellLevel);
        }
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity caster, MagicData magicData) {
        if (!super.checkPreCastConditions(level, spellLevel, caster, magicData)) {
            return false;
        }
        return switch (behaviour) {
            case ATTRACT, REPEL -> TargetingFeedback.requireTarget(caster,
                    MagneticSpellUtil.findLivingTarget(level, caster, 8.0D + spellLevel * 0.8D,
                            entity -> MagneticSpellUtil.canTargetEnemy(caster, entity)) != null);
            case SCAN -> TargetingFeedback.requireTarget(caster,
                    MagneticSpellUtil.findLivingTarget(level, caster, 14.0D + spellLevel * 3.0D,
                            entity -> MagneticSpellUtil.canTargetEnemy(caster, entity)) != null);
            case ARMAMENT -> TargetingFeedback.requireTarget(caster,
                    MagneticSpellUtil.findBarrageTarget(level, caster, 12.0D + spellLevel) != null)
                    && isValidArmament(caster.getMainHandItem());
            default -> true;
        };
    }

    private void castArmament(Level level, LivingEntity caster, int spellLevel) {
        ItemStack held = caster.getMainHandItem();
        LivingEntity target = MagneticSpellUtil.findBarrageTarget(level, caster, 12.0D + spellLevel);
        if (!TargetingFeedback.requireTarget(caster, target != null) || !isValidArmament(held)) {
            return;
        }

        float multiplier = 0.80F + (spellLevel - 1) * 0.08F;
        float damage = (float) (Math.max(1.0D, caster.getAttributeValue(Attributes.ATTACK_DAMAGE))
                * multiplier * getSpellPower(spellLevel, caster));
        int attacks = spellLevel <= 4 ? 1 : spellLevel <= 7 ? 2 : 3;

        SpellMagneticWeaponEntity weapon = ModEntities.SPELL_MAGNETIC_WEAPON.get().create(level);
        if (weapon != null) {
            Vec3 start = caster.getEyePosition().add(caster.getLookAngle().scale(0.45D));
            weapon.setPos(start.x, start.y, start.z);
            weapon.configure(caster, target, held, damage, attacks);
            if (level.addFreshEntity(weapon)) {
                MagnetizationSynergyUtil.reserveForAttack(caster, target, weapon);
            }
        }
    }

    private static boolean isValidArmament(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof TieredItem
                || stack.is(ACTagRegistry.MAGNETIC_ITEMS)
                || !stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE).isEmpty());
    }

    private void castBoundroidDrop(Level level, LivingEntity caster, int spellLevel) {
        double range = 12.0D + spellLevel;
        LivingEntity target = MagneticSpellUtil.findBarrageTarget(level, caster, range);
        Vec3 landing = findBoundroidLanding(level, caster, range, target);
        double spawnY = findClearSpawnY(level, landing, 9);

        HeavyweightSpellEntity heavyweight = ModEntities.HEAVYWEIGHT_SPELL.get().create(level);
        if (heavyweight != null) {
            heavyweight.setPos(landing.x, spawnY, landing.z);
            float damage = (float) ((9.0D + 3.0D * spellLevel) * getSpellPower(spellLevel, caster));
            float[] radii = {2.5F, 2.7F, 2.9F, 3.2F, 3.5F};
            float radius = radii[Math.max(0, Math.min(spellLevel - 1, radii.length - 1))];
            heavyweight.configure(caster, target, damage, radius);
            if (level.addFreshEntity(heavyweight) && target != null) {
                MagnetizationSynergyUtil.reserveForAttack(caster, target, heavyweight);
            }
        }
    }

    private static Vec3 findBoundroidLanding(Level level, LivingEntity caster, double range, LivingEntity target) {
        if (target != null) {
            return target.position();
        }
        Vec3 start = caster.getEyePosition();
        BlockHitResult hit = level.clip(new ClipContext(start, start.add(caster.getLookAngle().scale(range)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        return hit.getType() == HitResult.Type.MISS ? hit.getLocation() : hit.getLocation().add(0.0D, 0.05D, 0.0D);
    }

    private static double findClearSpawnY(Level level, Vec3 landing, int maximumHeight) {
        double result = landing.y + 1.1D;
        for (int i = 2; i <= maximumHeight; i++) {
            BlockPos pos = BlockPos.containing(landing.x, landing.y + i, landing.z);
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                break;
            }
            result = landing.y + i;
        }
        return result;
    }

    private void castFerrouslimeCohesion(Level level, LivingEntity caster, int spellLevel) {
        int count = 2 + (spellLevel - 1) / 2;
        int[] lifetimes = {600, 720, 840, 1000, 1200};
        int lifetime = lifetimes[Math.max(0, Math.min(spellLevel - 1, lifetimes.length - 1))];
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2.0D * i / count;
            Vec3 position = caster.position().add(Math.cos(angle) * 1.6D, 0.2D, Math.sin(angle) * 1.6D);
            SummonedFerrouslimeEntity slime = ModEntities.SUMMONED_FERROUSLIME.get().create(level);
            if (slime != null) {
                slime.setPos(position.x, position.y, position.z);
                slime.configure(caster, lifetime, 1);
                level.addFreshEntity(slime);
            }
        }
    }

    private void castTeslaArcNode(Level level, LivingEntity caster, int spellLevel) {
        Vec3 look = caster.getLookAngle();
        int chargeTicks = 30 + spellLevel * 4;
        Vec3 start = caster.getEyePosition().add(look.scale(0.55D));
        TeslaArcNodeEntity node = ModEntities.TESLA_ARC_NODE.get().create(level);
        if (node != null) {
            node.setPos(start.x, start.y, start.z);
            float radius = 5.5F + spellLevel * 0.5F;
            float damage = (float) ((3.5D + spellLevel * 0.5D) * getSpellPower(spellLevel, caster));
            int targets = spellLevel <= 2 ? 1 : 2;
            float explosionPower = 1.5F + spellLevel * 0.1F;
            node.configure(caster, chargeTicks, radius, damage, targets, explosionPower);
            node.shoot(look.x, look.y, look.z, 1.25F, 0.65F);
            level.addFreshEntity(node);
        }
    }

    private void castDipoleVortex(Level level, LivingEntity caster, int spellLevel) {
        LivingEntity target = caster instanceof cn.wanyiwige.magneticspellbooks.entities.MagneticMageEntity mage
                ? mage.getTarget() : null;
        Vec3 position = findBoundroidLanding(level, caster, 10.0D + spellLevel * 2.0D, target);
        DipoleVortexEntity vortex = ModEntities.DIPOLE_VORTEX.get().create(level);
        if (vortex != null) {
            vortex.setPos(position.x, position.y + 0.2D, position.z);
            int lifetime = (4 + spellLevel * 2) * 20;
            float radius = 6.0F + spellLevel * 2.0F;
            float force = 0.14F + spellLevel * 0.04F;
            // The vortex pulses twice per second; this intentionally makes the epic field a high-damage area spell.
            float damage = (float) ((2.5D + spellLevel * 0.75D) * getSpellPower(spellLevel, caster));
            vortex.configure(caster, lifetime, radius, force, damage);
            level.addFreshEntity(vortex);
        }
    }

    private void castNotorScan(Level level, LivingEntity caster, int spellLevel) {
        double range = 14.0D + spellLevel * 3.0D;
        int duration = (6 + spellLevel * 3) * 20;
        LivingEntity target = MagneticSpellUtil.findLivingTarget(level, caster, range,
                entity -> MagneticSpellUtil.canTargetEnemy(caster, entity));
        if (!TargetingFeedback.requireTarget(caster, target != null)) return;
        if (target != null) {
            target.addEffect(new MobEffectInstance(ModEffects.NOTOR_SCANNED.get(), duration, 0, false, false, false));
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, duration, 0, false, false, false));
            if (!target.hasEffect(ACEffectRegistry.MAGNETIZING.get())) {
                target.addEffect(new MobEffectInstance(ACEffectRegistry.MAGNETIZING.get(),
                        duration, 0, false, true, true));
            }
            MagnetizationSynergyUtil.markScan(caster, target, duration);
        }
    }

    private void magnetizeArea(Level level, LivingEntity caster, int spellLevel) {
        double radius = 4.0D + spellLevel;
        int duration = spellLevel * 600;
        int amplifier = (spellLevel - 1) / 2;
        double radiusSqr = radius * radius;
        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                caster.getBoundingBox().inflate(radius),
                entity -> MagneticSpellUtil.canMagnetize(caster, entity)
                        && entity.distanceToSqr(caster) <= radiusSqr)) {
            applyMagnetizing(target, duration, amplifier);
        }
    }

    private void applyMagnetizing(LivingEntity target, int duration) {
        applyMagnetizing(target, duration, 0);
    }

    private void applyMagnetizing(LivingEntity target, int duration, int amplifier) {
        if (target != null) {
            target.addEffect(new MobEffectInstance(ACEffectRegistry.MAGNETIZING.get(), duration, amplifier));
        }
    }

    private void damageTarget(LivingEntity target, LivingEntity caster, float damage) {
        if (target != null) {
            target.hurt(getDamageSource(caster), damage * getSpellPower(1, caster));
            applyMagnetizing(target, 80);
        }
    }

    private static void moveToward(LivingEntity target, Vec3 point, double force) {
        if (target != null) {
            target.setDeltaMovement(target.getDeltaMovement().add(point.subtract(target.position()).normalize().scale(force)));
        }
    }

    private static void moveAway(LivingEntity target, Vec3 point, double force) {
        if (target != null) {
            target.setDeltaMovement(target.getDeltaMovement().add(target.position().subtract(point).normalize().scale(force)));
        }
    }

    private void pulse(Level level, LivingEntity caster, double radius, boolean damage) {
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(radius), entity -> entity != caster && entity.isAlive())) {
            moveAway(target, caster.position(), 0.12D + radius * 0.01D);
            applyMagnetizing(target, 80);
            if (damage) {
                target.hurt(getDamageSource(caster), (float) (2.0D * getSpellPower(1, caster)));
            } else {
                target.addEffect(new MobEffectInstance(ACEffectRegistry.STUNNED.get(), 20));
            }
        }
    }

    private static LivingEntity findTarget(Level level, LivingEntity caster, double range) {
        return MagneticSpellUtil.findLivingTarget(level, caster, range, entity -> true);
    }
}
