package cn.wanyiwige.magneticspellbooks.gametest;
import cn.wanyiwige.magneticspellbooks.AlexCavesSpellsbook;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.Set;

/** Explicit opt-in only for the isolated production test server. */
@Mod.EventBusSubscriber(modid=AlexCavesSpellsbook.MOD_ID)
public final class MagneticGameTestBootstrap {
    private static boolean registered;
    private MagneticGameTestBootstrap() { }
    @SubscribeEvent public static void commands(RegisterCommandsEvent event) {
        if(!Boolean.getBoolean("wan.magneticGameTest"))return;
        if(!registered) {
            for(var method:MagneticCombatGameTests.class.getDeclaredMethods()) {
                if(method.isAnnotationPresent(GameTest.class))GameTestRegistry.register(method,Set.of(AlexCavesSpellsbook.MOD_ID));
            }
            registered=true;
        }
        TestCommand.register(event.getDispatcher());
    }
}

