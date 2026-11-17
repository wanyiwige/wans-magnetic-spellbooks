package cn.wanyiwige.magneticspellbooks.registry;

import cn.wanyiwige.magneticspellbooks.AlexCavesSpellsbook;
import cn.wanyiwige.magneticspellbooks.item.MagneticPolarityScrollItem;
import cn.wanyiwige.magneticspellbooks.item.MagneticMageArmorItem;
import cn.wanyiwige.magneticspellbooks.item.MagneticSpellbookItem;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.item.UpgradeOrbItem;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AlexCavesSpellsbook.MOD_ID);

    public static final RegistryObject<Item> MAGNETIC_MAGE_SPAWN_EGG = ITEMS.register(
            "magnetic_mage_spawn_egg", () -> new net.minecraftforge.common.ForgeSpawnEggItem(
                    ModEntities.MAGNETIC_MAGE, 0x3B424B, 0xB64B51, new Item.Properties()));

    public static final RegistryObject<Item> MAGNETIC_POLARITY_SPELL_SCROLL = ITEMS.register("magnetic_polarity_spell_scroll", () -> new MagneticPolarityScrollItem(new Item.Properties()));

    public static final RegistryObject<Item> MAGNETIC_POLARITY_RUNE = register("magnetic_polarity_rune");
    public static final RegistryObject<Item> MAGNETIC_POLARITY_UPGRADE_ORB = ITEMS.register(
            "magnetic_polarity_upgrade_orb",
            () -> new UpgradeOrbItem(new Item.Properties(), ModUpgradeOrbTypes.MAGNETIC_POWER));
    public static final RegistryObject<Item> MAGNET_SPELLBOOK = ITEMS.register(
            "magnet_spellbook",
            () -> new MagneticSpellbookItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> MAGNETIC_MAGE_HELMET = ITEMS.register(
            "magnetic_mage_helmet", () -> new MagneticMageArmorItem(ArmorItem.Type.HELMET));
    public static final RegistryObject<Item> MAGNETIC_MAGE_CHESTPLATE = ITEMS.register(
            "magnetic_mage_chestplate", () -> new MagneticMageArmorItem(ArmorItem.Type.CHESTPLATE));
    public static final RegistryObject<Item> MAGNETIC_MAGE_LEGGINGS = ITEMS.register(
            "magnetic_mage_leggings", () -> new MagneticMageArmorItem(ArmorItem.Type.LEGGINGS));
    public static final RegistryObject<Item> MAGNETIC_MAGE_BOOTS = ITEMS.register(
            "magnetic_mage_boots", () -> new MagneticMageArmorItem(ArmorItem.Type.BOOTS));
    public static final RegistryObject<Item> BIPOLAR_NEODYMIUM_INGOT = register("bipolar_neodymium_ingot");

    public static Item scrollForSchool(SchoolType school) {
        if (school == ModSchools.MAGNETIC.get()) return MAGNETIC_POLARITY_SPELL_SCROLL.get();
        return ItemRegistry.SCROLL.get();
    }

    private ModItems() { }

    private static RegistryObject<Item> register(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }
}
