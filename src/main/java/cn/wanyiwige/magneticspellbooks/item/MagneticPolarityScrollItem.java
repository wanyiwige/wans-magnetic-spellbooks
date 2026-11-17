package cn.wanyiwige.magneticspellbooks.item;

import io.redspace.ironsspellbooks.item.Scroll;
import net.minecraft.world.item.Item;

/**
 * The Magnetic School's shared scroll item.  Its contained SpellData decides
 * both the spell and level, exactly like Iron's standard scroll.
 */
public class MagneticPolarityScrollItem extends Scroll {
    public MagneticPolarityScrollItem(Item.Properties properties) {
        super(properties);
    }
}
