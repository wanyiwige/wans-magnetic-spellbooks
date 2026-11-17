package cn.wanyiwige.magneticspellbooks.registry;

import cn.wanyiwige.magneticspellbooks.AlexCavesSpellsbook;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public final class ModDamageTypes {
    public static final ResourceKey<DamageType> MAGNETIC_MAGIC = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(AlexCavesSpellsbook.MOD_ID, "magnetic_magic"));

    private ModDamageTypes() {
    }
}
