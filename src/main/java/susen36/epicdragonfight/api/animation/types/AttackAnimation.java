package susen36.epicdragonfight.api.animation.types;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.PartEntity;
import susen36.epicdragonfight.api.animation.*;
import susen36.epicdragonfight.api.animation.property.AnimationProperty.*;
import susen36.epicdragonfight.api.collider.Collider;
import susen36.epicdragonfight.api.model.Model;
import susen36.epicdragonfight.api.utils.AttackResult;
import susen36.epicdragonfight.api.utils.HitEntityList;
import susen36.epicdragonfight.api.utils.math.OpenMatrix4f;
import susen36.epicdragonfight.api.utils.math.Vec3f;
import susen36.epicdragonfight.gameasset.Models;
import susen36.epicdragonfight.world.capabilities.entitypatch.LivingEntityPatch;
import susen36.epicdragonfight.world.capabilities.entitypatch.MobPatch;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AttackAnimation extends ActionAnimation {
	protected static final ActionAnimationCoordSetter COMMON_COORD_SETTER = (self, entitypatch, transformSheet) -> {
		LivingEntity attackTarget = entitypatch.getTarget();
		
		if (!self.getRealAnimation().getProperty(AttackAnimationProperty.FIXED_MOVE_DISTANCE).orElse(false) && attackTarget != null) {
			TransformSheet transform = self.getTransfroms().get("Root").copyAll();
			Keyframe[] keyframes = transform.getKeyframes();
			int startFrame = 0;
			int endFrame = transform.getKeyframes().length - 1;
			Vec3f keyLast = keyframes[endFrame].transform().translation();
			Vec3 pos = entitypatch.getOriginal().getEyePosition();
			Vec3 targetpos = attackTarget.position();
			float horizontalDistance = Math.max((float)targetpos.subtract(pos).horizontalDistance() - (attackTarget.getBbWidth() + entitypatch.getOriginal().getBbWidth()) * 0.75F, 0.0F);
			Vec3f worldPosition = new Vec3f(keyLast.x, 0.0F, -horizontalDistance);
			float scale = Math.min(worldPosition.length() / keyLast.length(), 2.0F);
			
			for (int i = startFrame; i <= endFrame; i++) {
				Vec3f translation = keyframes[i].transform().translation();
				translation.z *= scale;
			}
			
			transformSheet.readFrom(transform);
		} else {
			transformSheet.readFrom(self.getTransfroms().get("Root"));
		}
	};
	
	public final Phase[] phases;
	
	public AttackAnimation(float convertTime, float antic, float preDelay, float contact, float recovery, @Nullable Collider collider, String index, String path, Model model) {
		this(convertTime, path, model, new Phase(0.0F, antic, preDelay, contact, recovery, Float.MAX_VALUE, index, collider));
	}
	
	public AttackAnimation(float convertTime, float antic, float preDelay, float contact, float recovery, InteractionHand hand, @Nullable Collider collider, String index, String path, Model model) {
		this(convertTime, path, model, new Phase(0.0F, antic, preDelay, contact, recovery, Float.MAX_VALUE, hand, index, collider));
	}
	
	public AttackAnimation(float convertTime, String path, Model model, Phase... phases) {
		super(convertTime, path, model);
		
		this.addProperty(ActionAnimationProperty.COORD_SET_BEGIN, COMMON_COORD_SETTER);
		this.addProperty(ActionAnimationProperty.COORD_SET_TICK, COMMON_COORD_SETTER);
		this.addProperty(ActionAnimationProperty.STOP_MOVEMENT, true);
		this.phases = phases;
		
		this.stateSpectrumBlueprint.clear();
		
		for (Phase phase : phases) {
			
			float preDelay = phase.preDelay;
			
			if (preDelay == 0.0F) {
				preDelay += 0.01F;
			}
			
			this.stateSpectrumBlueprint
				.newTimePair(phase.start,preDelay)
				.addState(EntityState.PHASE_LEVEL, 1)
				.newTimePair(phase.start, phase.contact + 0.01F)
				.addState(EntityState.CAN_SKILL_EXECUTION, false)
				.newTimePair(phase.start, phase.recovery)
				.addState(EntityState.MOVEMENT_LOCKED, true)
				.addState(EntityState.CAN_BASIC_ATTACK, false)
				.newTimePair(phase.start, phase.end)
				.addState(EntityState.INACTION, true)
				.newTimePair(phase.antic, phase.recovery)
				.addState(EntityState.TURNING_LOCKED, true)
				.newTimePair(preDelay, phase.contact + 0.01F)
				.addState(EntityState.ATTACKING, true)
				.addState(EntityState.PHASE_LEVEL, 2)
				.newTimePair(phase.contact + 0.01F, phase.end)
				.addState(EntityState.PHASE_LEVEL, 3);
		}
	}
	
	@Override
	public void tick(LivingEntityPatch<?> entitypatch) {
		super.tick(entitypatch);
		
		if (!entitypatch.isLogicalClient()) {
			AnimationPlayer player = entitypatch.getAnimator().getPlayerFor(this);
			float elapsedTime = player.getElapsedTime();
			float prevElapsedTime = player.getPrevElapsedTime();
			EntityState state = this.getState(elapsedTime);
			EntityState prevState = this.getState(prevElapsedTime);
			Phase phase = this.getPhaseByTime(elapsedTime);
			
			if (state.getLevel() == 1 && !state.turningLocked()) {
				if (entitypatch instanceof MobPatch) {
					((Mob)entitypatch.getOriginal()).getNavigation().stop();
					entitypatch.getOriginal().attackAnim = 2;
					LivingEntity target = entitypatch.getTarget();
					
					if (target != null) {
						entitypatch.rotateTo(target, entitypatch.getYRotLimit(), false);
					}
				}
			} else if (prevState.attacking() || state.attacking() || (prevState.getLevel() < 2 && state.getLevel() > 2)) {
				if (!prevState.attacking()) {
				//	entitypatch.playSound(this.getSwingSound(entitypatch, phase), 0.0F, 0.0F);
					entitypatch.currentlyAttackedEntity.clear();
				}
				
				this.hurtCollidingEntities(entitypatch, prevElapsedTime, elapsedTime, prevState, state, phase);
			}
		}
	}
	
	@Override
	public void end(LivingEntityPatch<?> entitypatch, boolean isEnd) {
		super.end(entitypatch, isEnd);
		entitypatch.currentlyAttackedEntity.clear();

	}
	
	public void hurtCollidingEntities(LivingEntityPatch<?> entitypatch, float prevElapsedTime, float elapsedTime, EntityState prevState, EntityState state, Phase phase) {
		Collider collider = this.getCollider(entitypatch, elapsedTime);
		LivingEntity entity = entitypatch.getOriginal();
		entitypatch.getEntityModel(Models.LOGICAL_SERVER).getArmature().initializeTransform();
		float prevPoseTime = prevState.attacking() ? prevElapsedTime : phase.preDelay;
		float poseTime = state.attacking() ? elapsedTime : phase.contact;
		List<Entity> list = collider.updateAndSelectCollideEntity(entitypatch, this, prevPoseTime, poseTime, phase.getColliderJointName(), this.getPlaySpeed(entitypatch));
		
		if (list.size() > 0) {
			HitEntityList hitEntities = new HitEntityList(entitypatch, list, phase.getProperty(AttackPhaseProperty.HIT_PRIORITY).orElse(HitEntityList.Priority.DISTANCE));
			boolean flag1 = true;
			entitypatch.getOriginal().setLastHurtMob(list.get(0));
			
			while (entitypatch.currentlyAttackedEntity.size() < 1 && hitEntities.next()) {
				Entity e = hitEntities.getEntity();
				LivingEntity trueEntity = this.getTrueEntity(e);
				
				if (!entitypatch.currentlyAttackedEntity.contains(trueEntity) && !entitypatch.isTeammate(e)) {
					if (e instanceof LivingEntity || e instanceof PartEntity) {
						if (entity.hasLineOfSight(e)) {
							DamageSource source = DamageSource.mobAttack(entity);
							AttackResult attackResult = entitypatch.tryHarm(e, this.getDamageTo(entitypatch, trueEntity, phase, source));
							boolean count = attackResult.resultType.count();
							
							if (attackResult.resultType.dealtDamage()) {
								int temp = e.invulnerableTime;
								trueEntity.invulnerableTime = 0;
								boolean attackSuccess = e.hurt((DamageSource)source, attackResult.damage);
								trueEntity.invulnerableTime = temp;
								count = attackSuccess || trueEntity.isDamageSourceBlocked((DamageSource)source);
								entitypatch.onHurtSomeone(e, phase.hand, source, attackResult.damage, attackSuccess);
								
								if (attackSuccess) {
                                 //	e.level.playSound(null, e.getX(), e.getY(), e.getZ(), this.getHitSound(entitypatch, phase), e.getSoundSource(), 1.0F, 1.0F);
									this.spawnHitParticle(((ServerLevel)e.level), entitypatch, e, phase);
								}
							}
							
							if (count) {
								entitypatch.currentlyAttackedEntity.add(trueEntity);
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	protected void onLoaded() {
		if (!this.getProperty(AttackAnimationProperty.LOCK_ROTATION).orElse(false)) {
			this.stateSpectrumBlueprint.newTimePair(0.0F, Float.MAX_VALUE).addStateRemoveOld(EntityState.TURNING_LOCKED, false);
		}
		
		super.onLoaded();
	}
	
	public Collider getCollider(LivingEntityPatch<?> entitypatch, float elapsedTime) {
		Phase phase = this.getPhaseByTime(elapsedTime);
		
		return  phase.collider;
	}
	
	public LivingEntity getTrueEntity(Entity entity) {
		if (entity instanceof LivingEntity) {
			return (LivingEntity)entity;
		} else if (entity instanceof PartEntity) {
			Entity parentEntity = ((PartEntity<?>)entity).getParent();
			
			if (parentEntity instanceof LivingEntity) {
				return (LivingEntity)parentEntity;
			}
		}
		
		return null;
	}
	

	
	protected float getDamageTo(LivingEntityPatch<?> entitypatch, LivingEntity target, Phase phase, DamageSource source) {


		return phase.getProperty(AttackPhaseProperty.DAMAGE).map((valueCorrector) -> valueCorrector.getTotalValue(entitypatch.getDamageTo(target, phase.hand))).orElse(entitypatch.getDamageTo(target, phase.hand));
	}
	

	

	
	protected void spawnHitParticle(ServerLevel world, LivingEntityPatch<?> attacker, Entity hit, Phase phase) {

	}
	
	@Override
	public Pose getPoseByTime(LivingEntityPatch<?> entitypatch, float time, float partialTicks) {
		Pose pose = super.getPoseByTime(entitypatch, time, partialTicks);
		
		this.getProperty(AttackAnimationProperty.ROTATE_X).ifPresent((flag) -> {
			if (flag) {
				float pitch = entitypatch.getAttackDirectionPitch();
				JointTransform chest = pose.getOrDefaultTransform("Chest");
				chest.frontResult(JointTransform.getRotation(Vector3f.XP.rotationDegrees(-pitch)), OpenMatrix4f::mulAsOriginFront);

			}
		});
		
		return pose;
	}
	
	@Override
	public float getPlaySpeed(LivingEntityPatch<?> entitypatch) {
		if (this.getProperty(StaticAnimationProperty.PLAY_SPEED).isPresent()) {
			return super.getPlaySpeed(entitypatch);
		}

		return 1.0F;
	}
	
	public <V> AttackAnimation addProperty(AttackAnimationProperty<V> propertyType, V value) {
		this.properties.put(propertyType, value);
		return this;
	}
	
	public <V> AttackAnimation addProperty(AttackPhaseProperty<V> propertyType, V value) {
		return this.addProperty(propertyType, value, 0);
	}
	
	public <V> AttackAnimation addProperty(AttackPhaseProperty<V> propertyType, V value, int index) {
		this.phases[index].addProperty(propertyType, value);
		return this;
	}
	
	public String getPathIndexByTime(float elapsedTime) {
		return this.getPhaseByTime(elapsedTime).jointName;
	}
	
	public Phase getPhaseByTime(float elapsedTime) {
		Phase currentPhase = null;
		
		for (Phase phase : this.phases) {
			currentPhase = phase;
			
			if (phase.end > elapsedTime) {
				break;
			}
		}
		
		return currentPhase;
	}
	
	@Deprecated
	public void changeCollider(Collider newCollider, int index) {
		this.phases[index].collider = newCollider;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderDebugging(PoseStack poseStack, MultiBufferSource buffer, LivingEntityPatch<?> entitypatch, float playTime, float partialTicks) {
		AnimationPlayer animPlayer = entitypatch.getAnimator().getPlayerFor(this);
		float prevElapsedTime = animPlayer.getPrevElapsedTime();
		float elapsedTime = animPlayer.getElapsedTime();
		this.getCollider(entitypatch, elapsedTime).draw(poseStack, buffer, entitypatch, this, prevElapsedTime, elapsedTime, partialTicks, this.getPlaySpeed(entitypatch));
	}
	
	public static class Phase {
		protected final Map<AttackPhaseProperty<?>, Object> properties = Maps.newHashMap();
		protected final float start;
		protected final float antic;
		protected final float preDelay;
		protected final float contact;
		protected final float recovery;
		protected final float end;
		protected final String jointName;
		protected final InteractionHand hand;
		protected Collider collider;
		
		public Phase(float start, float antic, float contact, float recovery, float end, String jointName, Collider collider) {
			this(start, antic, contact, recovery, end, InteractionHand.MAIN_HAND, jointName, collider);
		}
		
		public Phase(float start, float antic, float contact, float recovery, float end, InteractionHand hand, String jointName, Collider collider) {
			this(start, antic, antic, contact, recovery, end, hand, jointName, collider);
		}
		
		public Phase(float start, float antic, float preDelay, float contact, float recovery, float end, String jointName, Collider collider) {
			this(start, antic, preDelay, contact, recovery, end, InteractionHand.MAIN_HAND, jointName, collider);
		}
		
		public Phase(float start, float antic, float preDelay, float contact, float recovery, float end, InteractionHand hand, String jointName, Collider collider) {
			this.start = start;
			this.antic = antic;
			this.preDelay = preDelay;
			this.contact = contact;
			this.recovery = recovery;
			this.end = end;
			this.collider = collider;
			this.jointName = jointName;
			this.hand = hand;
		}
		
		public <V> Phase addProperty(AttackPhaseProperty<V> propertyType, V value) {
			this.properties.put(propertyType, value);
			return this;
		}
		
		public void addProperties(Set<Map.Entry<AttackPhaseProperty<?>, Object>> set) {
			for(Map.Entry<AttackPhaseProperty<?>, Object> entry : set) {
				this.properties.put(entry.getKey(), entry.getValue());
			}
		}
		
		@SuppressWarnings("unchecked")
		protected <V> Optional<V> getProperty(AttackPhaseProperty<V> propertyType) {
			return (Optional<V>) Optional.ofNullable(this.properties.get(propertyType));
		}
		
		public String getColliderJointName() {
			return this.jointName;
		}
		
		public InteractionHand getHand() {
			return this.hand;
		}
	}
}