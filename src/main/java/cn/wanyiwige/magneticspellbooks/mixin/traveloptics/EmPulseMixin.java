package cn.wanyiwige.magneticspellbooks.mixin.traveloptics;

import cn.wanyiwige.magneticspellbooks.compat.TravelOpticsSpellCompat;
import cn.wanyiwige.magneticspellbooks.registry.ModSchools;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.gametechbc.traveloptics.spells.lightning.EmPulse", remap = false)
public abstract class EmPulseMixin {
    public SchoolType getSchoolType() {
        return ModSchools.MAGNETIC.get();
    }

    @Inject(method = "getDefaultConfig", at = @At("HEAD"), cancellable = true, remap = false)
    private void alexCavesSpellsbook$magneticConfig(CallbackInfoReturnable<DefaultConfig> callback) {
        callback.setReturnValue(new DefaultConfig().setMinRarity(SpellRarity.COMMON)
                .setSchoolResource(ModSchools.MAGNETIC_RESOURCE).setMaxLevel(5)
                .setCooldownSeconds(45.0D).setAllowCrafting(true).build());
    }

    @Inject(method = "onCast", at = @At("HEAD"), cancellable = true, remap = false)
    private void alexCavesSpellsbook$replaceCast(Level level, int spellLevel, LivingEntity caster,
                                                 CastSource castSource, MagicData magicData, CallbackInfo callback) {
        TravelOpticsSpellCompat.castEmPulse((AbstractSpell) (Object) this, level, spellLevel, caster, castSource, magicData);
        callback.cancel();
    }
}
