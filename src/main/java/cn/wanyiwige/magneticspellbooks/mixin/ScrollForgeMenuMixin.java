package cn.wanyiwige.magneticspellbooks.mixin;

import cn.wanyiwige.magneticspellbooks.registry.ModItems;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.gui.scroll_forge.ScrollForgeMenu;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Iron's Scroll Forge has no result-factory hook in 3.16.3.  The base menu
 * creates a normal Scroll then writes the authoritative SpellData to it; this
 * inject runs immediately afterwards and only swaps the item for our school.
 */
@Mixin(value = ScrollForgeMenu.class, remap = false)
public abstract class ScrollForgeMenuMixin {
    @Shadow private Slot resultSlot;
    @Shadow private Slot inkSlot;

    @Inject(method = "setupResultSlot", at = @At("TAIL"), remap = false)
    private void alexCavesSpellsbook$useSchoolScroll(AbstractSpell spell, CallbackInfo callback) {
        Item scrollItem = ModItems.scrollForSchool(spell.getSchoolType());
        if (scrollItem == ItemRegistry.SCROLL.get()) {
            return;
        }
        ItemStack result = resultSlot.getItem();
        if (!result.is(ItemRegistry.SCROLL.get())) {
            return;
        }
        ItemStack schoolScroll = new ItemStack(scrollItem);
        if (result.getTag() != null) {
            schoolScroll.setTag(result.getTag().copy());
        }
        resultSlot.set(schoolScroll);
    }
}
