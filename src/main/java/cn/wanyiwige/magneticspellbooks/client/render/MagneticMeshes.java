package cn.wanyiwige.magneticspellbooks.client.render;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;

/** Small batch mesh primitives: no independently ticking decoration entities. */
public final class MagneticMeshes {
    public static final int RED=0xff4059,BLUE=0x429fff,WHITE=0xe6ffff,METAL=0xa6bacf;
    private MagneticMeshes() { }
    private static void vertex(PoseStack pose,VertexConsumer buffer,Vec3 p,int color,int alpha,float u,float v) {
        buffer.vertex(pose.last().pose(),(float)p.x,(float)p.y,(float)p.z)
                .color((color>>16)&255,(color>>8)&255,color&255,alpha).uv(u,v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(pose.last().normal(),0,1,0).endVertex();
    }
    public static void quad(PoseStack pose,VertexConsumer buffer,Vec3 a,Vec3 b,Vec3 c,Vec3 d,int color,int alpha) {
        vertex(pose,buffer,a,color,alpha,0,0);vertex(pose,buffer,b,color,alpha,0,1);
        vertex(pose,buffer,c,color,alpha,1,1);vertex(pose,buffer,d,color,alpha,1,0);
    }
    public static void beam(PoseStack pose,VertexConsumer buffer,Vec3 from,Vec3 to,double width,int color,int alpha) {
        Vec3 delta=to.subtract(from);if(delta.lengthSqr()<1e-8)return;
        Vec3 side=delta.normalize().cross(new Vec3(0,1,0));
        if(side.lengthSqr()<0.001)side=delta.normalize().cross(new Vec3(1,0,0));
        side=side.normalize().scale(width);
        Vec3 up=delta.normalize().cross(side.normalize()).scale(width);
        quad(pose,buffer,from.add(side),from.subtract(side),to.subtract(side),to.add(side),color,alpha);
        quad(pose,buffer,from.add(up),from.subtract(up),to.subtract(up),to.add(up),color,alpha);
    }
    public static void ring(PoseStack pose,VertexConsumer buffer,Vec3 center,double radius,double width,
                            double pitch,double phase,int color,int alpha,int segments) {
        for(int i=0;i<segments;i++) {
            double a=phase+i*Math.PI*2/segments,b=phase+(i+1)*Math.PI*2/segments;
            beam(pose,buffer,center.add(point(radius,a,pitch)),center.add(point(radius,b,pitch)),width,color,alpha);
        }
    }
    private static Vec3 point(double radius,double angle,double pitch) {
        return new Vec3(Math.cos(angle)*radius,Math.sin(angle)*radius*Math.sin(pitch),Math.sin(angle)*radius*Math.cos(pitch));
    }
    public static void core(PoseStack pose,VertexConsumer buffer,Vec3 center,double radius,int color,int alpha) {
        for(int i=0;i<6;i++) for(int j=0;j<10;j++) {
            double a=-Math.PI/2+Math.PI*i/6,b=-Math.PI/2+Math.PI*(i+1)/6;
            double c=j*Math.PI*2/10,d=(j+1)*Math.PI*2/10;
            quad(pose,buffer,center.add(sphere(radius,a,c)),center.add(sphere(radius,b,c)),
                    center.add(sphere(radius,b,d)),center.add(sphere(radius,a,d)),color,alpha);
        }
    }
    private static Vec3 sphere(double r,double a,double b) { return new Vec3(Math.cos(a)*Math.cos(b)*r,Math.sin(a)*r,Math.cos(a)*Math.sin(b)*r); }
    public static void box(PoseStack pose,VertexConsumer buffer,Vec3 center,double x,double y,double z,int color,int alpha) {
        Vec3[] p=new Vec3[8];
        for(int i=0;i<8;i++)p[i]=center.add((i&1)==0?-x:x,(i&2)==0?-y:y,(i&4)==0?-z:z);
        int[][] faces={{0,1,3,2},{4,6,7,5},{0,4,5,1},{2,3,7,6},{0,2,6,4},{1,5,7,3}};
        for(int[] f:faces)quad(pose,buffer,p[f[0]],p[f[1]],p[f[2]],p[f[3]],color,alpha);
    }
}

