package cn.wanyiwige.magneticspellbooks.registry;

import cn.wanyiwige.magneticspellbooks.AlexCavesSpellsbook;
import io.redspace.ironsspellbooks.item.armor.UpgradeOrbType;
import io.redspace.ironsspellbooks.registries.UpgradeOrbTypeRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/** Data-driven Iron's Spellbooks upgrade-orb type keys owned by this addon. */
public final class ModUpgradeOrbTypes {

    public static final ResourceKey<UpgradeOrbType> MAGNETIC_POWER = ResourceKey.create(
            UpgradeOrbTypeRegistry.UPGRADE_ORB_REGISTRY_KEY,
            ResourceLocation.fromNamespaceAndPath(AlexCavesSpellsbook.MOD_ID, "magnetic_power"));

    private ModUpgradeOrbTypes() {
    }
}
