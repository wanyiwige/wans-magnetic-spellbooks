package cn.wanyiwige.magneticspellbooks.spells;
import cn.wanyiwige.magneticspellbooks.entities.*;
import cn.wanyiwige.magneticspellbooks.registry.ModEntities;
import cn.wanyiwige.magneticspellbooks.util.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import java.util.List;
import java.util.Locale;

/** Selection and spawning shared by six independent spell registry classes. */
final class MagneticCombatCasting {
    private MagneticCombatCasting() { }
    private static String damage(float value) { return String.format(Locale.ROOT,"%.1f",value); }
    static List<MutableComponent> info(String id,int level,float power) {
        String key="spell.wans_magnetic_spellbooks."+id;
        MutableComponent values=switch(id) {
            case "polarity_collapse" -> Component.translatable(key+".damage",
                    damage(MagneticCombatRules.collapse(level,0)*power),
                    damage((MagneticCombatRules.collapse(level,1)-MagneticCombatRules.collapse(level,0))*power));
            case "magnetic_rail_lance" -> Component.translatable(key+".damage",
                    damage(MagneticCombatRules.rail(level)*power),damage(MagneticCombatRules.rail(level)*power*1.5F));
            case "dipole_anvil" -> Component.translatable(key+".damage",
                    damage(MagneticCombatRules.anvilPulse(level)*power),damage(MagneticCombatRules.anvilFinish(level)*power));
            case "magnetized_fracture_chain" -> Component.translatable(key+".damage",
                    damage(MagneticCombatRules.chain(level,0)*power),damage(MagneticCombatRules.chain(level,0)*power*0.25F));
            case "polarity_reversal" -> Component.translatable(key+".damage",
                    damage(MagneticCombatRules.reversal(level)*power),damage(MagneticCombatRules.collision(level)*power));
            default -> Component.translatable(key+".damage",
                    damage(MagneticCombatRules.stormPulse(level)*power),damage(MagneticCombatRules.stormFinish(level)*power));
        };
        return List.of(values,Component.translatable(key+".description"));
    }
    static boolean canCast(String id,Level level,int spellLevel,LivingEntity owner) {
        if(!owner.isAlive())return false;
        double range=MagneticCombatRules.range(id,spellLevel);
        if(id.equals("magnetic_rail_lance"))return true;
        if(id.equals("ferrous_sandstorm")) {
            return level.getEntitiesOfClass(FerrousSandstormEntity.class,owner.getBoundingBox().inflate(96),
                    e->owner.getUUID().equals(e.getOwnerUUID())).isEmpty()
                && !hasStormElsewhere(owner);
        }
        // Enemy potion effects and scan ownership are not reliable client prediction data.
        // The server performs selection again before creating an attack.
        if(level.isClientSide)return true;
        return TargetingFeedback.requireTarget(owner,
                MagneticCombatSupport.select(level,owner,range,false)!=null);
    }
    private static boolean hasStormElsewhere(LivingEntity owner) {
        if(!(owner.level() instanceof net.minecraft.server.level.ServerLevel server))return false;
        for(var entity:server.getAllEntities()) {
            if(entity instanceof FerrousSandstormEntity storm && owner.getUUID().equals(storm.getOwnerUUID()))return true;
        }
        return false;
    }
    static boolean cast(String id,Level level,int spellLevel,LivingEntity owner,float power) {
        spellLevel=MagneticCombatRules.level(spellLevel,
                id.equals("polarity_collapse")||id.equals("magnetized_fracture_chain")||id.equals("ferrous_sandstorm")?3:5);
        if(level.isClientSide||!canCast(id,level,spellLevel,owner))return false;
        LivingEntity target=MagneticCombatSupport.select(level,owner,MagneticCombatRules.range(id,spellLevel),false);
        MagneticCombatEntity entity=switch(id) {
            case "polarity_collapse" -> ModEntities.POLARITY_COLLAPSE.get().create(level);
            case "magnetic_rail_lance" -> ModEntities.MAGNETIC_RAIL_LANCE.get().create(level);
            case "dipole_anvil" -> ModEntities.DIPOLE_ANVIL.get().create(level);
            case "magnetized_fracture_chain" -> ModEntities.MAGNETIZED_FRACTURE_CHAIN.get().create(level);
            case "polarity_reversal" -> ModEntities.POLARITY_REVERSAL_TRACKER.get().create(level);
            case "ferrous_sandstorm" -> ModEntities.FERROUS_SANDSTORM.get().create(level);
            default -> null;
        };
        if(entity==null)return false;
        Vec3 position=target==null?owner.getEyePosition():MagneticCombatSupport.center(target);
        if(id.equals("magnetic_rail_lance")) position=owner.getEyePosition();
        if(id.equals("ferrous_sandstorm")) {
            Vec3 eye=owner.getEyePosition();
            if(target!=null) position=target.position();
            else {
                Vec3 end=eye.add(owner.getLookAngle().scale(MagneticCombatRules.range(id,spellLevel)));
                BlockHitResult hit=level.clip(new ClipContext(eye,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,owner));
                position=hit.getLocation().add(0,0.1,0);
            }
        }
        entity.setPos(position);entity.configure(owner,target,spellLevel,power);
        if(!level.addFreshEntity(entity))return false;
        entity.afterSpawn(owner);
        return true;
    }
}

