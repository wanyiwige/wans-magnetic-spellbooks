package cn.wanyiwige.magneticspellbooks.client;

import cn.wanyiwige.magneticspellbooks.item.MagneticMageArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class MagneticMageArmorModel extends GeoModel<MagneticMageArmorItem> {
    private static ResourceLocation asset(String path) {
        return ResourceLocation.fromNamespaceAndPath("wans_magnetic_spellbooks", path);
    }
    @Override
    public ResourceLocation getModelResource(MagneticMageArmorItem item) {
        return asset("geo/magnetic_mage_armor.geo.json");
    }
    @Override
    public ResourceLocation getTextureResource(MagneticMageArmorItem item) {
        return asset("textures/models/armor/magnetic_mage_v2.png");
    }
    @Override
    public ResourceLocation getAnimationResource(MagneticMageArmorItem item) {
        return asset("animations/magnetic_mage_armor.animation.json");
    }
}
