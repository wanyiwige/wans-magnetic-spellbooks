package cn.wanyiwige.magneticspellbooks.client.render;

import cn.wanyiwige.magneticspellbooks.entities.DipoleVortexEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** Two black-hole-inspired, emissive magnetic monopole cores. */
public final class DipoleVortexRenderer extends EntityRenderer<DipoleVortexEntity> {
    private static final int LATITUDES = 8;
    private static final int LONGITUDES = 12;

    public DipoleVortexRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(DipoleVortexEntity entity, float yaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Vec3[] poles = entity.getPolePositions(partialTick);
        renderCore(entity, poles[0].subtract(entity.position()), partialTick,
                poseStack, buffer, 255, 48, 38, true);
        renderCore(entity, poles[1].subtract(entity.position()), partialTick,
                poseStack, buffer, 45, 112, 255, false);
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    private void renderCore(DipoleVortexEntity entity, Vec3 offset, float partialTick,
                            PoseStack poseStack, MultiBufferSource buffer,
                            int red, int green, int blue, boolean reverse) {
        float age = entity.tickCount + partialTick;
        float pulse = 1.0F + Mth.sin(age * 0.22F + (reverse ? 0.0F : Mth.PI)) * 0.08F;
        float coreSize = (0.82F + entity.getFieldRadius() * 0.025F) * pulse;

        poseStack.pushPose();
        poseStack.translate(offset.x, offset.y, offset.z);
        // The original black-hole image has black pixels, so tinting it cannot create red/blue cores.
        // Draw actual untextured glowing spheres whose vertex color is the visible surface color.
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        drawSphere(poseStack.last().pose(), consumer, coreSize * 1.1F,
                red, green, blue, 145);
        drawSphere(poseStack.last().pose(), consumer, coreSize * 0.82F,
                red, green, blue, 245);
        poseStack.popPose();
    }

    private static void drawSphere(Matrix4f matrix, VertexConsumer consumer, float radius,
                                   int red, int green, int blue, int alpha) {
        for (int latitude = 0; latitude < LATITUDES; latitude++) {
            double pitchA = -Math.PI * 0.5D + Math.PI * latitude / LATITUDES;
            double pitchB = -Math.PI * 0.5D + Math.PI * (latitude + 1) / LATITUDES;
            for (int longitude = 0; longitude < LONGITUDES; longitude++) {
                double yawA = Math.PI * 2.0D * longitude / LONGITUDES;
                double yawB = Math.PI * 2.0D * (longitude + 1) / LONGITUDES;
                sphereVertex(matrix, consumer, radius, pitchA, yawA, red, green, blue, alpha);
                sphereVertex(matrix, consumer, radius, pitchB, yawA, red, green, blue, alpha);
                sphereVertex(matrix, consumer, radius, pitchB, yawB, red, green, blue, alpha);
                sphereVertex(matrix, consumer, radius, pitchA, yawB, red, green, blue, alpha);
            }
        }
    }

    private static void sphereVertex(Matrix4f matrix, VertexConsumer consumer, float radius,
                                     double pitch, double yaw, int red, int green, int blue, int alpha) {
        float x = (float) (Math.cos(pitch) * Math.cos(yaw) * radius);
        float y = (float) (Math.sin(pitch) * radius);
        float z = (float) (Math.cos(pitch) * Math.sin(yaw) * radius);
        consumer.vertex(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(DipoleVortexEntity entity) {
        return ResourceLocation.fromNamespaceAndPath("minecraft", "textures/misc/white.png");
    }
}
