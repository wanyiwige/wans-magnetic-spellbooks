package cn.wanyiwige.magneticspellbooks.client.render;

import cn.wanyiwige.magneticspellbooks.client.MagneticMageArmorModel;
import cn.wanyiwige.magneticspellbooks.item.MagneticMageArmorItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.RenderUtils;

public final class MagneticMageArmorRenderer extends GeoArmorRenderer<MagneticMageArmorItem> {
    public MagneticMageArmorRenderer() { super(new MagneticMageArmorModel()); }

    @Override
    protected void applyBaseTransformations(HumanoidModel<?> base) {
        super.applyBaseTransformations(base);
        getGeoModel().getBone("armorWaist").ifPresent(bone -> {
            RenderUtils.matchModelPartRot(base.body, bone);
            bone.updatePosition(base.body.x, -base.body.y, base.body.z);
        });
    }

    @Override
    protected void setAllBonesVisible(boolean visible) {
        super.setAllBonesVisible(visible);
        getGeoModel().getBone("armorWaist").ifPresent(bone -> setBoneVisible(bone, visible));
    }

    @Override
    protected void applyBoneVisibilityBySlot(EquipmentSlot slot) {
        super.applyBoneVisibilityBySlot(slot);
        getGeoModel().getBone("armorWaist").ifPresent(bone ->
                setBoneVisible(bone, slot == EquipmentSlot.LEGS && baseModel.body.visible));
    }

    @Override
    public void applyBoneVisibilityByPart(EquipmentSlot slot, ModelPart part, HumanoidModel<?> model) {
        super.applyBoneVisibilityByPart(slot, part, model);
        getGeoModel().getBone("armorWaist").ifPresent(bone ->
                setBoneVisible(bone, slot == EquipmentSlot.LEGS && part == model.body));
    }
}
