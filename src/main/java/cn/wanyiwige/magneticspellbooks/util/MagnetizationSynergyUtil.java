package cn.wanyiwige.magneticspellbooks.util;

import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.UUID;

/** Server-only scan ownership and immutable primary-target attack snapshots.
 * Forge persistent entity data carries both records through entity saves.
 * No damage event hook is used: only explicit primary spell hits opt in.
 */
public final class MagnetizationSynergyUtil {
    private static final String SCANS = "WanMagneticScans";
    private static final String ATTACK = "WanMagneticPrimary";
    public static final float SCAN_DAMAGE_MULTIPLIER = 1.25F;

    private MagnetizationSynergyUtil() { }

    public record Snapshot(UUID caster, UUID target, UUID attack, int actualStrength,
                           int effectiveStrength, boolean scanBonus) {
        public float damageMultiplier() {
            return scanBonus ? SCAN_DAMAGE_MULTIPLIER : 1.0F;
        }
    }

    public static int getStrength(LivingEntity target) {
        MobEffectInstance effect = target.getEffect(ACEffectRegistry.MAGNETIZING.get());
        return effect == null ? 0 : Math.max(1, Math.min(5, effect.getAmplifier() + 1));
    }

    /** Replace rather than update, so an old hidden stronger effect cannot reappear. */
    public static boolean consumeOneLevel(LivingEntity target) {
        if (target.level().isClientSide) return false;
        MobEffectInstance effect = target.getEffect(ACEffectRegistry.MAGNETIZING.get());
        int strength = getStrength(target);
        if (effect == null) return false;
        MobEffectInstance reduced = strength > 1
                ? new MobEffectInstance(effect.getEffect(), effect.getDuration(), strength - 2,
                    effect.isAmbient(), effect.isVisible(), effect.showIcon())
                : null;
        target.removeEffect(ACEffectRegistry.MAGNETIZING.get());
        if (reduced != null) target.addEffect(reduced);
        return true;
    }

    /** Rescanning refreshes one record per caster; it never accumulates charges. */
    public static void markScan(LivingEntity caster, LivingEntity target, int durationTicks) {
        if (target.level().isClientSide || caster.level() != target.level()
                || !MagneticSpellUtil.canTargetEnemy(caster, target)) return;
        CompoundTag records = liveScans(target);
        CompoundTag record = new CompoundTag();
        record.putUUID("Caster", caster.getUUID());
        record.putLong("Expires", target.level().getGameTime() + Math.max(1, durationTicks));
        records.put(caster.getUUID().toString(), record);
        target.getPersistentData().put(SCANS, records);
    }

    public static boolean hasScanReward(LivingEntity caster, LivingEntity target) {
        if (target.level().isClientSide) return false;
        return liveScans(target).contains(caster.getUUID().toString());
    }

    private static CompoundTag liveScans(LivingEntity target) {
        CompoundTag records = target.getPersistentData().getCompound(SCANS);
        long now = target.level().getGameTime();
        for (String key : new ArrayList<>(records.getAllKeys())) {
            if (records.getCompound(key).getLong("Expires") <= now) records.remove(key);
        }
        if (records.isEmpty()) target.getPersistentData().remove(SCANS);
        else target.getPersistentData().put(SCANS, records);
        return records;
    }

