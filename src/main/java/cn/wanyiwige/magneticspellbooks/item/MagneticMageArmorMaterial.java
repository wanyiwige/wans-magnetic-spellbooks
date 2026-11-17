package cn.wanyiwige.magneticspellbooks.item;

import cn.wanyiwige.magneticspellbooks.registry.ModItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;

public enum MagneticMageArmorMaterial implements ArmorMaterial {
    INSTANCE;

    private static final int[] DURABILITY_PER_SLOT = new int[]{13, 15, 16, 11};
    private static final EnumMap<ArmorItem.Type, Integer> DEFENSE = new EnumMap<>(ArmorItem.Type.class);

    static {
        DEFENSE.put(ArmorItem.Type.BOOTS, 2);
        DEFENSE.put(ArmorItem.Type.LEGGINGS, 6);
        DEFENSE.put(ArmorItem.Type.CHESTPLATE, 5);
        DEFENSE.put(ArmorItem.Type.HELMET, 2);
    }

    private final LazyLoadedValue<Ingredient> repairIngredient = new LazyLoadedValue<>(
            () -> Ingredient.of(ModItems.BIPOLAR_NEODYMIUM_INGOT.get()));

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return DURABILITY_PER_SLOT[type.getSlot().getIndex()] * 25;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return DEFENSE.get(type);
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_IRON;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient.get();
    }

    @Override
    public String getName() {
        return "wans_magnetic_spellbooks:magnetic_mage";
    }

    @Override
    public float getToughness() {
        return 1.0F;
    }

    @Override
    public float getKnockbackResistance() {
        return 0.0F;
    }
}
