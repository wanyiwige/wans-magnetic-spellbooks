package cn.wanyiwige.magneticspellbooks.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

public final class MagneticLevitationEffect extends MobEffect {
    public MagneticLevitationEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x3A7BFF);
        // Use the same native flight attribute path as AttributesLib's FlyingEffect.
        Attribute flight = ForgeRegistries.ATTRIBUTES.getValue(
                ResourceLocation.fromNamespaceAndPath("attributeslib", "creative_flight"));
        if (flight != null) {
            addAttributeModifier(flight, "38d5ee77-ef8d-4e66-9139-1ed7490df2ad",
                    1.0D, AttributeModifier.Operation.ADDITION);
        }
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.fallDistance = 0.0F;
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
