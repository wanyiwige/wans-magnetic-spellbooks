package cn.wanyiwige.magneticspellbooks.mixin.alexscaves;

import cn.wanyiwige.magneticspellbooks.entities.SummonedFerrouslimeEntity;
import cn.wanyiwige.magneticspellbooks.registry.ModSpells;
import com.github.alexmodguy.alexscaves.server.entity.living.FerrouslimeEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Preserve Alex's melee timeline while attributing summoned slime attacks to their caster. */
@Mixin(value = FerrouslimeEntity.class, remap = false)
public abstract class FerrouslimeDamageSourceMixin {
    // This dependency ships SRG names; target its actual runtime bytecode without remapping.
    @Redirect(method = "m_8119_()V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/damagesource/DamageSources;m_269333_(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/damagesource/DamageSource;",
            remap = false), remap = false, require = 1, allow = 1)
    private DamageSource wan$attributeSummonedSlimeAttack(DamageSources sources, LivingEntity attacker) {
        if (attacker instanceof SummonedFerrouslimeEntity summoned) {
            LivingEntity caster = summoned.getSummoner();
            if (caster != null) {
                return ModSpells.FERROUSLIME_COHESION.get().getDamageSource(summoned, caster);
            }
        }
        return sources.mobAttack(attacker);
    }
}
