package cn.wanyiwige.magneticspellbooks.entities;

import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.NeutralWizard;
import io.redspace.ironsspellbooks.entity.mobs.goals.WizardRecoverGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

/** Neutral mage using Iron's synchronized casting, potion and anger lifecycle. */
public class MagneticMageEntity extends NeutralWizard {
    private static final EntityDataAccessor<Integer> SPELL_TARGET =
            SynchedEntityData.defineId(MagneticMageEntity.class, EntityDataSerializers.INT);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SPELL_TARGET, -1);
    }

    @Override
    public void setTarget(LivingEntity target) {
        super.setTarget(target);
        if (!level().isClientSide) {
            entityData.set(SPELL_TARGET, getTarget() == null ? -1 : getTarget().getId());
        }
    }

    public LivingEntity getSpellTarget() {
        if (!level().isClientSide) return getTarget();
        return level().getEntity(entityData.get(SPELL_TARGET)) instanceof LivingEntity living ? living : null;
    }

    public MagneticMageEntity(EntityType<? extends MagneticMageEntity> type, Level level) {
        super(type, level);
        xpReward = 25;
        // A real weapon is required by Teletor Armament; never drop a free sword.
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MagneticMageAttackGoal(this));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.75D));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        goalSelector.addGoal(10, new WizardRecoverGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class,
                10, true, false, this::isHostileTowards));
        targetSelector.addGoal(5, new ResetUniversalAngerTargetGoal<>(this, false));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D).add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D).add(Attributes.ARMOR, 6.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.0D);
    }

    @Override
    public boolean guardsBlocks() {
        // This NPC has no claimed structure or shop yet.
        return false;
    }
}
