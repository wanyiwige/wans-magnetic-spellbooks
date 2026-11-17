package cn.wanyiwige.magneticspellbooks.client;

import cn.wanyiwige.magneticspellbooks.AlexCavesSpellsbook;
import cn.wanyiwige.magneticspellbooks.registry.ModEffects;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Low-frequency client particles that keep magnetic status effects readable after the cast burst. */
@Mod.EventBusSubscriber(modid = AlexCavesSpellsbook.MOD_ID, value = Dist.CLIENT)
public final class ClientMagneticVisualEvents {
    private ClientMagneticVisualEvents() {
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player player && player == Minecraft.getInstance().player
                && entity.hasEffect(ModEffects.MAGNETIC_LEVITATION.get())) {
            // Keep the local jump-to-fly ability usable even if another client-side mod rewrites it.
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            int amplifier = player.getEffect(ModEffects.MAGNETIC_LEVITATION.get()).getAmplifier();
            player.getAbilities().setFlyingSpeed(Math.min(0.10F, 0.05F + 0.01F * (amplifier + 1)));
        }
        if (!entity.level().isClientSide || entity.tickCount % 10 != 0) {
            return;
        }
        if (entity.hasEffect(ACEffectRegistry.MAGNETIZING.get())) {
            MagneticSpellEffects.spawnMagnetizedStatus(entity.level(), entity);
        }
        if (entity.hasEffect(ModEffects.NOTOR_SCANNED.get())) {
            MagneticSpellEffects.spawnNotorStatus(entity.level(), entity);
        }
        if (entity instanceof Player player
                && player.getAbilities().flying
                && entity.hasEffect(ModEffects.MAGNETIC_LEVITATION.get())) {
            MagneticSpellEffects.spawnLevitationField(entity.level(), entity);
        }
    }
}
