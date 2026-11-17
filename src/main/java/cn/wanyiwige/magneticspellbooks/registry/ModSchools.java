package cn.wanyiwige.magneticspellbooks.registry;

import cn.wanyiwige.magneticspellbooks.AlexCavesSpellsbook;
import cn.wanyiwige.magneticspellbooks.util.ModTags;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModSchools {
    public static final ResourceLocation MAGNETIC_RESOURCE =
            ResourceLocation.fromNamespaceAndPath(AlexCavesSpellsbook.MOD_ID, "magnetic");

    public static final DeferredRegister<SchoolType> SCHOOLS = DeferredRegister.create(
            SchoolRegistry.SCHOOL_REGISTRY_KEY, AlexCavesSpellsbook.MOD_ID);

    public static final RegistryObject<SchoolType> MAGNETIC = SCHOOLS.register("magnetic", () ->
            new SchoolType(
                    MAGNETIC_RESOURCE,
                    ModTags.MAGNETIC_FOCUS,
                    Component.translatable("school.wans_magnetic_spellbooks.magnetic")
                            .withStyle(style -> style.withColor(0xB34CFF)),
                    ModAttributes.MAGNETIC_SPELL_POWER,
                    ModAttributes.MAGNETIC_MAGIC_RESIST,
                    () -> ACSoundRegistry.MAGNETRON_ASSEMBLE.get(),
                    ModDamageTypes.MAGNETIC_MAGIC));

    private ModSchools() {
    }
}
