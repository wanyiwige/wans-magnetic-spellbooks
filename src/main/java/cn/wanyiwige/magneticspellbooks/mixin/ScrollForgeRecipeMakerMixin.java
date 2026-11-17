package cn.wanyiwige.magneticspellbooks.mixin;

import cn.wanyiwige.magneticspellbooks.registry.ModItems;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Optional JEI bridge: the existing Iron's category remains the recipe UI. */
@Pseudo
@Mixin(targets = "io.redspace.ironsspellbooks.jei.ScrollForgeRecipeMaker", remap = false)
public abstract class ScrollForgeRecipeMakerMixin {
    @Inject(method = "getScrollStack", at = @At("RETURN"), cancellable = true, remap = false)
    private static void alexCavesSpellsbook$showSchoolScroll(AbstractSpell spell, int spellLevel,
                                                               CallbackInfoReturnable<ItemStack> callback) {
        Item scrollItem = ModItems.scrollForSchool(spell.getSchoolType());
        if (scrollItem == ItemRegistry.SCROLL.get()) {
            return;
        }
        ItemStack scroll = new ItemStack(scrollItem);
        ISpellContainer.createScrollContainer(spell, spellLevel, scroll);
        callback.setReturnValue(scroll);
    }
}
