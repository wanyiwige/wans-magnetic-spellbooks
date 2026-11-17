package cn.wanyiwige.magneticspellbooks.util;

import cn.wanyiwige.magneticspellbooks.AlexCavesSpellsbook;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

public final class ModTags {
    public static final TagKey<Item> MAGNETIC_FOCUS = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(AlexCavesSpellsbook.MOD_ID, "magnetic_focus"));

    /** Alex's Caves ships this datapack tag but does not expose it as an ACTagRegistry field. */
    public static final TagKey<EntityType<?>> RESISTS_MAGNETS = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath("alexscaves", "resists_magnets"));

    private ModTags() {
    }
}
