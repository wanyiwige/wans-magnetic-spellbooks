package cn.wanyiwige.magneticspellbooks.client.render;

import cn.wanyiwige.magneticspellbooks.AlexCavesSpellsbook;
import cn.wanyiwige.magneticspellbooks.entities.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.*;
import static cn.wanyiwige.magneticspellbooks.client.render.MagneticMeshes.*;

/** Six distinct staged scenes, sharing a fullbright depth-tested mesh batch. */
public class MagneticCombatRenderer<T extends MagneticCombatEntity> extends EntityRenderer<T> {
    protected static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath(AlexCavesSpellsbook.MOD_ID,"textures/entity/magnetic_mesh.png");
    public MagneticCombatRenderer(EntityRendererProvider.Context context) { super(context);shadowRadius=0; }
    @Override public boolean shouldRender(T entity,Frustum frustum,double x,double y,double z) {
        // Rail line and storm extend well beyond their small collision boxes.
        return entity.distanceToSqr(x,y,z)<128*128;
    }
    @Override public void render(T entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light) {
        float age=entity.renderAge(partial);
        double distance=Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceTo(entity.position());
        boolean detail=distance<32 && Minecraft.getInstance().options.particles().get()!=ParticleStatus.MINIMAL;
        VertexConsumer buffer=buffers.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
        if(entity instanceof MagneticRailLanceEntity) rail(entity,age,pose,buffer,distance,detail);
        else if(entity instanceof PolarityCollapseEntity) collapse(entity,age,pose,buffer,detail);
        else if(entity instanceof DipoleAnvilEntity) anvil(entity,age,pose,buffer,detail);
        else if(entity instanceof MagnetizedFractureChainEntity) chain(entity,age,pose,buffer,detail);
        else if(entity instanceof PolarityReversalTrackerEntity) reversal(entity,age,pose,buffer,detail);
        else if(entity instanceof FerrousSandstormEntity) storm(entity,age,pose,buffer,detail);
        super.render(entity,yaw,partial,pose,buffers,light);
    }
    protected Vec3 targetOffset(T entity,int id) {
        Entity target=entity.level().getEntity(id);
        return target==null?Vec3.ZERO:target.position().add(0,target.getBbHeight()*0.5,0).subtract(entity.position());
    }
    private int fading(T e,float age) {
        return e.isFinished()?Math.max(0,(int)(255*(1-(age-e.getVisual().getInt("FinishAge"))/8))):255;
    }
    private void shock(T e,float age,PoseStack p,VertexConsumer b,double radius) {
        float t=Math.max(0,(age-e.getVisual().getInt("FinishAge"))/8);
        ring(p,b,Vec3.ZERO,radius*t,0.07,0,age*0.1,RED,(int)(200*(1-t)),32);
        ring(p,b,new Vec3(0,0.08,0),radius*t*0.85,0.06,0,-age*0.1,BLUE,(int)(200*(1-t)),32);
        if(t<0.4)core(p,b,Vec3.ZERO,0.3+t,WHITE,(int)(255*(1-t)));
    }
    private void collapse(T e,float age,PoseStack p,VertexConsumer b,boolean detail) {
        if(e.isFinished()) { shock(e,age,p,b,e.getVisual().getFloat("Radius"));return; }
        double radius=age<10?1.3:Math.max(0.15,1.3*(20-age)/10);
        ring(p,b,Vec3.ZERO,radius,0.06,1.0,age*0.14,RED,230,detail?32:16);
        ring(p,b,Vec3.ZERO,radius*1.1,0.05,-1.0,-age*0.14,BLUE,230,detail?32:16);
        if(detail) {
            ring(p,b,Vec3.ZERO,radius*1.2,0.025,0,age*0.08,WHITE,110,24);
            Vec3 hand=targetOffset(e,e.getVisual().getInt("OwnerId"));
            core(p,b,hand.add(-0.2,0.45,0),0.09,RED,220);
            core(p,b,hand.add(0.2,0.45,0),0.09,BLUE,220);
            for(int i=0;i<6;i++) {
                double a=age*0.18+i*Math.PI/3;
                beam(p,b,new Vec3(Math.cos(a)*radius,Math.sin(a*2)*0.5,Math.sin(a)*radius),Vec3.ZERO,0.015,(i&1)==0?RED:BLUE,180);
            }
        }
        core(p,b,Vec3.ZERO,0.07+age*0.005,WHITE,200);
    }
    private void anvil(T e,float age,PoseStack p,VertexConsumer b,boolean detail) {
        if(e.isFinished()) { shock(e,age,p,b,e.getVisual().getFloat("Radius"));return; }
        boolean magnetic=e.getVisual().getBoolean("Magnetic");
        double cycle=magnetic && age<31?((age-1)%10)/10:Math.max(0,(31-age)/31);
        double gap=0.18+0.9*Math.abs(Math.sin(cycle*Math.PI));
        double scale=e.getVisual().contains("Scale")?e.getVisual().getFloat("Scale"):1;
        for(int i=0;i<2;i++) {
            int color=i==0?RED:BLUE;double sign=i==0?1:-1;
            Vec3 center=new Vec3(0,sign*gap*scale,0);
            box(p,b,center,0.6*scale,0.13*scale,0.45*scale,METAL,255);
            box(p,b,center.add(0,sign*0.2*scale,0),0.32*scale,0.1*scale,0.28*scale,color,240);
            ring(p,b,center,0.72*scale,0.035,0,sign*age*0.08,color,200,detail?28:14);
            ring(p,b,new Vec3(0,sign*1.2*scale,0),0.95*scale,0.025,0,-sign*age*0.05,color,160,24);
            if(detail) for(int j=0;j<4;j++) {
                double a=j*Math.PI/2+sign*age*0.08;
                box(p,b,center.add(Math.cos(a)*0.55*scale,sign*0.16*scale,Math.sin(a)*0.4*scale),
                        0.05*scale,0.08*scale,0.05*scale,color,255);
            }
        }
        if(magnetic&&age<31&&((age-1)%10)<2)core(p,b,Vec3.ZERO,0.28,WHITE,230);
    }
    private void chain(T e,float age,PoseStack p,VertexConsumer b,boolean detail) {
        int[] nodes=e.getVisual().getIntArray("Nodes");int alpha=fading(e,age);
        for(int i=0;i<nodes.length;i++) {
            Vec3 offset=targetOffset(e,nodes[i]);
            if(nodes[i]<0)continue;
            ring(p,b,offset,0.45,0.04,1.0,age*0.14,(i&1)==0?RED:BLUE,alpha,18);
            core(p,b,offset,0.09,WHITE,alpha);
            if(i==0)continue;
            double length=offset.length();int segments=Math.min(detail?24:8,Math.max(2,(int)(length*3)));
            Vec3 last=Vec3.ZERO;
            for(int j=1;j<=segments;j++) {
                double t=(double)j/segments;
                Vec3 point=offset.scale(t).add(0,Math.sin(t*Math.PI)*Math.sin(age*0.25+j)*0.06,0);
                beam(p,b,last,point,age<18?0.05:0.075,((j+(int)age/2)&1)==0?RED:BLUE,alpha);
                if(detail&&j%2==0)core(p,b,point,0.045,WHITE,alpha/2);
                last=point;
            }
        }
    }
    private void reversal(T e,float age,PoseStack p,VertexConsumer b,boolean detail) {
        if(e.isFinished()) { shock(e,age,p,b,1.5);return; }
        boolean push=age>=4;double radius=push?Math.min(1.4,0.2+(age-4)*0.12):1.4-age*0.25;
        ring(p,b,Vec3.ZERO,radius,0.055,1.1,age*0.2,push?BLUE:RED,220,detail?32:16);
        if(detail) {
            Vec3 hand=targetOffset(e,e.getVisual().getInt("OwnerId"));
            Vec3 direction=hand.normalize();
            for(int i=0;i<4;i++)beam(p,b,direction.scale(i*0.3),direction.scale((i+1)*0.3),0.025,
                    (i&1)==0?RED:BLUE,180-i*30);
        }
    }
    private void storm(T e,float age,PoseStack p,VertexConsumer b,boolean detail) {
        double radius=e.getVisual().getFloat("Radius");
        if(e.isFinished()) { shock(e,age,p,b,radius);return; }
        ring(p,b,new Vec3(0,0.06,0),radius,0.05,0,age*0.05,RED,170,detail?48:24);
        ring(p,b,new Vec3(0,0.09,0),radius*0.82,0.04,0,-age*0.05,BLUE,170,detail?48:24);
        for(int i=0;i<2;i++) {
            Vec3 last=new Vec3(radius*0.55,0,0);
            for(int j=1;j<=32;j++) {
                double h=j/8.0,angle=(i==0?1:-1)*(age*0.13+h*3.0);
                Vec3 point=new Vec3(Math.cos(angle)*radius*0.55,h,Math.sin(angle)*radius*0.55);
                beam(p,b,last,point,0.035,i==0?RED:BLUE,135);last=point;
            }
        }
        for(int i=0;i<(detail?24:8);i++) {
            double angle=age*0.15+i*2.4,rad=radius*(0.15+(i%5)*0.12),h=(i*0.47+age*0.035)%3.8;
            Vec3 point=new Vec3(Math.cos(angle)*rad,h,Math.sin(angle)*rad);
            int id=e.getVisual().getInt("DeflectId");
            if(id>0 && i%3==0)point=point.lerp(targetOffset(e,id),0.2);
            box(p,b,point,0.04+(i%3)*0.02,0.035,0.03,METAL,220);
        }
        core(p,b,new Vec3(0,1.6,0),0.12,WHITE,100);
    }
    private void rail(T e,float age,PoseStack p,VertexConsumer b,double distance,boolean detail) {
        CompoundTag v=e.getVisual();
        Vec3 origin=new Vec3(v.getDouble("OriginX"),v.getDouble("OriginY"),v.getDouble("OriginZ")).subtract(e.position());
        Vec3 direction=new Vec3(v.getDouble("DirX"),v.getDouble("DirY"),v.getDouble("DirZ")).normalize();
        Vec3 side=direction.cross(new Vec3(0,1,0));if(side.lengthSqr()<0.001)side=direction.cross(new Vec3(1,0,0));
        side=side.normalize();Vec3 up=direction.cross(side).normalize();
        int alpha=e.isFinished()?Math.max(0,(int)(255*(1-(age-v.getInt("FinishAge"))/6))):255;
        if(distance>64) { core(p,b,Vec3.ZERO,0.16,WHITE,alpha);return; }
        // All endpoints stop at the server's swept first collision; the render type also depth-tests.
        beam(p,b,origin,Vec3.ZERO,0.025,WHITE,alpha);
        int segments=detail?Math.min(48,Math.max(2,(int)v.getDouble("Travel")*2)):2;
        for(int pole=0;pole<2;pole++) {
            Vec3 last=origin;double sign=pole==0?1:-1;
            for(int i=0;i<=segments;i++) {
                double t=(double)i/segments;
                double amplitude=0.14+0.03*Math.sin(age*0.3+t*8);
                Vec3 point=origin.lerp(Vec3.ZERO,t).add(side.scale(sign*amplitude*Math.sin(Math.PI*t)));
                if(i>0)beam(p,b,last,point,0.028,pole==0?RED:BLUE,alpha*3/4);
                last=point;
            }
        }
        if(!e.isFinished()) {
            beam(p,b,direction.scale(-0.8),direction.scale(0.18),0.055,METAL,alpha);
            core(p,b,direction.scale(0.16),0.06,WHITE,alpha);
            if(detail) {
                Vec3 last=direction.scale(-1.4);
                for(int i=1;i<=16;i++) {
                    double t=i/16.0,a=age*0.4+t*Math.PI*4;
                    Vec3 point=direction.scale(-1.4+1.4*t).add(side.scale(Math.cos(a)*0.11)).add(up.scale(Math.sin(a)*0.11));
                    beam(p,b,last,point,0.015,(i&1)==0?RED:BLUE,alpha/2);last=point;
                }
            }
        }
        if(e.isFinished()||(v.contains("HitAge")&&age-v.getInt("HitAge")<3)) {
            Vec3 hit=e.isFinished()?Vec3.ZERO:new Vec3(v.getDouble("HitX"),v.getDouble("HitY"),v.getDouble("HitZ")).subtract(e.position());
            core(p,b,hit,0.17,WHITE,alpha);
            ring(p,b,hit,0.55,0.04,Math.PI/2,age*0.1,BLUE,alpha,24);
            for(int i=0;i<8;i++) {
                double a=i*Math.PI/4;
                Vec3 spread=side.scale(Math.cos(a)*0.4).add(up.scale(Math.sin(a)*0.4));
                beam(p,b,hit,hit.add(direction.scale(0.35)).add(spread),0.012,(i&1)==0?RED:BLUE,alpha/2);
            }
        }
    }
    @Override public ResourceLocation getTextureLocation(T entity) { return TEXTURE; }
}

