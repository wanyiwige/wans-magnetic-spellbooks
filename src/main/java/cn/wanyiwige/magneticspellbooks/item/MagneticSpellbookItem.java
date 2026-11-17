package cn.wanyiwige.magneticspellbooks.item;

import cn.wanyiwige.magneticspellbooks.registry.ModSpells;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainerMutable;
import io.redspace.ironsspellbooks.item.SpellBook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * The "Overclocked Magnet" spellbook, migrated from the KubeJS pack addon.
 * Pre-loaded with the two magnetic pole spells: scarlet attraction and azure
 * repulsion, mirroring the original pull/damage theme with the school's two poles.
 */
public final class MagneticSpellbookItem extends SpellBook {
    public MagneticSpellbookItem(Item.Properties properties) {
        super(15, properties);
    }

    @Override
    public void initializeSpellContainer(ItemStack stack) {
        if (stack == null || ISpellContainer.isSpellContainer(stack)) {
            return;
        }
        ISpellContainerMutable container = ISpellContainer.create(getMaxSpellSlots(), true, true).mutableCopy();
        container.addSpell(ModSpells.SCARLET_ATTRACTION.get(), 5, true);
        container.addSpell(ModSpells.AZURE_REPULSION.get(), 5, true);
        ISpellContainer.set(stack, container.toImmutable());
    }
}
