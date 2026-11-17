package cn.wanyiwige.magneticspellbooks.events;

import cn.wanyiwige.magneticspellbooks.AlexCavesSpellsbook;
import cn.wanyiwige.magneticspellbooks.registry.ModItems;
import cn.wanyiwige.magneticspellbooks.registry.ModSchools;
import cn.wanyiwige.magneticspellbooks.util.MagneticSpellUtil;
import cn.wanyiwige.magneticspellbooks.util.MagnetizationSynergyUtil;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AlexCavesSpellsbook.MOD_ID)
public final class MagneticMageArmorEvents {
    private static final String ECHO_COOLDOWN = "magnetic_mage_echo_cooldown";

    private MagneticMageArmorEvents() {
    }

    @SubscribeEvent
    public static void reduceMagneticManaCost(SpellOnCastEvent event) {
        if (hasFullSet(event.getEntity()) && event.getSchoolType() == ModSchools.MAGNETIC.get()) {
            event.setManaCost(Math.max(0, Mth.ceil(event.getManaCost() * 0.85F)));
        }
    }

    @SubscribeEvent
    public static void polarEcho(SpellDamageEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }
        SpellDamageSource source = event.getSpellDamageSource();
        if (!(source.getEntity() instanceof ServerPlayer caster)
                || !hasFullSet(caster)
                || source.spell().getSchoolType() != ModSchools.MAGNETIC.get()
                || !event.getEntity().hasEffect(ACEffectRegistry.MAGNETIZING.get())) {
            return;
        }
        long now = level.getGameTime();
        if (caster.getPersistentData().getLong(ECHO_COOLDOWN) > now) {
            return;
        }
        LivingEntity primary = event.getEntity();
        LivingEntity secondary = level.getEntitiesOfClass(LivingEntity.class, primary.getBoundingBox().inflate(8.0D),
                        candidate -> candidate != primary && MagneticSpellUtil.canTargetEnemy(caster, candidate))
                .stream()
                .min(java.util.Comparator.comparingDouble(primary::distanceToSqr))
                .orElse(null);
        if (secondary == null) {
            return;
        }
        caster.getPersistentData().putLong(ECHO_COOLDOWN, now + 40L);
        // Echo neither consumes another scan nor copies the primary's 25% scan bonus.
        float unscannedDamage = event.getAmount() / MagnetizationSynergyUtil.getReservedDamageMultiplier(
                caster, primary, source.getDirectEntity());
        float damage = Mth.clamp(unscannedDamage * 0.25F, 1.0F, 6.0F);
        secondary.hurt(source.spell().getDamageSource(caster, caster), damage);
        Vec3 direction = secondary.position().subtract(primary.position());
        MagneticSpellUtil.applyMagneticForce(secondary, direction, 0.24D);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                (primary.getX() + secondary.getX()) * 0.5D, (primary.getY() + secondary.getY()) * 0.5D + 0.8D,
                (primary.getZ() + secondary.getZ()) * 0.5D, 4, 0.18D, 0.18D, 0.18D, 0.01D);
    }

    public static boolean hasFullSet(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.MAGNETIC_MAGE_HELMET.get())
                && player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.MAGNETIC_MAGE_CHESTPLATE.get())
                && player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.MAGNETIC_MAGE_LEGGINGS.get())
                && player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.MAGNETIC_MAGE_BOOTS.get());
    }
}
