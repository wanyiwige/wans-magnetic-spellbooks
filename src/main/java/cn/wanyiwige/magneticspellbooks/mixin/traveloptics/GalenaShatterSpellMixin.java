package cn.wanyiwige.magneticspellbooks.mixin.traveloptics;

import cn.wanyiwige.magneticspellbooks.compat.TravelOpticsSpellCompat;
import cn.wanyiwige.magneticspellbooks.registry.ModSchools;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.gametechbc.traveloptics.spells.lightning.GalenaShatterSpell", remap = false)
public abstract class GalenaShatterSpellMixin {
    public SchoolType getSchoolType() {
        return ModSchools.MAGNETIC.get();
    }

    @Inject(method = "getDefaultConfig", at = @At("HEAD"), cancellable = true, remap = false)
    private void alexCavesSpellsbook$magneticConfig(CallbackInfoReturnable<DefaultConfig> callback) {
        callback.setReturnValue(new DefaultConfig().setMinRarity(SpellRarity.LEGENDARY)
                .setSchoolResource(ModSchools.MAGNETIC_RESOURCE).setMaxLevel(1)
                .setCooldownSeconds(20.0D).setAllowCrafting(true).build());
    }

    @Inject(method = "spawnChargingLightning", at = @At("HEAD"), cancellable = true, remap = false)
    private void alexCavesSpellsbook$replaceCharging(Level level, Vec3 center, LivingEntity caster,
                                                     double radius, CallbackInfo callback) {
        TravelOpticsSpellCompat.spawnBipolarRing(level, center, radius, 4, caster);
        callback.cancel();
    }

    @Inject(method = "spawnExpandingLightning", at = @At("HEAD"), cancellable = true, remap = false)
    private void alexCavesSpellsbook$replaceExpansion(Level level, LivingEntity caster, Vec3 center,
                                                      double radius, CallbackInfo callback) {
        TravelOpticsSpellCompat.spawnBipolarRing(level, center, radius, 12, caster);
        callback.cancel();
    }

    @Inject(method = "spawnDischargeParticles", at = @At("HEAD"), cancellable = true, remap = false)
    private void alexCavesSpellsbook$replaceDischarge(LivingEntity target, int stacks, boolean azure,
                                                      CallbackInfo callback) {
        TravelOpticsSpellCompat.spawnGalenaDischarge(target, stacks, azure);
        callback.cancel();
    }

    @Inject(method = "spawnReverseBlastwaveParticles", at = @At("HEAD"), cancellable = true, remap = false)
    private void alexCavesSpellsbook$replaceReverseWave(Level level, Vec3 center, boolean scarlet,
                                                        double radius, CallbackInfo callback) {
        TravelOpticsSpellCompat.spawnPolarityOrbits(level, center, Math.max(0.8D, radius * 0.35D));
        callback.cancel();
    }

    @Inject(method = "spawnMainBlastwave", at = @At("HEAD"), cancellable = true, remap = false)
    private void alexCavesSpellsbook$replaceMainWave(Level level, LivingEntity caster, Vec3 center,
                                                     double radius, CallbackInfo callback) {
        TravelOpticsSpellCompat.spawnBipolarRing(level, center, radius, 16, caster);
        callback.cancel();
    }

    @Inject(method = "spawnRandomMagnetParticles", at = @At("HEAD"), cancellable = true, remap = false)
    private void alexCavesSpellsbook$replaceRandomMagnets(Level level, Vec3 center, double radius,
                                                          LivingEntity caster, CallbackInfo callback) {
        TravelOpticsSpellCompat.spawnBipolarRing(level, center, radius * 0.75D, 8, caster);
        callback.cancel();
    }
}
