package cn.wanyiwige.magneticspellbooks.entities;
import cn.wanyiwige.magneticspellbooks.registry.ModSpells;
import cn.wanyiwige.magneticspellbooks.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.*;
import java.util.*;

public final class MagnetizedFractureChainEntity extends MagneticCombatEntity {
    private final List<UUID> nodes = new ArrayList<>();
    private int[] strengths = new int[0];
    public MagnetizedFractureChainEntity(EntityType<?> type, Level level) { super(type,level); maximumAge=29; }
    @Override public void configure(LivingEntity owner, LivingEntity target, int spellLevel, float power) {
        super.configure(owner,target,spellLevel,power);
        List<LivingEntity> targets = new ArrayList<>(); targets.add(target);
        targets.addAll(MagneticCombatSupport.chainTargets(level(),owner,target,spellLevel));
        strengths = new int[targets.size()];
        for (int i=0;i<targets.size();i++) { nodes.add(targets.get(i).getUUID()); strengths[i]=MagnetizationSynergyUtil.getStrength(targets.get(i)); }
        updateNodes();
    }
    private void updateNodes() {
        CompoundTag visual=getVisual().copy();
        visual.putIntArray("Nodes", nodes.stream().map(this::living).mapToInt(e->e==null?-1:e.getId()).toArray());
        setVisual(visual);
    }
    @Override protected void serverTick(ServerLevel level, LivingEntity owner) {
        if (finished) return;
        LivingEntity main=primary();
        if (main==null && age<=10) { discard();return; }
        if (main!=null && (!validPrimary(owner,main) || owner.distanceToSqr(main)>Math.pow(MagneticCombatRules.range("magnetized_fracture_chain",spellLevel),2))) { discard();return; }
        if(main!=null)setPos(MagneticCombatSupport.center(main));
        updateNodes();
        if(age==1) MagneticCombatEffects.charge(level,position(),1.2F);
        if(age==10 || age==20) {
            for(int i=0;i<nodes.size();i++) {
                LivingEntity target=living(nodes.get(i));
                if(!MagneticSpellUtil.canTargetEnemy(owner,target) || !MagneticCombatSupport.visible(level,position(),target)) continue;
                int strength=i==0?effectiveStrength(owner,target):strengths[i];
                float damage=MagneticCombatRules.chain(spellLevel,strength)*power*(age==20?0.25F:1);
                if(i==0) damage*=reward(owner,target);
                hit(ModSpells.MAGNETIZED_FRACTURE_CHAIN.get(),owner,target,damage);
                // One synchronized local burst per node; no recursively spawned chains.
                if(age==20) MagneticCombatEffects.burst(level,MagneticCombatSupport.center(target),0.6F,6,0);
            }
            if(age==20) {
                for(int i=1;i<nodes.size();i++) {
                    LivingEntity target=living(nodes.get(i));
                    if(target!=null) MagneticCombatEffects.arc(level,position(),MagneticCombatSupport.center(target),(i&1)==0);
                }
                finish();
            }
        }
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag); ListTag list=new ListTag();
        for(UUID id:nodes) { CompoundTag item=new CompoundTag(); item.putUUID("Id",id);list.add(item); }
        tag.put("Nodes",list);tag.putIntArray("Strengths",strengths);
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);nodes.clear();ListTag list=tag.getList("Nodes",Tag.TAG_COMPOUND);
        for(int i=0;i<Math.min(5,list.size());i++) nodes.add(list.getCompound(i).getUUID("Id"));
        strengths=tag.getIntArray("Strengths");
        if(strengths.length!=nodes.size()) strengths=new int[nodes.size()];
    }
}

