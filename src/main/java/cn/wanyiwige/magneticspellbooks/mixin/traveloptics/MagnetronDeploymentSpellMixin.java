package cn.wanyiwige.magneticspellbooks.mixin.traveloptics;

import cn.wanyiwige.magneticspellbooks.registry.ModSchools;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.gametechbc.traveloptics.spells.lightning.MagnetronDeploymentSpell", remap = false)
public abstract class MagnetronDeploymentSpellMixin {
    public SchoolType getSchoolType() {
        return ModSchools.MAGNETIC.get();
    }

    @Inject(method = "getDefaultConfig", at = @At("HEAD"), cancellable = true, remap = false)
    private void alexCavesSpellsbook$magneticConfig(CallbackInfoReturnable<DefaultConfig> callback) {
        callback.setReturnValue(new DefaultConfig().setMinRarity(SpellRarity.EPIC)
                .setSchoolResource(ModSchools.MAGNETIC_RESOURCE).setMaxLevel(1)
                .setCooldownSeconds(270.0D).setAllowCrafting(true).build());
    }
}
