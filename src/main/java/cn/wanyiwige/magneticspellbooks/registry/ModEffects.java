package cn.wanyiwige.magneticspellbooks.registry;

import cn.wanyiwige.magneticspellbooks.AlexCavesSpellsbook;
import cn.wanyiwige.magneticspellbooks.effects.MagneticLevitationEffect;
import cn.wanyiwige.magneticspellbooks.effects.NotorScannedEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, AlexCavesSpellsbook.MOD_ID);

    public static final RegistryObject<MobEffect> MAGNETIC_LEVITATION =
            EFFECTS.register("magnetic_levitation", MagneticLevitationEffect::new);
    public static final RegistryObject<MobEffect> NOTOR_SCANNED =
            EFFECTS.register("notor_scanned", NotorScannedEffect::new);

    private ModEffects() {
    }
}
