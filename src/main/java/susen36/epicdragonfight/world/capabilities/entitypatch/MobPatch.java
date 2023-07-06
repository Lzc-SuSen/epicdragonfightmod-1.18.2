package susen36.epicdragonfight.world.capabilities.entitypatch;

import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import susen36.epicdragonfight.EpicDragonFight;
import susen36.epicdragonfight.world.entity.ai.goal.AnimatedAttackGoal;

import java.util.Set;

public abstract class MobPatch<T extends Mob> extends LivingEntityPatch<T> {

	@Override
	public void onJoinWorld(T entityIn, EntityJoinWorldEvent event) {
		super.onJoinWorld(entityIn, event);
		
		if (!entityIn.level.isClientSide() && !this.original.isNoAi()) {
			this.initAI();
		}
	}
	
	protected void initAI() {
		Set<Goal> toRemove = Sets.newHashSet();
		this.selectGoalToRemove(toRemove);
		toRemove.forEach(this.original.goalSelector::removeGoal);
	}
	
	protected void selectGoalToRemove(Set<Goal> toRemove) {
		for (WrappedGoal wrappedGoal : this.original.goalSelector.getAvailableGoals()) {
			Goal goal = wrappedGoal.getGoal();
			
			if (goal instanceof MeleeAttackGoal || goal instanceof AnimatedAttackGoal || goal instanceof RangedAttackGoal) {
				toRemove.add(goal);
			}
		}
	}

	@Override
	public LivingEntity getTarget() {
		return this.original.getTarget();
	}
	
	public void setAttakTargetSync(LivingEntity entityIn) {
		if (!this.original.level.isClientSide()) {
			this.original.setTarget(entityIn);
			}
	}
	
	@Override
	public float getAttackDirectionPitch() {
		Entity attackTarget = this.getTarget();
		if (attackTarget != null) {
			float partialTicks = EpicDragonFight.isPhysicalClient() ? Minecraft.getInstance().getFrameTime() : 1.0F;
			Vec3 target = attackTarget.getEyePosition(partialTicks);
			Vec3 vector3d = this.original.getEyePosition(partialTicks);
			double d0 = target.x - vector3d.x;
			double d1 = target.y - vector3d.y;
			double d2 = target.z - vector3d.z;
			double d3 = Math.sqrt(d0 * d0 + d2 * d2);
			return Mth.clamp(Mth.wrapDegrees((float) ((Mth.atan2(d1, d3) * (double) (180F / (float) Math.PI)))), -30.0F, 30.0F);
		} else {
			return super.getAttackDirectionPitch();
		}
	}
}