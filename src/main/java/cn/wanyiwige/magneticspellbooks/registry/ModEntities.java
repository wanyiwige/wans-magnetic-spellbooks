package cn.wanyiwige.magneticspellbooks.registry;

import cn.wanyiwige.magneticspellbooks.AlexCavesSpellsbook;
import cn.wanyiwige.magneticspellbooks.entities.HeavyweightSpellEntity;
import cn.wanyiwige.magneticspellbooks.entities.SpellMagneticWeaponEntity;
import cn.wanyiwige.magneticspellbooks.entities.SummonedFerrouslimeEntity;
import cn.wanyiwige.magneticspellbooks.entities.TeslaArcNodeEntity;
import cn.wanyiwige.magneticspellbooks.entities.DipoleVortexEntity;
import cn.wanyiwige.magneticspellbooks.entities.MagneticMageEntity;
import cn.wanyiwige.magneticspellbooks.entities.PolarityCollapseEntity;
import cn.wanyiwige.magneticspellbooks.entities.MagneticRailLanceEntity;
import cn.wanyiwige.magneticspellbooks.entities.DipoleAnvilEntity;
import cn.wanyiwige.magneticspellbooks.entities.MagnetizedFractureChainEntity;
import cn.wanyiwige.magneticspellbooks.entities.PolarityReversalTrackerEntity;
import cn.wanyiwige.magneticspellbooks.entities.FerrousSandstormEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AlexCavesSpellsbook.MOD_ID);

    public static final RegistryObject<EntityType<MagneticMageEntity>> MAGNETIC_MAGE =
            ENTITY_TYPES.register("magnetic_mage", () -> EntityType.Builder
                    .of(MagneticMageEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).updateInterval(3)
                    .build("magnetic_mage"));

    public static final RegistryObject<EntityType<SpellMagneticWeaponEntity>> SPELL_MAGNETIC_WEAPON =
            ENTITY_TYPES.register("spell_magnetic_weapon", () -> EntityType.Builder
                    .of(SpellMagneticWeaponEntity::new, MobCategory.MISC)
                    .sized(0.6F, 0.6F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("spell_magnetic_weapon"));

    public static final RegistryObject<EntityType<HeavyweightSpellEntity>> HEAVYWEIGHT_SPELL =
            ENTITY_TYPES.register("heavyweight_spell", () -> EntityType.Builder
                    .of(HeavyweightSpellEntity::new, MobCategory.MISC)
                    .sized(0.9F, 0.9F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("heavyweight_spell"));

    public static final RegistryObject<EntityType<SummonedFerrouslimeEntity>> SUMMONED_FERROUSLIME =
            ENTITY_TYPES.register("summoned_ferrouslime", () -> EntityType.Builder
                    .of(SummonedFerrouslimeEntity::new, MobCategory.CREATURE)
                    .sized(0.9F, 0.9F)
                    .clientTrackingRange(10)
                    .updateInterval(2)
                    .build("summoned_ferrouslime"));

    public static final RegistryObject<EntityType<TeslaArcNodeEntity>> TESLA_ARC_NODE =
            ENTITY_TYPES.register("tesla_arc_node", () -> EntityType.Builder
                    .of(TeslaArcNodeEntity::new, MobCategory.MISC)
                    .sized(0.8F, 1.1F)
                    .clientTrackingRange(12)
                    .updateInterval(1)
                    .build("tesla_arc_node"));

    public static final RegistryObject<EntityType<DipoleVortexEntity>> DIPOLE_VORTEX =
            ENTITY_TYPES.register("dipole_vortex", () -> EntityType.Builder
                    .of(DipoleVortexEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(12)
                    .updateInterval(1)
                    .build("dipole_vortex"));

    public static final RegistryObject<EntityType<PolarityCollapseEntity>> POLARITY_COLLAPSE =
            ENTITY_TYPES.register("polarity_collapse", () -> EntityType.Builder
                    .<PolarityCollapseEntity>of(PolarityCollapseEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(16).updateInterval(1).build("polarity_collapse"));

    public static final RegistryObject<EntityType<MagneticRailLanceEntity>> MAGNETIC_RAIL_LANCE =
            ENTITY_TYPES.register("magnetic_rail_lance", () -> EntityType.Builder
                    .<MagneticRailLanceEntity>of(MagneticRailLanceEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(16).updateInterval(1).build("magnetic_rail_lance"));

    public static final RegistryObject<EntityType<DipoleAnvilEntity>> DIPOLE_ANVIL =
            ENTITY_TYPES.register("dipole_anvil", () -> EntityType.Builder
                    .<DipoleAnvilEntity>of(DipoleAnvilEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(16).updateInterval(1).build("dipole_anvil"));

    public static final RegistryObject<EntityType<MagnetizedFractureChainEntity>> MAGNETIZED_FRACTURE_CHAIN =
            ENTITY_TYPES.register("magnetized_fracture_chain", () -> EntityType.Builder
                    .<MagnetizedFractureChainEntity>of(MagnetizedFractureChainEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(16).updateInterval(1).build("magnetized_fracture_chain"));

    public static final RegistryObject<EntityType<PolarityReversalTrackerEntity>> POLARITY_REVERSAL_TRACKER =
            ENTITY_TYPES.register("polarity_reversal_tracker", () -> EntityType.Builder
                    .<PolarityReversalTrackerEntity>of(PolarityReversalTrackerEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(16).updateInterval(1).build("polarity_reversal_tracker"));

    public static final RegistryObject<EntityType<FerrousSandstormEntity>> FERROUS_SANDSTORM =
            ENTITY_TYPES.register("ferrous_sandstorm", () -> EntityType.Builder
                    .<FerrousSandstormEntity>of(FerrousSandstormEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(16).updateInterval(1).build("ferrous_sandstorm"));

    private ModEntities() {
    }
}
