package cn.wanyiwige.magneticspellbooks.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** Action-bar feedback for spells that require a living target. */
public final class TargetingFeedback {
    private TargetingFeedback() { }

    public static boolean requireTarget(LivingEntity caster, boolean targetFound) {
        if (!targetFound && caster instanceof Player player) {
            player.displayClientMessage(Component.translatable(
                    "ui.wans_magnetic_spellbooks.target_required").withStyle(ChatFormatting.RED), true);
        }
        return targetFound;
    }
}
