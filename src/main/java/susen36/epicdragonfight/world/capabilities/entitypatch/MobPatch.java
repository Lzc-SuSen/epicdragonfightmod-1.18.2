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
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import susen36.epicdragonfight.EpicDragonFight;
import susen36.epicdragonfight.api.animation.LivingMotions;
import susen36.epicdragonfight.api.client.animation.Layer;
import susen36.epicdragonfight.network.EpicFightNetworkManager;
import susen36.epicdragonfight.network.server.SPSetAttackTarget;
import susen36.epicdragonfight.world.entity.ai.goal.AnimatedAttackGoal;
import susen36.epicdragonfight.world.entity.ai.goal.TargetChasingGoal;

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
			
			if (goal instanceof MeleeAttackGoal || goal instanceof AnimatedAttackGoal || goal instanceof RangedAttackGoal || goal instanceof TargetChasingGoal) {
				toRemove.add(goal);
			}
		}
	}
	
	protected final void commonMobUpdateMotion(boolean considerInaction) {
		if (this.original.getHealth() <= 0.0F) {
			currentLivingMotion = LivingMotions.DEATH;
		} else if (this.state.inaction() && considerInaction) {
			currentLivingMotion = LivingMotions.IDLE;
		} else {
			if (original.getVehicle() != null)
				currentLivingMotion = LivingMotions.MOUNT;
			else
				if (this.original.getDeltaMovement().y < -0.55F)
					currentLivingMotion = LivingMotions.FALL;
				else if (original.animationSpeed > 0.01F)
					currentLivingMotion = LivingMotions.WALK;
				else
					currentLivingMotion = LivingMotions.IDLE;
		}
		
		this.currentCompositeMotion = this.currentLivingMotion;
	}
	
	protected final void commonAggressiveMobUpdateMotion(boolean considerInaction) {
		if (this.original.getHealth() <= 0.0F) {
			currentLivingMotion = LivingMotions.DEATH;
		} else if (this.state.inaction() && considerInaction) {
			currentLivingMotion = LivingMotions.IDLE;
		} else {
			if (original.getVehicle() != null) {
				currentLivingMotion = LivingMotions.MOUNT;
			} else {
				if (this.original.getDeltaMovement().y < -0.55F)
					currentLivingMotion = LivingMotions.FALL;
				else if (original.animationSpeed > 0.01F)
					if (original.isAggressive())
						currentLivingMotion = LivingMotions.CHASE;
					else
						currentLivingMotion = LivingMotions.WALK;
				else
					currentLivingMotion = LivingMotions.IDLE;
			}
		}
		
		this.currentCompositeMotion = this.currentLivingMotion;
	}
	
	protected final void commonAggressiveRangedMobUpdateMotion(boolean considerInaction) {
		this.commonAggressiveMobUpdateMotion(considerInaction);
		UseAnim useAction = this.original.getItemInHand(this.original.getUsedItemHand()).getUseAnimation();
		
		if (this.original.isUsingItem()) {
			if (useAction == UseAnim.CROSSBOW)
				currentCompositeMotion = LivingMotions.RELOAD;
			else
				currentCompositeMotion = LivingMotions.AIM;
		} else {
			if (this.getClientAnimator().getCompositeLayer(Layer.Priority.MIDDLE).animationPlayer.getAnimation().isReboundAnimation())
				currentCompositeMotion = LivingMotions.NONE;
		}
		
		if (CrossbowItem.isCharged(this.original.getMainHandItem()))
			currentCompositeMotion = LivingMotions.AIM;
		else if (this.getClientAnimator().isAiming() && currentCompositeMotion != LivingMotions.AIM)
			this.playReboundAnimation();
	}

	
	@Override
	public LivingEntity getTarget() {
		return this.original.getTarget();
	}
	
	public void setAttakTargetSync(LivingEntity entityIn) {
		if (!this.original.level.isClientSide()) {
			this.original.setTarget(entityIn);
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(new SPSetAttackTarget(this.original.getId(), entityIn != null ? entityIn.getId() : -1), this.original);
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
			double d3 = (double) Math.sqrt(d0 * d0 + d2 * d2);
			return Mth.clamp(Mth.wrapDegrees((float) ((Mth.atan2(d1, d3) * (double) (180F / (float) Math.PI)))), -30.0F, 30.0F);
		} else {
			return super.getAttackDirectionPitch();
		}
	}
}