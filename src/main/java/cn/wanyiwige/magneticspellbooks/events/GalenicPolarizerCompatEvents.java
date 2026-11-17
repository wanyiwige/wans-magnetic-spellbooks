package cn.wanyiwige.magneticspellbooks.events;

import cn.wanyiwige.magneticspellbooks.AlexCavesSpellsbook;
import cn.wanyiwige.magneticspellbooks.registry.ModAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = AlexCavesSpellsbook.MOD_ID)
public final class GalenicPolarizerCompatEvents {
    private static final UUID MAGNETIC_POWER_ID = UUID.fromString("795047de-12db-43e7-b524-252199b5c70f");

    private GalenicPolarizerCompatEvents() {
    }

    @SubscribeEvent
    public static void addMagneticSpellPower(ItemAttributeModifierEvent event) {
        if (event.getSlotType() != EquipmentSlot.MAINHAND) {
            return;
        }

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem());
        if (itemId == null || !"traveloptics".equals(itemId.getNamespace())) {
            return;
        }
        String path = itemId.getPath();
        if (!path.equals("galenic_polarizer")
                && !path.equals("galenic_polarizer_level_one")
                && !path.equals("galenic_polarizer_level_two")
                && !path.equals("galenic_polarizer_level_three")) {
            return;
        }

        event.addModifier(ModAttributes.MAGNETIC_SPELL_POWER.get(),
                new AttributeModifier(MAGNETIC_POWER_ID, "Galenic Polarizer magnetic spell power",
                        0.20D, AttributeModifier.Operation.MULTIPLY_BASE));
    }
}
