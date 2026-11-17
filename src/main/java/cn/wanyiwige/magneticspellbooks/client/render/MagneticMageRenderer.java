package cn.wanyiwige.magneticspellbooks.client.render;

import cn.wanyiwige.magneticspellbooks.client.MagneticMageModel;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public final class MagneticMageRenderer extends AbstractSpellCastingMobRenderer {
    public MagneticMageRenderer(EntityRendererProvider.Context context) {
        super(context, new MagneticMageModel());
    }
}
