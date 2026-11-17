package cn.wanyiwige.magneticspellbooks.entities;

import cn.wanyiwige.magneticspellbooks.registry.ModSpells;
import cn.wanyiwige.magneticspellbooks.util.MagneticSpellUtil;
import cn.wanyiwige.magneticspellbooks.util.MagnetizationSynergyUtil;
import com.github.alexmodguy.alexscaves.server.entity.item.MagneticWeaponEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public final class SpellMagneticWeaponEntity extends MagneticWeaponEntity {
    private UUID ownerUUID;
    private UUID targetUUID;
    private float spellDamage;
    private int attacksRemaining;
    private int attackIndex;
    private int returnCooldown;
    private boolean returning;

    public SpellMagneticWeaponEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public void configure(LivingEntity owner, LivingEntity target, ItemStack visualStack,
                          float damage, int attacks) {
        this.ownerUUID = owner.getUUID();
        this.targetUUID = target.getUUID();
        this.spellDamage = damage;
        this.attacksRemaining = attacks;
        this.setControllerUUID(owner.getUUID());
        ItemStack copy = visualStack.copy();
        copy.setCount(1);
        this.setItemStack(copy);
    }

    @Override
    public void tick() {
        this.baseTick();
        if (level().isClientSide) {
            move(MoverType.SELF, getDeltaMovement());
            return;
        }
        if (!(level() instanceof ServerLevel serverLevel) || tickCount > 200) {
            discard();
            return;
        }

        Entity ownerEntity = ownerUUID == null ? null : serverLevel.getEntity(ownerUUID);
        if (!(ownerEntity instanceof LivingEntity owner) || !owner.isAlive()) {
            discard();
            return;
        }
        Entity targetEntity = targetUUID == null ? null : serverLevel.getEntity(targetUUID);
        LivingEntity target = targetEntity instanceof LivingEntity living && living.isAlive() ? living : null;
        if (returnCooldown > 0) {
            returnCooldown--;
        }

        Vec3 ownerHand = owner.getEyePosition().add(owner.getLookAngle().scale(0.45D));
        if (returning || target == null) {
            moveToward(ownerHand, 0.42D);
            if (position().distanceToSqr(ownerHand) < 0.65D) {
                if (attacksRemaining > 0 && target != null && returnCooldown == 0) {
                    returning = false;
                } else if (attacksRemaining <= 0 || target == null) {
                    discard();
                } else {
                    setDeltaMovement(Vec3.ZERO);
                }
            }
        } else {
            Vec3 strikePoint = target.getEyePosition().add(0.0D, -target.getBbHeight() * 0.25D, 0.0D);
            moveToward(strikePoint, 0.48D);
            if (distanceToSqr(target) <= 1.7D && returnCooldown == 0
                    && MagneticSpellUtil.canTargetEnemy(owner, target)) {
                float decay = attackIndex == 0 ? 1.0F : attackIndex == 1 ? 0.85F : 0.70F;
                float damage = MagnetizationSynergyUtil.applyPrimaryReward(spellDamage * decay, owner, target, this);
                target.hurt(ModSpells.TELETOR_ARMAMENT.get().getDamageSource(this, owner), damage);
                attackIndex++;
                attacksRemaining--;
                returnCooldown = 20;
                returning = true;
            }
        }

        move(MoverType.SELF, getDeltaMovement());
    }

    private void moveToward(Vec3 destination, double speed) {
        Vec3 offset = destination.subtract(position());
        if (offset.lengthSqr() < 1.0E-4D) {
            setDeltaMovement(Vec3.ZERO);
            return;
        }
        Vec3 movement = getDeltaMovement().scale(0.55D).add(offset.normalize().scale(speed));
        if (movement.lengthSqr() > speed * speed) {
            movement = movement.normalize().scale(speed);
        }
        setDeltaMovement(movement);
        setYRot((float) (Math.atan2(movement.z, movement.x) * 180.0D / Math.PI) - 90.0F);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUUID != null) tag.putUUID("SpellOwner", ownerUUID);
        if (targetUUID != null) tag.putUUID("SpellTarget", targetUUID);
        tag.putFloat("SpellDamage", spellDamage);
        tag.putInt("AttacksRemaining", attacksRemaining);
        tag.putInt("AttackIndex", attackIndex);
        tag.putInt("ReturnCooldown", returnCooldown);
        tag.putBoolean("Returning", returning);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ownerUUID = tag.hasUUID("SpellOwner") ? tag.getUUID("SpellOwner") : null;
        targetUUID = tag.hasUUID("SpellTarget") ? tag.getUUID("SpellTarget") : null;
        spellDamage = tag.getFloat("SpellDamage");
        attacksRemaining = tag.getInt("AttacksRemaining");
        attackIndex = tag.getInt("AttackIndex");
        returnCooldown = tag.getInt("ReturnCooldown");
        returning = tag.getBoolean("Returning");
    }
}
