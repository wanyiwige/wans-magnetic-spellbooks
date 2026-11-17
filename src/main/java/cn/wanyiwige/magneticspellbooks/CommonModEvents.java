package cn.wanyiwige.magneticspellbooks;

import cn.wanyiwige.magneticspellbooks.registry.ModEntities;
import com.github.alexmodguy.alexscaves.server.entity.living.FerrouslimeEntity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AlexCavesSpellsbook.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CommonModEvents {
    private CommonModEvents() {
    }

    @SubscribeEvent
    public static void createAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.MAGNETIC_MAGE.get(),
                cn.wanyiwige.magneticspellbooks.entities.MagneticMageEntity.createAttributes().build());
        event.put(ModEntities.SUMMONED_FERROUSLIME.get(), FerrouslimeEntity.createAttributes().build());
    }
}
