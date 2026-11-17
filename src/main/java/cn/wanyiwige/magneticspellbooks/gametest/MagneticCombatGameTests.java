package cn.wanyiwige.magneticspellbooks.gametest;

import cn.wanyiwige.magneticspellbooks.AlexCavesSpellsbook;
import cn.wanyiwige.magneticspellbooks.entities.*;
import cn.wanyiwige.magneticspellbooks.registry.ModEntities;
import cn.wanyiwige.magneticspellbooks.util.*;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.*;

/** Opt-in real-world server regression tests. No user save is read by the test server. */
@GameTestHolder(AlexCavesSpellsbook.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MagneticCombatGameTests {
    private static void floor(GameTestHelper helper) {
        for(int x=0;x<24;x++)for(int z=0;z<16;z++)helper.setBlock(x,0,z,Blocks.STONE);
    }
    private static Cow cow(GameTestHelper helper,int x,int z) {
        Cow cow=helper.spawnWithNoFreeWill(EntityType.COW,new Vec3(x+0.5,1,z+0.5));
        cow.setNoGravity(true);cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);cow.setHealth(1000);
        cow.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
        return cow;
    }
    private static void magnetic(LivingEntity target,int strength) {
        target.addEffect(new MobEffectInstance(ACEffectRegistry.MAGNETIZING.get(),1000,strength-1));
    }
    private static void health(GameTestHelper h,LivingEntity target,float damage,String label) {
        h.assertTrue(Math.abs(target.getHealth()-(1000-damage))<0.15,label+": health="+target.getHealth()+", expected="+(1000-damage));
    }
    private static <T extends MagneticCombatEntity> T start(GameTestHelper h,EntityType<T> type,Cow owner,Cow target,int level,Vec3 position) {
        T entity=type.create(h.getLevel());h.assertTrue(entity!=null,"entity factory");
        entity.setPos(position);entity.configure(owner,target,level,1);
        h.assertTrue(h.getLevel().addFreshEntity(entity),"spawn succeeded");entity.afterSpawn(owner);return entity;
    }

    @GameTest(template="magnetic_arena",timeoutTicks=80)
    public static void collapse_consumes_and_separates_area(GameTestHelper h) {
        floor(h);Cow owner=cow(h,2,4),target=cow(h,6,4),other=cow(h,8,4);magnetic(target,1);
        MagnetizationSynergyUtil.markScan(owner,target,180);
        var entity=start(h,ModEntities.POLARITY_COLLAPSE.get(),owner,target,1,MagneticCombatSupport.center(target));
        h.assertTrue(MagnetizationSynergyUtil.getStrength(target)==0,"consumes actual I on spawn");
        h.runAtTickTime(35,()->{health(h,target,75,"primary virtual II and 25% scan");health(h,other,20.4F,"area actual I without scan");h.assertTrue(entity.isRemoved(),"collapse reclaimed");h.succeed();});
    }

    @GameTest(template="magnetic_arena",timeoutTicks=80)
    public static void anvil_has_four_distinct_hits(GameTestHelper h) {
        floor(h);Cow owner=cow(h,2,4),target=cow(h,6,4);magnetic(target,1);
        var entity=start(h,ModEntities.DIPOLE_ANVIL.get(),owner,target,1,MagneticCombatSupport.center(target));
        h.runAtTickTime(45,()->{health(h,target,51,"three compressions plus final");h.assertTrue(entity.isRemoved(),"anvil reclaimed");h.succeed();});
    }

    @GameTest(template="magnetic_arena",timeoutTicks=80)
    public static void anvil_unmagnetized_gets_only_final(GameTestHelper h) {
        floor(h);Cow owner=cow(h,2,4),target=cow(h,6,4);
        start(h,ModEntities.DIPOLE_ANVIL.get(),owner,target,1,MagneticCombatSupport.center(target));
        h.runAtTickTime(45,()->{health(h,target,24,"unmagnetized final only");h.succeed();});
    }

    @GameTest(template="magnetic_arena",timeoutTicks=80)
    public static void fracture_has_finite_nodes_and_afterburst(GameTestHelper h) {
        floor(h);Cow owner=cow(h,2,4),main=cow(h,6,4),other=cow(h,9,4);magnetic(main,1);magnetic(other,1);
        var entity=start(h,ModEntities.MAGNETIZED_FRACTURE_CHAIN.get(),owner,main,1,MagneticCombatSupport.center(main));
        h.runAtTickTime(35,()->{health(h,main,45,"primary plus afterburst");health(h,other,45,"secondary plus afterburst");
            h.assertTrue(entity.getVisual().getIntArray("Nodes").length==2,"two finite nodes");h.assertTrue(entity.isRemoved(),"chain reclaimed");h.succeed();});
    }

    @GameTest(template="magnetic_arena",timeoutTicks=80)
    public static void rail_pierces_once_and_deduplicates(GameTestHelper h) {
        floor(h);Cow owner=cow(h,2,4),first=cow(h,6,4),second=cow(h,10,4),third=cow(h,14,4);
        magnetic(first,1);magnetic(second,1);owner.setYRot(-90);owner.setXRot(0);
        var entity=start(h,ModEntities.MAGNETIC_RAIL_LANCE.get(),owner,null,1,owner.getEyePosition());
        h.runAtTickTime(30,()->{health(h,first,45,"first magnetic hit");health(h,second,22.5F,"one piercing hit at half damage");health(h,third,0,"no second piercing at I");h.assertTrue(entity.isRemoved(),"rail reclaimed");h.succeed();});
    }

    @GameTest(template="magnetic_arena",timeoutTicks=80)
    public static void rail_stops_at_first_wall(GameTestHelper h) {
        floor(h);Cow owner=cow(h,2,4),behind=cow(h,10,4);owner.setYRot(-90);owner.setXRot(0);
        for(int y=1;y<=4;y++)for(int z=3;z<=5;z++)h.setBlock(7,y,z,Blocks.STONE);
        start(h,ModEntities.MAGNETIC_RAIL_LANCE.get(),owner,null,5,owner.getEyePosition());
        h.runAtTickTime(25,()->{health(h,behind,0,"wall blocks all damage");h.succeed();});
    }

    @GameTest(template="magnetic_arena",timeoutTicks=130)
    public static void storm_has_eight_pulses_and_one_final(GameTestHelper h) {
        floor(h);Cow owner=cow(h,2,4),target=cow(h,8,4);magnetic(target,1);
        var entity=start(h,ModEntities.FERROUS_SANDSTORM.get(),owner,target,1,target.position());
        h.runAtTickTime(100,()->{health(h,target,73.8F,"eight 4.725 pulses plus 36 final");h.assertTrue(entity.isRemoved(),"storm reclaimed");h.succeed();});
    }

    @GameTest(template="magnetic_arena",timeoutTicks=80)
    public static void reversal_direct_damage_and_timeout(GameTestHelper h) {
        floor(h);Cow owner=cow(h,2,4),target=cow(h,6,4);
        var entity=start(h,ModEntities.POLARITY_REVERSAL_TRACKER.get(),owner,target,1,MagneticCombatSupport.center(target));
        h.runAtTickTime(35,()->{health(h,target,18,"unmagnetized direct only");h.assertTrue(entity.isRemoved(),"tracker reclaimed");h.succeed();});
    }

    @GameTest(template="magnetic_arena",timeoutTicks=40)
    public static void scan_isolated_owners(GameTestHelper h) {
        floor(h);Cow a=cow(h,2,4),b=cow(h,2,8),target=cow(h,7,4);magnetic(target,3);
        MagnetizationSynergyUtil.markScan(a,target,100);MagnetizationSynergyUtil.markScan(b,target,100);
        var first=start(h,ModEntities.POLARITY_COLLAPSE.get(),a,target,1,MagneticCombatSupport.center(target));
        h.assertTrue(!MagnetizationSynergyUtil.hasScanReward(a,target),"a consumed own mark");
        h.assertTrue(MagnetizationSynergyUtil.hasScanReward(b,target),"b retains mark");
        h.assertTrue(MagnetizationSynergyUtil.reserveForAttack(a,target,first).effectiveStrength()==4,"snapshot virtual IV");
        first.discard();h.succeed();
    }
}

