package cn.wanyiwige.magneticspellbooks.client;

import cn.wanyiwige.magneticspellbooks.AlexCavesSpellsbook;
import cn.wanyiwige.magneticspellbooks.client.render.HeavyweightSpellRenderer;
import cn.wanyiwige.magneticspellbooks.client.render.SpellMagneticWeaponRenderer;
import cn.wanyiwige.magneticspellbooks.client.render.TeslaArcNodeRenderer;
import cn.wanyiwige.magneticspellbooks.client.render.DipoleVortexRenderer;
import cn.wanyiwige.magneticspellbooks.client.render.PolarityCollapseRenderer;
import cn.wanyiwige.magneticspellbooks.client.render.MagneticRailLanceRenderer;
import cn.wanyiwige.magneticspellbooks.client.render.DipoleAnvilRenderer;
import cn.wanyiwige.magneticspellbooks.client.render.MagnetizedFractureChainRenderer;
import cn.wanyiwige.magneticspellbooks.client.render.PolarityReversalRenderer;
import cn.wanyiwige.magneticspellbooks.client.render.FerrousSandstormRenderer;
import cn.wanyiwige.magneticspellbooks.registry.ModEntities;
import com.github.alexmodguy.alexscaves.client.render.entity.FerrouslimeRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AlexCavesSpellsbook.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {

        event.registerEntityRenderer(ModEntities.POLARITY_COLLAPSE.get(), PolarityCollapseRenderer::new);
        event.registerEntityRenderer(ModEntities.MAGNETIC_RAIL_LANCE.get(), MagneticRailLanceRenderer::new);
        event.registerEntityRenderer(ModEntities.DIPOLE_ANVIL.get(), DipoleAnvilRenderer::new);
        event.registerEntityRenderer(ModEntities.MAGNETIZED_FRACTURE_CHAIN.get(), MagnetizedFractureChainRenderer::new);
        event.registerEntityRenderer(ModEntities.POLARITY_REVERSAL_TRACKER.get(), PolarityReversalRenderer::new);
        event.registerEntityRenderer(ModEntities.FERROUS_SANDSTORM.get(), FerrousSandstormRenderer::new);

        event.registerEntityRenderer(ModEntities.MAGNETIC_MAGE.get(),
                cn.wanyiwige.magneticspellbooks.client.render.MagneticMageRenderer::new);
        event.registerEntityRenderer(ModEntities.SPELL_MAGNETIC_WEAPON.get(), SpellMagneticWeaponRenderer::new);
        event.registerEntityRenderer(ModEntities.HEAVYWEIGHT_SPELL.get(), HeavyweightSpellRenderer::new);
        event.registerEntityRenderer(ModEntities.SUMMONED_FERROUSLIME.get(), ferrouslimeRenderer());
        event.registerEntityRenderer(ModEntities.TESLA_ARC_NODE.get(), TeslaArcNodeRenderer::new);
        event.registerEntityRenderer(ModEntities.DIPOLE_VORTEX.get(), DipoleVortexRenderer::new);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static EntityRendererProvider ferrouslimeRenderer() {
        return context -> (EntityRenderer) new FerrouslimeRenderer(context);
    }
}
