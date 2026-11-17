package cn.wanyiwige.magneticspellbooks.entities;

import cn.wanyiwige.magneticspellbooks.registry.ModEffects;
import cn.wanyiwige.magneticspellbooks.registry.ModSpells;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.entity.mobs.goals.WizardAttackGoal;
import java.util.HashMap;
import java.util.Map;

/** Reuses native pursuit/strafe/flee movement, with bounded school-specific casts. */
public final class MagneticMageAttackGoal extends WizardAttackGoal {
    private final MagneticMageEntity mage;
    private final Map<AbstractSpell, Long> readyAt = new HashMap<>();
    private long potionReadyAt;

    public MagneticMageAttackGoal(MagneticMageEntity mage) {
        super(mage, 1.15D, 45, 65);
        this.mage = mage;
        spellcastingRange = 14.0F;
        spellcastingRangeSqr = spellcastingRange * spellcastingRange;
    }

    @Override
    protected void doSpellAction() {
        if (target == null || !target.isAlive() || !hasLineOfSight) return;
        double distance = mage.distanceToSqr(target);
        if (distance > spellcastingRangeSqr) return;
        long now = mage.level().getGameTime();
        if (mage.getHealth() < mage.getMaxHealth() * 0.45F
                && distance > 36.0D && now >= potionReadyAt) {
            mage.startDrinkingPotion();
            potionReadyAt = now + 300;
            return;
        }
        if (distance < 25.0D && cast(ModSpells.AZURE_REPULSION.get(), 3, 120)) return;
        if (!target.hasEffect(ModEffects.NOTOR_SCANNED.get())
                && cast(ModSpells.NOTOR_SCAN.get(), 1, 280)) return;
        if (mage.getHealth() < mage.getMaxHealth() * 0.65F
                && distance < 144.0D && cast(ModSpells.DIPOLE_VORTEX.get(), 1, 700)) return;
        if (cast(ModSpells.TESLA_ARC_NODE.get(), 1, 700)) return;
        if (distance <= 169.0D && cast(ModSpells.BOUNDROID_DROP.get(), 1, 360)) return;
        if (cast(ModSpells.TELETOR_ARMAMENT.get(), 2, 180)) return;
        if (distance > 64.0D && distance < 100.0D) {
            cast(ModSpells.SCARLET_ATTRACTION.get(), 2, 120);
        }
    }

    private boolean cast(AbstractSpell spell, int level, int cooldownTicks) {
        long now = mage.level().getGameTime();
        if (!spell.isEnabled() || now < readyAt.getOrDefault(spell, 0L)) return false;
        level = Math.max(spell.getMinLevel(), Math.min(level, spell.getMaxLevel()));
        mage.lookAt(target, 360.0F, 360.0F);
        if (!spell.checkPreCastConditions(mage.level(), level, mage, MagicData.getPlayerMagicData(mage))) {
            return false;
        }
        mage.initiateCastSpell(spell, level);
        if (!mage.isCasting()) return false;
        readyAt.put(spell, now + cooldownTicks + spell.getCastTime(level));
        fleeCooldown = 7 + spell.getCastTime(level);
        return true;
    }

    @Override
    public void stop() {
        // Native stop only resets movement: also prevent a cast finishing after target loss.
        mage.cancelCast();
        super.stop();
        // Do not reset cooldowns when line of sight/targets change.
    }
}
