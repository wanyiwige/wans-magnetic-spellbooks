package cn.wanyiwige.magneticspellbooks.item;

import cn.wanyiwige.magneticspellbooks.client.render.MagneticMageArmorRenderer;
import cn.wanyiwige.magneticspellbooks.registry.ModAttributes;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Consumer;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class MagneticMageArmorItem extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final ResourceLocation LAYER_ONE = new ResourceLocation(
            "wans_magnetic_spellbooks", "textures/models/armor/magnetic_mage_v2.png");
    private static final ResourceLocation LAYER_TWO = new ResourceLocation(
            "wans_magnetic_spellbooks", "textures/models/armor/magnetic_mage_v2.png");
    private final Multimap<Attribute, AttributeModifier> attributes;

    public MagneticMageArmorItem(Type type) {
        super(MagneticMageArmorMaterial.INSTANCE, type, new Item.Properties());
        ImmutableMultimap.Builder<Attribute, AttributeModifier> modifiers = ImmutableMultimap.builder();
        modifiers.putAll(super.getDefaultAttributeModifiers(type.getSlot()));
        add(modifiers, ModAttributes.MAGNETIC_SPELL_POWER.get(), type, magneticPower(type), AttributeModifier.Operation.MULTIPLY_BASE);
        add(modifiers, ModAttributes.MAGNETIC_MAGIC_RESIST.get(), type, magneticResist(type), AttributeModifier.Operation.MULTIPLY_BASE);
        add(modifiers, io.redspace.ironsspellbooks.api.registry.AttributeRegistry.MAX_MANA.get(), type, mana(type), AttributeModifier.Operation.ADDITION);
        if (type == Type.BOOTS) {
            add(modifiers, Attributes.MOVEMENT_SPEED, type, 0.03D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        }
        attributes = modifiers.build();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == getEquipmentSlot() ? attributes : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public String getArmorTexture(ItemStack stack, net.minecraft.world.entity.Entity entity,
                                  EquipmentSlot slot, @Nullable String type) {
        return (slot == EquipmentSlot.LEGS ? LAYER_TWO : LAYER_ONE).toString();
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private MagneticMageArmorRenderer model;

            @Override
            @OnlyIn(Dist.CLIENT)
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack stack,
                                                           EquipmentSlot slot, HumanoidModel<?> original) {
                if (model == null) {
                    model = new MagneticMageArmorRenderer();
                }
                model.prepForRender(livingEntity, stack, slot, original);
                return model;
            }
        });
    }

    private static void add(ImmutableMultimap.Builder<Attribute, AttributeModifier> modifiers,
                            Attribute attribute, Type type, double value, AttributeModifier.Operation operation) {
        UUID id = UUID.nameUUIDFromBytes(("magnetic_mage_" + type.getName() + "_" + attribute.getDescriptionId())
                .getBytes(StandardCharsets.UTF_8));
        modifiers.put(attribute, new AttributeModifier(id, "Magnetic mage armor", value, operation));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) { }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    private static int mana(Type type) {
        return switch (type) {
            case HELMET -> 35;
            case CHESTPLATE -> 80;
            case LEGGINGS -> 55;
            case BOOTS -> 30;
        };
    }

    private static double magneticPower(Type type) {
        return switch (type) {
            case HELMET -> 0.04D;
            case CHESTPLATE -> 0.08D;
            case LEGGINGS -> 0.06D;
            case BOOTS -> 0.02D;
        };
    }

    private static double magneticResist(Type type) {
        return switch (type) {
            case HELMET -> 0.03D;
            case CHESTPLATE -> 0.07D;
            case LEGGINGS -> 0.05D;
            case BOOTS -> 0.05D;
        };
    }
}
