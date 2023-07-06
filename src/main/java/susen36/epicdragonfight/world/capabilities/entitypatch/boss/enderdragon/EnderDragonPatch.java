package susen36.epicdragonfight.world.capabilities.entitypatch.boss.enderdragon;

import com.google.common.collect.Maps;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import susen36.epicdragonfight.api.animation.LivingMotion;
import susen36.epicdragonfight.api.animation.LivingMotions;
import susen36.epicdragonfight.api.animation.TransformSheet;
import susen36.epicdragonfight.api.animation.types.StaticAnimation;
import susen36.epicdragonfight.api.animation.types.procedural.IKInfo;
import susen36.epicdragonfight.api.animation.types.procedural.TipPointAnimation;
import susen36.epicdragonfight.api.client.animation.ClientAnimator;
import susen36.epicdragonfight.api.model.Model;
import susen36.epicdragonfight.api.utils.math.MathUtils;
import susen36.epicdragonfight.api.utils.math.OpenMatrix4f;
import susen36.epicdragonfight.api.utils.math.Vec3f;
import susen36.epicdragonfight.gameasset.Animations;
import susen36.epicdragonfight.gameasset.Models;
import susen36.epicdragonfight.world.capabilities.entitypatch.MobPatch;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EnderDragonPatch extends MobPatch<EnderDragon> {
	public static final TargetingConditions DRAGON_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight();
	public static EnderDragonPatch INSTANCE_CLIENT;
	public static EnderDragonPatch INSTANCE_SERVER;
	private Map<String, TipPointAnimation> tipPointAnimations = Maps.newHashMap();
	private Map<LivingMotions, StaticAnimation> livingMotions = Maps.newHashMap();
	private boolean groundPhase;
	public float xRoot;
	public float xRootO;
	public float zRoot;
	public float zRootO;
	public LivingMotion prevMotion = LivingMotions.FLY;
	@Override
	public void onConstructed(EnderDragon entityIn) {
		this.livingMotions.put(LivingMotions.IDLE, Animations.DRAGON_IDLE);
		this.livingMotions.put(LivingMotions.WALK, Animations.DRAGON_WALK);
		this.livingMotions.put(LivingMotions.FLY, Animations.DRAGON_FLY);
		this.livingMotions.put(LivingMotions.CHASE, Animations.DRAGON_AIRSTRIKE);
		this.livingMotions.put(LivingMotions.DEATH, Animations.DRAGON_DEATH);
		super.onConstructed(entityIn);

		if(this.currentLivingMotion!=LivingMotions.FLY){
			this.currentLivingMotion=LivingMotions.WALK;
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onJoinWorld(EnderDragon entityIn, EntityJoinWorldEvent event) {
		super.onJoinWorld(entityIn, event);
		
		DragonPhaseInstance currentPhase = this.original.phaseManager.getCurrentPhase();
		EnderDragonPhase<?> startPhase = (currentPhase == null || !(currentPhase instanceof PatchedDragonPhase)) ? PatchedPhases.FLYING : this.original.phaseManager.getCurrentPhase().getPhase();
		this.original.phaseManager = new PhaseManagerPatch(this.original);
		this.original.phaseManager.setPhase(startPhase);
		entityIn.maxUpStep = 1.0F;
		
		if (entityIn.level.isClientSide()) {
			INSTANCE_CLIENT = this;
		} else {
			INSTANCE_SERVER = this;
		}
	}
	@Override
	protected void initAttributes() {
		super.initAttributes();
		this.original.getAttribute(Attributes.ARMOR).setBaseValue(250.0F);
		this.original.getAttribute(Attributes.ARMOR).setBaseValue(4.0F);
		this.original.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(2.0F);
		this.original.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(12.0F);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void initAnimator(ClientAnimator clientAnimator) {
		for (Map.Entry<LivingMotions, StaticAnimation> livingmotionEntry : this.livingMotions.entrySet()) {
			clientAnimator.addLivingAnimation(livingmotionEntry.getKey(), livingmotionEntry.getValue());
		}
		clientAnimator.setCurrentMotionsAsDefault();
	}

	@Override
	public void updateMotion(boolean considerInaction) {
		if (this.original.getHealth() <= 0.0F) {
			currentLivingMotion = LivingMotions.DEATH;
		} else if (this.state.inaction() && considerInaction) {
			this.currentLivingMotion = LivingMotions.IDLE;
		} else {
			DragonPhaseInstance phase = this.original.getPhaseManager().getCurrentPhase();
			
			if (!this.groundPhase) {
				if (phase.getPhase() == PatchedPhases.AIRSTRIKE && ((DragonAirstrikePhase)phase).isActuallyAttacking()) {
					this.currentLivingMotion = LivingMotions.CHASE;
				} else {
					this.currentLivingMotion = LivingMotions.FLY;
				}
			} else {
				if (phase.getPhase() == PatchedPhases.GROUND_BATTLE) {
						this.currentLivingMotion = LivingMotions.WALK;
				}
			}
		}
	}

	@Override
	public void tick(LivingUpdateEvent event) {
		super.tick(event);
		
		if (this.original.getPhaseManager().getCurrentPhase().isSitting()) {
			this.original.nearestCrystal = null;
		}
	}

	@Override
	public void serverTick(LivingUpdateEvent event) {
		super.serverTick(event);
		this.original.hurtTime = 2;
		this.original.getSensing().tick();
		this.updateMotion(true);
		
		if (this.prevMotion != this.currentLivingMotion && !this.animator.getEntityState().inaction()) {
			if (this.livingMotions.containsKey(this.currentLivingMotion)) {
				this.animator.playAnimation(this.livingMotions.get(this.currentLivingMotion), 0.0F);
			}
			
			this.prevMotion = this.currentLivingMotion;
		}
		this.updateTipPoints();
		Entity bodyPart = Objects.requireNonNull(this.original.getParts())[2];
		AABB bodyBoundingBox = bodyPart.getBoundingBox();
		List<Entity> list = this.original.level.getEntities(this.original, bodyBoundingBox, EntitySelector.pushableBy(this.original));
		if (!list.isEmpty()) {
			for (int l = 0; l < list.size(); ++l) {
				Entity entity = list.get(l);
				double d0 = entity.getX() - this.original.getX();
				double d1 = entity.getZ() - this.original.getZ();
				double d2 = Mth.absMax(d0, d1);
				
				if (d2 >= 0.01D) {
					d2 = Math.sqrt(d2);
					d0 = d0 / d2;
					d1 = d1 / d2;
					double d3 = 1.0D / d2;
					
					if (d3 > 1.0D) {
						d3 = 1.0D;
					}
					
					d0 = d0 * d3 * 0.2D;
					d1 = d1 * d3 * 0.2D;
					
					if (!entity.isVehicle()) {
						entity.push(d0, 0.0D, d1);
						entity.hurtMarked = true;
					}
				}
			}
		}
	}

	@Override
	public void clientTick(LivingUpdateEvent event) {
		this.xRootO = this.xRoot;
		this.zRootO = this.zRoot;
		super.clientTick(event);
		this.updateTipPoints();
	}

	@Override
	public void rotateTo(Entity target, float limit, boolean partialSync) {
		double d0 = target.getX() - this.original.getX();
        double d1 = target.getZ() - this.original.getZ();
        float degree = 180.0F - (float)Math.toDegrees(Mth.atan2(d0, d1));
    	this.rotateTo(degree, limit, partialSync);
	}
	
	public void updateTipPoints() {
		for (Map.Entry<String, TipPointAnimation> entry : this.tipPointAnimations.entrySet()) {
			if (entry.getValue().isOnWorking()) {
				entry.getValue().tick();
			}
		}
		
		if (this.tipPointAnimations.size() > 0) {
			TipPointAnimation frontL = this.getTipPointAnimation("Leg_Front_L3");
			TipPointAnimation frontR = this.getTipPointAnimation("Leg_Front_R3");
			TipPointAnimation backL = this.getTipPointAnimation("Leg_Back_L3");
			TipPointAnimation backR = this.getTipPointAnimation("Leg_Back_R3");
			float entityPosY = (float)this.original.position().y;
			float yFrontL = (frontL != null && frontL.isTouchingGround()) ? frontL.getTargetPosition().y : entityPosY;
			float yFrontR = (frontR != null && frontR.isTouchingGround()) ? frontR.getTargetPosition().y : entityPosY;
			float yBackL = (backL != null && backL.isTouchingGround()) ? backL.getTargetPosition().y : entityPosY;
			float yBackR = (backR != null && backR.isTouchingGround()) ? backR.getTargetPosition().y : entityPosY;
			float xdiff = (yFrontL + yBackL) * 0.5F - (yFrontR + yBackR) * 0.5F;
			float zdiff = (yFrontL + yFrontR) * 0.5F - (yBackL + yBackR) * 0.5F;
			float xdistance = 4.0F;
			float zdistance = 5.7F;
			this.xRoot += Mth.clamp(((float)Math.toDegrees(Math.atan2(zdiff, zdistance)) - this.xRoot), -1.0F, 1.0F);
			this.zRoot += Mth.clamp(((float)Math.toDegrees(Math.atan2(xdiff, xdistance)) - this.zRoot), -1.0F, 1.0F);
			float averageY = (yFrontL + yFrontR + yBackL + yBackR) * 0.25F;
			
			if (!this.isLogicalClient()) {
				float dy = averageY - entityPosY;
				this.original.move(MoverType.SELF, new Vec3(0.0F, dy, 0.0F));
			}
		}
	}

	public void resetTipAnimations() {
		this.tipPointAnimations.clear();
	}
	
	public void setFlyingPhase() {
		this.groundPhase = false;
		this.original.horizontalCollision = false;
		this.original.verticalCollision = false;
	}

	public void setGroundPhase() {
		this.groundPhase = true;
	}

	public boolean isGroundPhase() {
		return this.groundPhase;
	}

	@Override
	public <M extends Model> M getEntityModel(Models<M> modelDB) {
		return modelDB.dragon;
	}
	
	@Override
	public OpenMatrix4f getModelMatrix(float partialTicks) {
		return MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, this.original.yRotO, this.original.getYRot(), partialTicks, -1.0F, 1.0F, -1.0F);
	}
	
	@Override
	public double getAngleTo(Entity entityIn) {
		Vec3 a = this.original.getLookAngle().scale(-1.0D);
		Vec3 b = new Vec3(entityIn.getX() - this.original.getX(), entityIn.getY() - this.original.getY(), entityIn.getZ() - this.original.getZ()).normalize();
		double cosTheta = (a.x * b.x + a.y * b.y + a.z * b.z);
		
		return Math.toDegrees(Math.acos(cosTheta));
	}
	
	@Override
	public double getAngleToHorizontal(Entity entityIn) {
		Vec3 a = this.original.getLookAngle().scale(-1.0D);
		Vec3 b = new Vec3(entityIn.getX() - this.original.getX(), 0.0D, entityIn.getZ() - this.original.getZ()).normalize();
		double cos = (a.x * b.x + a.y * b.y + a.z * b.z);
		
		return Math.toDegrees(Math.acos(cos));
	}
	
	public TipPointAnimation getTipPointAnimation(String jointName) {
		return this.tipPointAnimations.get(jointName);
	}
	
	public void addTipPointAnimation(String jointName, Vec3f initpos, TransformSheet transformSheet, IKInfo ikSetter) {
		this.tipPointAnimations.put(jointName, new TipPointAnimation(transformSheet, initpos, ikSetter));
	}

}