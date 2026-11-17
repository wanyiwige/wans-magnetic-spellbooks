package cn.wanyiwige.magneticspellbooks;

import cn.wanyiwige.magneticspellbooks.registry.ModCreativeTabs;
import cn.wanyiwige.magneticspellbooks.registry.ModAttributes;
import cn.wanyiwige.magneticspellbooks.registry.ModEffects;
import cn.wanyiwige.magneticspellbooks.registry.ModEntities;
import cn.wanyiwige.magneticspellbooks.registry.ModItems;
import cn.wanyiwige.magneticspellbooks.registry.ModSchools;
import cn.wanyiwige.magneticspellbooks.registry.ModSpells;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AlexCavesSpellsbook.MOD_ID)
public final class AlexCavesSpellsbook {
    public static final String MOD_ID = "wans_magnetic_spellbooks";

    public AlexCavesSpellsbook(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        ModItems.ITEMS.register(modEventBus);
        ModAttributes.ATTRIBUTES.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModSchools.SCHOOLS.register(modEventBus);
        ModSpells.SPELLS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
    }
}
