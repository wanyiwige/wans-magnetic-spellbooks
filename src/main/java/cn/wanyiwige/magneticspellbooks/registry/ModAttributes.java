package cn.wanyiwige.magneticspellbooks.registry;

import cn.wanyiwige.magneticspellbooks.AlexCavesSpellsbook;
import io.redspace.ironsspellbooks.api.attribute.MagicPercentAttribute;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, AlexCavesSpellsbook.MOD_ID);

    public static final RegistryObject<Attribute> MAGNETIC_SPELL_POWER = ATTRIBUTES.register(
            "magnetic_spell_power",
            () -> new MagicPercentAttribute("attribute.wans_magnetic_spellbooks.magnetic_spell_power",
                    1.0D, -100.0D, 100.0D).setSyncable(true));

    public static final RegistryObject<Attribute> MAGNETIC_MAGIC_RESIST = ATTRIBUTES.register(
            "magnetic_magic_resist",
            () -> new MagicPercentAttribute("attribute.wans_magnetic_spellbooks.magnetic_magic_resist",
                    1.0D, -100.0D, 100.0D).setSyncable(true));

    private ModAttributes() {
    }
}
