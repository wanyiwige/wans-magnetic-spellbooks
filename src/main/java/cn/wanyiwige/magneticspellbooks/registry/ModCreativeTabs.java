package cn.wanyiwige.magneticspellbooks.registry;

import cn.wanyiwige.magneticspellbooks.AlexCavesSpellsbook;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AlexCavesSpellsbook.MOD_ID);
    public static final RegistryObject<CreativeModeTab> MAGNETIC = TABS.register("magnetic", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.wans_magnetic_spellbooks.magnetic"))
        .icon(() -> ModItems.MAGNETIC_POLARITY_SPELL_SCROLL.get().getDefaultInstance())
        .displayItems((parameters, output) -> {
            ModSpells.SPELLS.getEntries().stream().map(RegistryObject::get).filter(AbstractSpell::isEnabled)
                .forEach(spell -> addAllLevels(output, spell));
            addExternalSpell(output, "em_pulse");
            addExternalSpell(output, "magnetron_deployment");
            addExternalSpell(output, "galena_shatter");
            ModItems.ITEMS.getEntries().stream().map(RegistryObject::get)
                .filter(item -> item != ModItems.MAGNETIC_POLARITY_SPELL_SCROLL.get()).forEach(output::accept);
        }).build());
    private static void addAllLevels(CreativeModeTab.Output output, AbstractSpell spell) {
        for (int level = spell.getMinLevel(); level <= spell.getMaxLevel(); level++) {
            ItemStack scroll = new ItemStack(ModItems.scrollForSchool(spell.getSchoolType()));
            ISpellContainer.createScrollContainer(spell, level, scroll);
            output.accept(scroll);
        }
    }

    private static void addExternalSpell(CreativeModeTab.Output output, String path) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("traveloptics", path);
        AbstractSpell spell = SpellRegistry.getSpell(id);
        if (spell.getSpellResource().equals(id)) {
            addAllLevels(output, spell);
        }
    }
}