    /**
     * Call only AFTER addFreshEntity succeeds, or when a flying attack first binds
     * its primary target. Removing the live charge reserves it for this attack.
     * Repeated calls reuse its snapshot without taking a later scan's reward.
     */
    public static Snapshot reserveForAttack(LivingEntity caster, LivingEntity target, Entity attack) {
        if (attack.level().isClientSide || attack.level() != target.level()
                || caster.level() != target.level()
                || !MagneticSpellUtil.canTargetEnemy(caster, target)) {
            return plainSnapshot(caster, target, attack);
        }
        CompoundTag previous = attack.getPersistentData().getCompound(ATTACK);
        if (previous.hasUUID("Target")) {
            if (previous.hasUUID("Caster") && previous.hasUUID("Attack")
                    && previous.getUUID("Caster").equals(caster.getUUID())
                    && previous.getUUID("Target").equals(target.getUUID())
                    && previous.getUUID("Attack").equals(attack.getUUID())) {
                return readSnapshot(previous);
            }
            // Secondary targets cannot replace or borrow the primary snapshot.
            return plainSnapshot(caster, target, attack);
        }
        CompoundTag records = liveScans(target);
        String key = caster.getUUID().toString();
        boolean bonus = records.contains(key);
        int actual = getStrength(target);
        Snapshot snapshot = new Snapshot(caster.getUUID(), target.getUUID(), attack.getUUID(),
                actual, bonus ? Math.min(5, actual + 1) : actual, bonus);
        CompoundTag attackData = writeSnapshot(snapshot);
        if (bonus) {
            CompoundTag record = records.getCompound(key);
            record.putUUID("ReservedBy", attack.getUUID());
            record.putLong("ReservedUntil", record.getLong("Expires"));
            attackData.put("ScanReservation", record.copy());
            records.remove(key);
            if (records.isEmpty()) target.getPersistentData().remove(SCANS);
            else target.getPersistentData().put(SCANS, records);
        }
        attack.getPersistentData().put(ATTACK, attackData);
        return snapshot;
    }

    private static Snapshot plainSnapshot(LivingEntity caster, LivingEntity target, Entity attack) {
        int actual = getStrength(target);
        return new Snapshot(caster.getUUID(), target.getUUID(), attack.getUUID(), actual, actual, false);
    }

    private static CompoundTag writeSnapshot(Snapshot snapshot) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Caster", snapshot.caster());
        tag.putUUID("Target", snapshot.target());
        tag.putUUID("Attack", snapshot.attack());
        tag.putInt("Actual", snapshot.actualStrength());
        tag.putInt("Effective", snapshot.effectiveStrength());
        tag.putBoolean("ScanBonus", snapshot.scanBonus());
        return tag;
    }

    private static Snapshot readSnapshot(CompoundTag tag) {
        return new Snapshot(tag.getUUID("Caster"), tag.getUUID("Target"), tag.getUUID("Attack"),
                tag.getInt("Actual"), tag.getInt("Effective"), tag.getBoolean("ScanBonus"));
    }

    /** The power multiplier is applied once, including to additive magnetic damage. */
    public static float calculateDamage(float baseDamage, float powerMultiplier,
                                        float magneticMultiplier, float magneticAddition,
                                        Snapshot snapshot, boolean primaryTarget) {
        float damage = (baseDamage * magneticMultiplier + magneticAddition) * powerMultiplier;
        return primaryTarget ? damage * snapshot.damageMultiplier() : damage;
    }

    /** Read an existing reservation without consuming a charge (used by armor echoes). */
    public static float getReservedDamageMultiplier(LivingEntity caster, LivingEntity target, Entity attack) {
        if (attack == null) return 1.0F;
        CompoundTag tag = attack.getPersistentData().getCompound(ATTACK);
        if (!tag.hasUUID("Caster") || !tag.hasUUID("Target") || !tag.hasUUID("Attack")
                || !tag.getUUID("Caster").equals(caster.getUUID())
                || !tag.getUUID("Target").equals(target.getUUID())
                || !tag.getUUID("Attack").equals(attack.getUUID())) return 1.0F;
        return tag.getBoolean("ScanBonus") ? SCAN_DAMAGE_MULTIPLIER : 1.0F;
    }

    /** Existing entities already store powered damage: do not apply spell power again. */
    public static float applyPrimaryReward(float poweredDamage, LivingEntity caster,
                                           LivingEntity target, Entity attack) {
        return poweredDamage * reserveForAttack(caster, target, attack).damageMultiplier();
    }
}

