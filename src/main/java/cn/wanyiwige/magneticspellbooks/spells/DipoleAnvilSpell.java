package cn.wanyiwige.magneticspellbooks.spells;

import cn.wanyiwige.magneticspellbooks.AlexCavesSpellsbook;
import cn.wanyiwige.magneticspellbooks.registry.ModSchools;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;

/** Magnetic spell with an independent server entity and staged client rendering. */
public final class DipoleAnvilSpell extends AbstractSpell {
    public static final int MAX_LEVEL = 5;
    private static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(AlexCavesSpellsbook.MOD_ID, "dipole_anvil");
    private static final SpellRarity[] RARITIES = {
            SpellRarity.RARE, SpellRarity.RARE, SpellRarity.EPIC, SpellRarity.LEGENDARY, SpellRarity.LEGENDARY
    };
    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(ModSchools.MAGNETIC_RESOURCE)
            .setMaxLevel(MAX_LEVEL)
            .setCooldownSeconds(16)
            .setAllowCrafting(true)
            .build();

    public DipoleAnvilSpell() {
        baseManaCost = 65;
        manaCostPerLevel = 15;
        baseSpellPower = 1;
        spellPowerPerLevel = 0;
        castTime = 20;
    }

    @Override
    public ResourceLocation getSpellResource() { return ID; }

    @Override
    public DefaultConfig getDefaultConfig() { return defaultConfig; }

    @Override
    public CastType getCastType() { return CastType.LONG; }

    @Override
    public SpellRarity getRarity(int spellLevel) {
        return RARITIES[Math.max(0, Math.min(RARITIES.length - 1, spellLevel - 1))];
    }

    @Override
    public int getMaxRarity() { return SpellRarity.LEGENDARY.getValue(); }

    @Override
    public int getMinLevelForRarity(SpellRarity rarity) {
        for (int i = 0; i < RARITIES.length; i++) {
            if (RARITIES[i] == rarity) return i + 1;
        }
        return 0;
    }

    public double getTargetRange(int spellLevel) {
        return 16.0D + 2.0D * (Math.max(1, Math.min(MAX_LEVEL, spellLevel)) - 1);
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity caster, MagicData magicData) {
        return super.checkPreCastConditions(level,spellLevel,caster,magicData)
                && MagneticCombatCasting.canCast(ID.getPath(),level,spellLevel,caster);
    }

    @Override
    public boolean allowLooting() { return true; }

    @Override
    public boolean allowCrafting() { return true; }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return MagneticCombatCasting.info(ID.getPath(),spellLevel,getSpellPower(spellLevel,caster));
    }

    @Override
    public void onCast(Level level,int spellLevel,LivingEntity caster,CastSource castSource,MagicData magicData) {
        if (!level.isClientSide && MagneticCombatCasting.cast(ID.getPath(),level,spellLevel,caster,getSpellPower(spellLevel,caster))) {
            super.onCast(level,spellLevel,caster,castSource,magicData);
        }
    }
}

