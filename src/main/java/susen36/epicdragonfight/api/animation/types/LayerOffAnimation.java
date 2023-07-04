package susen36.epicdragonfight.api.animation.types;

import java.util.Optional;

import net.minecraft.client.Minecraft;
import susen36.epicdragonfight.api.animation.Pose;
import susen36.epicdragonfight.api.animation.property.AnimationProperty;
import susen36.epicdragonfight.api.client.animation.Layer.Priority;
import susen36.epicdragonfight.api.client.animation.JointMask.BindModifier;
import susen36.epicdragonfight.gameasset.Animations;
import susen36.epicdragonfight.world.capabilities.entitypatch.LivingEntityPatch;

public class LayerOffAnimation extends DynamicAnimation {
	private DynamicAnimation lastAnimation;
	private Pose lastPose;
	private Priority layerPriority;
	
	public LayerOffAnimation(Priority layerPriority) {
		this.layerPriority = layerPriority;
	}
	
	public void setLastPose(Pose pose) {
		this.lastPose = pose;
	}
	
	@Override
	public void end(LivingEntityPatch<?> entitypatch, boolean isEnd) {
		if (entitypatch.isLogicalClient()) {
			entitypatch.getClientAnimator().baseLayer.disableLayer(this.layerPriority);
		}
	}
	
	@Override
	public Pose getPoseByTime(LivingEntityPatch<?> entitypatch, float time, float partialTicks) {
		Pose lowerLayerPose = entitypatch.getClientAnimator().getComposedLayerPoseBelow(this.layerPriority, Minecraft.getInstance().getFrameTime());
		return Pose.interpolatePose(this.lastPose, lowerLayerPose, time / this.totalTime);
	}
	
	@Override
	public boolean isJointEnabled(LivingEntityPatch<?> entitypatch, String joint) {
		return this.lastPose.getJointTransformData().containsKey(joint);
	}
	
	@Override
	public <V> Optional<V> getProperty(AnimationProperty<V> propertyType) {
		return this.lastAnimation.getProperty(propertyType);
	}
	
	public void setLastAnimation(DynamicAnimation animation) {
		this.lastAnimation = animation;
	}
	
	@Override
	public BindModifier getBindModifier(LivingEntityPatch<?> entitypatch, String joint) {
		return this.lastAnimation.getBindModifier(entitypatch, joint);
	}
	
	@Override
	public DynamicAnimation getRealAnimation() {
		return Animations.DUMMY_ANIMATION;
	}
}