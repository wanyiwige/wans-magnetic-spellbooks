package cn.wanyiwige.magneticspellbooks.effects;

import cn.wanyiwige.magneticspellbooks.AlexCavesSpellsbook;
import cn.wanyiwige.magneticspellbooks.registry.ModEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/** Grants level-scaled creative flight anywhere while Magnetic Levitation is active. */
@Mod.EventBusSubscriber(modid = AlexCavesSpellsbook.MOD_ID)
public final class MagneticFlightEvents {
    private static final String DATA_PREFIX = AlexCavesSpellsbook.MOD_ID + ".magnetic_flight.";
    private static final String FALLBACK_ACTIVE = DATA_PREFIX + "fallback_active";
    private static final String PREVIOUS_MAYFLY = DATA_PREFIX + "previous_mayfly";
    private static final String PREVIOUS_FLYING = DATA_PREFIX + "previous_flying";
    private static final String SPEED_ACTIVE = DATA_PREFIX + "speed_active";
    private static final String PREVIOUS_SPEED = DATA_PREFIX + "previous_speed";
    private static final ResourceLocation CREATIVE_FLIGHT =
            ResourceLocation.fromNamespaceAndPath("attributeslib", "creative_flight");

    private MagneticFlightEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide
                || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        MobEffectInstance levitation = player.getEffect(ModEffects.MAGNETIC_LEVITATION.get());
        if (levitation != null) {
            enableFlight(player, levitation.getAmplifier());
            player.fallDistance = 0.0F;
        } else {
            disableFlight(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            disableFlight(player);
        }
    }

    private static void enableFlight(ServerPlayer player, int amplifier) {
        // The effect supplies AttributesLib's flight attribute; mayfly remains a compatibility fallback.
        Abilities abilities = player.getAbilities();
        CompoundTag data = player.getPersistentData();
        boolean changed = false;

        if (!data.getBoolean(FALLBACK_ACTIVE)) {
            data.putBoolean(FALLBACK_ACTIVE, true);
            data.putBoolean(PREVIOUS_MAYFLY, abilities.mayfly);
            data.putBoolean(PREVIOUS_FLYING, abilities.flying);
        }
        if (!abilities.mayfly) {
            abilities.mayfly = true;
            changed = true;
        }
        // mayfly only permits a double-jump toggle. The spell promises usable
        // flight immediately, including when another mod clears the toggle.
        if (!abilities.flying) {
            abilities.flying = true;
            changed = true;
        }

        if (!data.getBoolean(SPEED_ACTIVE)) {
            data.putBoolean(SPEED_ACTIVE, true);
            data.putFloat(PREVIOUS_SPEED, abilities.getFlyingSpeed());
        }

        float targetSpeed = Math.min(0.10F, 0.05F + 0.01F * (amplifier + 1));
        if (Math.abs(abilities.getFlyingSpeed() - targetSpeed) > 1.0E-4F) {
            abilities.setFlyingSpeed(targetSpeed);
            changed = true;
        }
        // Refresh periodically as some ability/attribute mods rewrite mayfly during their own tick handlers.
        if (changed || player.tickCount % 10 == 0) {
            player.onUpdateAbilities();
        }
    }

    private static void disableFlight(ServerPlayer player) {
        Abilities abilities = player.getAbilities();
        CompoundTag data = player.getPersistentData();
        boolean changed = false;
        if (data.getBoolean(FALLBACK_ACTIVE)) {
            if (!player.isCreative() && !player.isSpectator()) {
                Attribute flight = ForgeRegistries.ATTRIBUTES.getValue(CREATIVE_FLIGHT);
                boolean anotherAttributeGrantsFlight = flight != null && player.getAttribute(flight) != null
                        && player.getAttributeValue(flight) > 0.0D;
                abilities.mayfly = data.getBoolean(PREVIOUS_MAYFLY) || anotherAttributeGrantsFlight;
                abilities.flying = (data.getBoolean(PREVIOUS_FLYING)
                        || (anotherAttributeGrantsFlight && abilities.flying)) && abilities.mayfly;
                changed = true;
            }
            data.remove(FALLBACK_ACTIVE);
            data.remove(PREVIOUS_MAYFLY);
            data.remove(PREVIOUS_FLYING);
        }
        if (data.getBoolean(SPEED_ACTIVE)) {
            abilities.setFlyingSpeed(data.contains(PREVIOUS_SPEED, Tag.TAG_FLOAT)
                    ? data.getFloat(PREVIOUS_SPEED) : 0.05F);
            data.remove(SPEED_ACTIVE);
            data.remove(PREVIOUS_SPEED);
            changed = true;
        }
        if (changed) {
            player.onUpdateAbilities();
        }
    }
}
