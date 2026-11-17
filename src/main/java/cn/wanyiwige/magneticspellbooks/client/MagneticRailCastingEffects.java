package cn.wanyiwige.magneticspellbooks.client;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.DustParticleOptions;
import org.joml.Vector3f;

/** Twelve-tick paired launch rails with a bright focus; only a bounded cast burst. */
public final class MagneticRailCastingEffects {
    private MagneticRailCastingEffects() { }
    public static void charge(Level level,LivingEntity caster) {
        Vec3 origin=caster.getEyePosition(),direction=caster.getLookAngle().normalize();
        Vec3 side=direction.cross(new Vec3(0,1,0));
        if(side.lengthSqr()<0.001)side=direction.cross(new Vec3(1,0,0));
        side=side.normalize();
        for(int pole=0;pole<2;pole++) {
            var color=pole==0?new Vector3f(1,0.22F,0.3F):new Vector3f(0.2F,0.6F,1);
            for(int i=1;i<=8;i++) {
                Vec3 p=origin.add(direction.scale(i*0.18)).add(side.scale(pole==0?0.14:-0.14));
                level.addParticle(new DustParticleOptions(color,0.8F),p.x,p.y,p.z,0,0,0);
            }
        }
        Vec3 focus=origin.add(direction.scale(1.5));
        level.addParticle(new DustParticleOptions(new Vector3f(0.8F,1,1),1.2F),focus.x,focus.y,focus.z,0,0,0);
    }
}

