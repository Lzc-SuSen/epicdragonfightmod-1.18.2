package susen36.epicdragonfight.world.capabilities.entitypatch;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import susen36.epicdragonfight.EpicDragonFight;
import susen36.epicdragonfight.api.animation.Animator;
import susen36.epicdragonfight.api.animation.LivingMotion;
import susen36.epicdragonfight.api.animation.LivingMotions;
import susen36.epicdragonfight.api.animation.types.EntityState;
import susen36.epicdragonfight.api.animation.types.StaticAnimation;
import susen36.epicdragonfight.api.client.animation.ClientAnimator;
import susen36.epicdragonfight.api.model.Model;
import susen36.epicdragonfight.api.utils.math.MathUtils;
import susen36.epicdragonfight.api.utils.math.OpenMatrix4f;
import susen36.epicdragonfight.gameasset.Models;
import susen36.epicdragonfight.network.DragoFightNetworkManager;
import susen36.epicdragonfight.network.server.SPPlayAnimation;
import susen36.epicdragonfight.world.entity.ai.attribute.DragonFightAttributeSupplier;

import java.util.ArrayList;
import java.util.List;

public abstract class LivingEntityPatch<T extends LivingEntity> extends EntityPatch<T> {
	protected EntityState state = EntityState.DEFAULT;
	protected Animator animator;
	public LivingMotion currentLivingMotion = LivingMotions.IDLE;
	public LivingMotion currentCompositeMotion = LivingMotions.IDLE;
	public List<LivingEntity> currentlyAttackedEntity;
	
	@Override
	public void onConstructed(T entityIn) {
		super.onConstructed(entityIn);
		this.animator = EpicDragonFight.getAnimator(this);
		this.animator.init();
		this.currentlyAttackedEntity = new ArrayList<>();
	}
	
	@Override
	public void onJoinWorld(T entityIn, EntityJoinWorldEvent event) {
		super.onJoinWorld(entityIn, event);
		this.original.getAttributes().supplier = new DragonFightAttributeSupplier(this.original.getAttributes().supplier);
		this.initAttributes();
	}
	
	@OnlyIn(Dist.CLIENT)
	public abstract void initAnimator(ClientAnimator clientAnimator);
	public abstract void updateMotion(boolean considerInaction);
	public abstract <M extends Model> M getEntityModel(Models<M> modelDB);
	
	protected void initAttributes() {
	}
	
	@Override
	protected void clientTick(LivingUpdateEvent event) {
	}
	
	@Override
	protected void serverTick(LivingUpdateEvent event) {

	}
	
	@Override
	public void tick(LivingUpdateEvent event) {
		this.animator.tick();
		
		if (this.isLogicalClient()) {
			this.clientTick(event);
		} else {
			this.serverTick(event);
		}
		
		if (this.original.deathTime == 19) {
			this.aboutToDeath();
		}
	}
	
	public void onDeath() {
		this.currentLivingMotion = LivingMotions.DEATH;
	}
	
	public void updateEntityState() {
		this.state = this.animator.getEntityState();
	}
	
	public void cancelUsingItem() {
		this.original.stopUsingItem();
		ForgeEventFactory.onUseItemStop(this.original, this.original.getUseItem(), this.original.getUseItemRemainingTicks());
	}

	public void onHurtSomeone(Entity target, InteractionHand handIn) {

	}

	public void gatherDamageDealt(DamageSource source, float amount) {}


	public void rotateTo(float degree, float limit, boolean synchronizeOld) {
		LivingEntity entity = this.getOriginal();
		float amount = degree - entity.getYRot();
		
        while (amount < -180.0F) {
        	amount += 360.0F;
        }
        
        while (amount > 180.0F) {
        	amount -= 360.0F;
        }
        
        amount = Mth.clamp(amount, -limit, limit);
        float f1 = entity.getYRot() + amount;
        
		if (synchronizeOld) {
			entity.yRotO = f1;
			entity.yHeadRotO = f1;
			entity.yBodyRotO = f1;
		}
		
		entity.setYRot(f1);
		entity.yHeadRot = f1;
		entity.yBodyRot = f1;
	}
	
	public void rotateTo(Entity target, float limit, boolean partialSync) {
		double d0 = target.getX() - this.original.getX();
        double d1 = target.getZ() - this.original.getZ();
        float degree = -(float)Math.toDegrees(Mth.atan2(d0, d1));
    	this.rotateTo(degree, limit, partialSync);
	}
	
	public void playSound(SoundEvent sound, float pitchModifierMin, float pitchModifierMax) {
		this.playSound(sound, 1.0F, pitchModifierMin, pitchModifierMax);
	}
	
	public void playSound(SoundEvent sound, float volume, float pitchModifierMin, float pitchModifierMax) {
		float pitch = (this.original.getRandom().nextFloat() * 2.0F - 1.0F) * (pitchModifierMax - pitchModifierMin);
		
		if (!this.isLogicalClient()) {
			this.original.level.playSound(null, this.original.getX(), this.original.getY(), this.original.getZ(), sound, this.original.getSoundSource(), volume, 1.0F + pitch);
		} else {
			this.original.level.playLocalSound(this.original.getX(), this.original.getY(), this.original.getZ(), sound, this.original.getSoundSource(), volume, 1.0F + pitch, false);
		}
	}
	
	public LivingEntity getTarget() {
		return this.original.getLastHurtMob();
	}
	
	public float getAttackDirectionPitch() {
		float partialTicks = EpicDragonFight.isPhysicalClient() ? Minecraft.getInstance().getFrameTime() : 1.0F;
		float pitch = -this.getOriginal().getViewXRot(partialTicks);
		float correct = (pitch > 0) ? 0.03333F * (float)Math.pow(pitch, 2) : -0.03333F * (float)Math.pow(pitch, 2);
		return Mth.clamp(correct, -30.0F, 30.0F);
	}

	@Override
	public OpenMatrix4f getModelMatrix(float partialTicks) {
		float prevYRot;
		float yRot;
		float scale = this.original.isBaby() ? 0.5F : 1.0F;

		prevYRot = this.isLogicalClient() ? this.original.yBodyRotO : this.original.getYRot();
		yRot = this.isLogicalClient() ? this.original.yBodyRot : this.original.getYRot();

		
		return MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, prevYRot, yRot, partialTicks, scale, scale, scale);
	}


	public void playAnimationSynchronized(StaticAnimation animation, float convertTimeModifier) {
		this.playAnimationSynchronized(animation, convertTimeModifier, SPPlayAnimation::new);
	}
	
	public void playAnimationSynchronized(StaticAnimation animation, float convertTimeModifier, AnimationPacketProvider packetProvider) {
		this.animator.playAnimation(animation, convertTimeModifier);
		DragoFightNetworkManager.sendToAllPlayerTrackingThisEntity(packetProvider.get(animation, convertTimeModifier, this), this.original);
	}
	
	@FunctionalInterface
	public static interface AnimationPacketProvider {
		public SPPlayAnimation get(StaticAnimation animation, float convertTimeModifier, LivingEntityPatch<?> entitypatch);
	}
	

	@SuppressWarnings("unchecked")
	public <A extends Animator> A getAnimator() {
		return (A) this.animator;
	}
	
	public ClientAnimator getClientAnimator() {
		return this.<ClientAnimator>getAnimator();
	}


	public void aboutToDeath() {}
	
	public boolean shouldSkipRender() {
		return false;
	}

	
	public float getYRotLimit() {
		return 20.0F;
	}
	
	public EntityState getEntityState() {
		return this.state;
	}
	
	public LivingMotion getCurrentLivingMotion() {
		return this.currentLivingMotion;
	}
}