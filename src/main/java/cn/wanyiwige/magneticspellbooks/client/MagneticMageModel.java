package cn.wanyiwige.magneticspellbooks.client;

import cn.wanyiwige.magneticspellbooks.AlexCavesSpellsbook;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobModel;
import net.minecraft.resources.ResourceLocation;

/** v6 keeps Iron's bone names, so native walking/aiming/casting animations apply. */
public final class MagneticMageModel extends AbstractSpellCastingMobModel {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(
            AlexCavesSpellsbook.MOD_ID, "geo/magnetic_mage.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AlexCavesSpellsbook.MOD_ID, "textures/entity/magnetic_mage.png");

    @Override
    public ResourceLocation getModelResource(AbstractSpellCastingMob entity) { return MODEL; }

    @Override
    public ResourceLocation getTextureResource(AbstractSpellCastingMob entity) { return TEXTURE; }
}
