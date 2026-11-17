package cn.wanyiwige.magneticspellbooks.client.render;

import cn.wanyiwige.magneticspellbooks.entities.HeavyweightSpellEntity;
import com.github.alexmodguy.alexscaves.client.model.BoundroidModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class HeavyweightSpellRenderer extends EntityRenderer<HeavyweightSpellEntity> {
    private static final ResourceLocation BOUNDROID_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "alexscaves", "textures/entity/boundroid.png");

    private final BoundroidModel model;

    public HeavyweightSpellRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new BoundroidModel();
        shadowRadius = 0.8F;
    }

    @Override
    public void render(HeavyweightSpellEntity entity, float yaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0D, -1.501D, 0.0D);
        model.animateForQuarry(entity.tickCount + partialTick, 0.0F);
        model.showChains();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(BOUNDROID_TEXTURE));
        model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(HeavyweightSpellEntity entity) {
        return BOUNDROID_TEXTURE;
    }
}
