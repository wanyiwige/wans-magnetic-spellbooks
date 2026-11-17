package cn.wanyiwige.magneticspellbooks.registry;

import cn.wanyiwige.magneticspellbooks.AlexCavesSpellsbook;
import cn.wanyiwige.magneticspellbooks.spells.MagneticSpell;
import cn.wanyiwige.magneticspellbooks.spells.PolarityCollapseSpell;
import cn.wanyiwige.magneticspellbooks.spells.MagneticRailLanceSpell;
import cn.wanyiwige.magneticspellbooks.spells.DipoleAnvilSpell;
import cn.wanyiwige.magneticspellbooks.spells.MagnetizedFractureChainSpell;
import cn.wanyiwige.magneticspellbooks.spells.PolarityReversalSpell;
import cn.wanyiwige.magneticspellbooks.spells.FerrousSandstormSpell;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModSpells {
    public static final DeferredRegister<AbstractSpell> SPELLS = DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY, AlexCavesSpellsbook.MOD_ID);

    public static final RegistryObject<AbstractSpell> MAGNETIZE = register("magnetize", SpellRarity.COMMON, 10, CastType.INSTANT, 4, 15, 5, 0, MagneticSpell.Behaviour.MAGNETIZE);
    public static final RegistryObject<AbstractSpell> SCARLET_ATTRACTION = register("scarlet_attraction", SpellRarity.COMMON, 10, CastType.INSTANT, 5, 20, 7, 0, MagneticSpell.Behaviour.ATTRACT);
    public static final RegistryObject<AbstractSpell> AZURE_REPULSION = register("azure_repulsion", SpellRarity.COMMON, 10, CastType.INSTANT, 5, 20, 7, 0, MagneticSpell.Behaviour.REPEL);
    public static final RegistryObject<AbstractSpell> TELETOR_ARMAMENT = register("teletor_armament", SpellRarity.UNCOMMON, 8, CastType.LONG, 9, 45, 10, 14, MagneticSpell.Behaviour.ARMAMENT);
    public static final RegistryObject<AbstractSpell> MAGNETIC_LEVITATION = register("magnetic_levitation", SpellRarity.UNCOMMON, 5, CastType.INSTANT, 12, 35, 10, 0, MagneticSpell.Behaviour.LEVITATION);
    public static final RegistryObject<AbstractSpell> NOTOR_SCAN = register("notor_scan", SpellRarity.UNCOMMON, 5, CastType.LONG, 10, 25, 10, 12, MagneticSpell.Behaviour.SCAN);
    public static final RegistryObject<AbstractSpell> BOUNDROID_DROP = register("boundroid_drop", SpellRarity.RARE, 5, CastType.LONG, 18, 80, 25, 20, MagneticSpell.Behaviour.BOUNDROID_DROP);
    public static final RegistryObject<AbstractSpell> FERROUSLIME_COHESION = register("ferrouslime_cohesion", SpellRarity.RARE, 5, CastType.LONG, 60, 90, 25, 30, MagneticSpell.Behaviour.FERROUSLIME_COHESION);
    public static final RegistryObject<AbstractSpell> TESLA_ARC_NODE = register("tesla_arc_node", SpellRarity.RARE, 5, CastType.LONG, 35, 90, 25, 20, MagneticSpell.Behaviour.TESLA_ARC_NODE);
    public static final RegistryObject<AbstractSpell> DIPOLE_VORTEX = register("dipole_vortex", SpellRarity.EPIC, 3, CastType.LONG, 35, 150, 40, 25, MagneticSpell.Behaviour.DIPOLE_VORTEX);

    // Independent magnetic combat spells with explicit level/rarity mappings.
    public static final RegistryObject<AbstractSpell> POLARITY_COLLAPSE = SPELLS.register("polarity_collapse", PolarityCollapseSpell::new);
    public static final RegistryObject<AbstractSpell> MAGNETIC_RAIL_LANCE = SPELLS.register("magnetic_rail_lance", MagneticRailLanceSpell::new);
    public static final RegistryObject<AbstractSpell> DIPOLE_ANVIL = SPELLS.register("dipole_anvil", DipoleAnvilSpell::new);
    public static final RegistryObject<AbstractSpell> MAGNETIZED_FRACTURE_CHAIN = SPELLS.register("magnetized_fracture_chain", MagnetizedFractureChainSpell::new);
    public static final RegistryObject<AbstractSpell> POLARITY_REVERSAL = SPELLS.register("polarity_reversal", PolarityReversalSpell::new);
    public static final RegistryObject<AbstractSpell> FERROUS_SANDSTORM = SPELLS.register("ferrous_sandstorm", FerrousSandstormSpell::new);

    private ModSpells() {
    }

    private static RegistryObject<AbstractSpell> register(String name, SpellRarity rarity, int maxLevel, CastType castType,
                                                          double cooldown, int baseMana, int manaPerLevel, int castTime,
                                                          MagneticSpell.Behaviour behaviour) {
        return SPELLS.register(name, () -> new MagneticSpell(name, rarity, maxLevel, castType, cooldown, baseMana, manaPerLevel, castTime, behaviour));
    }

}
