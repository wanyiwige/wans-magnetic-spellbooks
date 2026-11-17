package cn.wanyiwige.magneticspellbooks.client.render;

import cn.wanyiwige.magneticspellbooks.entities.TeslaArcNodeEntity;
import com.github.alexmodguy.alexscaves.client.model.TeslaBulbModel;
import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class TeslaArcNodeRenderer extends EntityRenderer<TeslaArcNodeEntity> {
    private static final TeslaBulbModel MODEL = new TeslaBulbModel();
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("alexscaves", "textures/entity/tesla_bulb.png");

    public TeslaArcNodeRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.35F;
    }

    @Override
    public void render(TeslaArcNodeEntity entity, float yaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.05D, 0.0D);
        if (!entity.isCharging()) {
            float flightYaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
            float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
            // The placed Tesla bulb points along local +Y after the model's X flip.
            // Align that axis with velocity without adding any roll or continuous spin.
            poseStack.mulPose(Axis.YP.rotationDegrees(flightYaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F - pitch));
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        float age = entity.tickCount + partialTick;
        float chargePulse = entity.isCharging() ? 1.5F + (float) Math.sin(age * 0.32F) * 1.5F : 0.0F;
        MODEL.setupAnim(entity, 0.0F, chargePulse, age, 0.0F, 0.0F);
        VertexConsumer consumer = buffer.getBuffer(ACRenderTypes.getTeslaBulb(TEXTURE));
        MODEL.renderToBuffer(poseStack, consumer, LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(TeslaArcNodeEntity entity) {
        return TEXTURE;
    }
}
