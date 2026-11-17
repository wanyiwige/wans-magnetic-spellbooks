package cn.wanyiwige.magneticspellbooks.mixin;

import cn.wanyiwige.magneticspellbooks.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Iron's Spellbooks 3.16.3 only accepts its exact vanilla scroll item in the
 * inscription-table scroll slot. Magnetic scrolls extend Scroll and work with
 * the inscription logic itself, so widen only the slot admission check for our
 * custom scroll item.
 */
@Mixin(targets = "io.redspace.ironsspellbooks.gui.inscription_table.InscriptionTableMenu$4", remap = false)
public abstract class InscriptionTableScrollSlotMixin {
    @Inject(
            method = "m_5857_(Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void alexCavesSpellsbook$acceptMagneticScroll(ItemStack stack,
                                                          CallbackInfoReturnable<Boolean> callback) {
        if (stack.is(ModItems.MAGNETIC_POLARITY_SPELL_SCROLL.get())) {
            callback.setReturnValue(true);
        }
    }
}
