package susen36.epicdragonfight.gameasset;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.commons.lang3.tuple.Pair;
import susen36.epicdragonfight.EpicDragonFight;
import susen36.epicdragonfight.api.animation.JointTransform;
import susen36.epicdragonfight.api.animation.TransformSheet;
import susen36.epicdragonfight.api.animation.property.AnimationProperty.ActionAnimationProperty;
import susen36.epicdragonfight.api.animation.property.AnimationProperty.AttackAnimationProperty;
import susen36.epicdragonfight.api.animation.property.AnimationProperty.StaticAnimationProperty;
import susen36.epicdragonfight.api.animation.types.ActionAnimation.ActionTime;
import susen36.epicdragonfight.api.animation.types.AttackAnimation;
import susen36.epicdragonfight.api.animation.types.StaticAnimation;
import susen36.epicdragonfight.api.animation.types.StaticAnimation.Event;
import susen36.epicdragonfight.api.animation.types.StaticAnimation.Event.Side;
import susen36.epicdragonfight.api.animation.types.procedural.*;
import susen36.epicdragonfight.api.client.model.ClientModels;
import susen36.epicdragonfight.api.forgeevent.AnimationRegistryEvent;
import susen36.epicdragonfight.api.model.Model;
import susen36.epicdragonfight.api.utils.math.MathUtils;
import susen36.epicdragonfight.api.utils.math.OpenMatrix4f;
import susen36.epicdragonfight.api.utils.math.Vec3f;
import susen36.epicdragonfight.world.capabilities.entitypatch.LivingEntityPatch;
import susen36.epicdragonfight.world.capabilities.entitypatch.boss.enderdragon.EnderDragonPatch;
import susen36.epicdragonfight.world.capabilities.entitypatch.boss.enderdragon.PatchedPhases;
import susen36.epicdragonfight.world.entity.DragonBreathball;

import java.util.function.Consumer;

public class Animations {
	public static StaticAnimation DUMMY_ANIMATION = new StaticAnimation();
	public static StaticAnimation DRAGON_IDLE;
	public static StaticAnimation DRAGON_WALK;
	public static StaticAnimation DRAGON_FLY;
	public static StaticAnimation DRAGON_DEATH;
	public static StaticAnimation DRAGON_GROUND_TO_FLY;
	public static StaticAnimation DRAGON_FLY_TO_GROUND;
	public static StaticAnimation DRAGON_ATTACK1;
	public static StaticAnimation DRAGON_ATTACK2;
	public static StaticAnimation DRAGON_ATTACK3;
	public static StaticAnimation DRAGON_ATTACK4;
	public static StaticAnimation DRAGON_FIREBALL1;
	public static StaticAnimation DRAGON_FIREBALL2;
	public static StaticAnimation DRAGON_AIRSTRIKE;
	public static StaticAnimation DRAGON_BACKJUMP_PREPARE;
	public static StaticAnimation DRAGON_BACKJUMP_MOVE;
	public static StaticAnimation DRAGON_BACKJUMP_RECOVERY;
	public static StaticAnimation DRAGON_CRYSTAL_LINK;
	public static StaticAnimation DRAGON_NEUTRALIZED;
	public static StaticAnimation DRAGON_NEUTRALIZED_RECOVERY;

	public static void registerAnimations(AnimationRegistryEvent event) {
		event.getRegistryMap().put(EpicDragonFight.MODID, Animations::build);
	}

	private static void build() {
		Models<?> models = FMLEnvironment.dist == Dist.CLIENT ? ClientModels.LOGICAL_CLIENT : Models.LOGICAL_SERVER;
		Model dragon = models.dragon;

		DRAGON_IDLE = new StaticAnimation(0.6F, true, "dragon/idle", dragon);
		DRAGON_WALK = new EnderDraonWalkAnimation(0.35F, "dragon/walk", dragon,
				new IKInfo[] {
						IKInfo.make("Leg_Front_L1", "Leg_Front_L3", "Leg_Front_R3", Pair.of(0, 3), 0.12F, 0, new boolean[] {true, true, true}),
						IKInfo.make("Leg_Front_R1", "Leg_Front_R3", "Leg_Front_L3", Pair.of(2, 5), 0.12F, 2, new boolean[] {true, true, true}),
						IKInfo.make("Leg_Back_L1", "Leg_Back_L3", "Leg_Back_R3", Pair.of(2, 5), 0.1344F, 4, new boolean[] {true, true, true}),
						IKInfo.make("Leg_Back_R1", "Leg_Back_R3", "Leg_Back_L3", Pair.of(0, 3), 0.1344F, 2, new boolean[] {true, true, true})
				});
		DRAGON_FLY = new StaticAnimation(0.35F, true, "dragon/fly", dragon)
				.addProperty(StaticAnimationProperty.EVENTS, new Event[]{Event.create(0.4F, ReuseableEvents.WING_FLAP, Side.CLIENT)});

		DRAGON_DEATH = new EnderDragonDeathAnimation(1.0F, "dragon/death", dragon);

		DRAGON_GROUND_TO_FLY = new EnderDragonActionAnimation(0.25F, "dragon/ground_to_fly", dragon, new IKInfo[]{
				IKInfo.make("Leg_Front_L1", "Leg_Front_L3", null, Pair.of(3, 7), 0.12F, 0, new boolean[]{true, false, false, false}),
				IKInfo.make("Leg_Front_R1", "Leg_Front_R3", null, Pair.of(3, 7), 0.12F, 0, new boolean[]{true, false, false, false}),
				IKInfo.make("Leg_Back_L1", "Leg_Back_L3", null, Pair.of(4, 7), 0.1344F, 0, new boolean[]{true, false, false, false}),
				IKInfo.make("Leg_Back_R1", "Leg_Back_R3", null, Pair.of(4, 7), 0.1344F, 0, new boolean[]{true, false, false, false})
		})
				.addProperty(ActionAnimationProperty.STOP_MOVEMENT, true)
				.addProperty(ActionAnimationProperty.MOVE_VERTICAL, true)
				.addProperty(StaticAnimationProperty.EVENTS, new Event[]{Event.create(0.25F, ReuseableEvents.WING_FLAP, Side.CLIENT), Event.create(1.05F, ReuseableEvents.WING_FLAP, Side.CLIENT), Event.create(1.45F, (entitypatch) -> {
					if (entitypatch instanceof EnderDragonPatch) {
						((EnderDragonPatch) entitypatch).setFlyingPhase();
					}
				}, Side.BOTH)});

		DRAGON_FLY_TO_GROUND = new EnderDragonDynamicActionAnimation(0.35F, "dragon/fly_to_ground", dragon, new IKInfo[]{
				IKInfo.make("Leg_Front_L1", "Leg_Front_L3", null, Pair.of(0, 4), 0.12F, 9, new boolean[]{false, false, false, true}),
				IKInfo.make("Leg_Front_R1", "Leg_Front_R3", null, Pair.of(0, 4), 0.12F, 9, new boolean[]{false, false, false, true}),
				IKInfo.make("Leg_Back_L1", "Leg_Back_L3", null, Pair.of(0, 4), 0.1344F, 7, new boolean[]{false, false, false, true}),
				IKInfo.make("Leg_Back_R1", "Leg_Back_R3", null, Pair.of(0, 4), 0.1344F, 7, new boolean[]{false, false, false, true})
		})
				.addProperty(ActionAnimationProperty.STOP_MOVEMENT, true)
				.addProperty(ActionAnimationProperty.MOVE_VERTICAL, true)
				.addProperty(ActionAnimationProperty.MOVE_ON_LINK, false)
				.addProperty(ActionAnimationProperty.MOVE_TIME, new ActionTime[]{ActionTime.crate(0.0F, 1.35F)})
				.addProperty(ActionAnimationProperty.COORD_SET_BEGIN, (self, entitypatch, transformSheet) -> {
					if (entitypatch instanceof EnderDragonPatch) {
						TransformSheet transform = self.getTransfroms().get("Root").copyAll();
						Vec3 dragonpos = entitypatch.getOriginal().position();
						Vec3 targetpos = ((EnderDragon) entitypatch.getOriginal()).getPhaseManager().getPhase(PatchedPhases.LANDING).getLandingPosition();
						float horizontalDistance = (float) dragonpos.subtract(0, dragonpos.y, 0).distanceTo(targetpos.subtract(0, targetpos.y, 0));
						float verticalDistance = (float) Math.abs(dragonpos.y - targetpos.y);
						JointTransform jt0 = transform.getKeyframes()[0].transform();
						JointTransform jt1 = transform.getKeyframes()[1].transform();
						JointTransform jt2 = transform.getKeyframes()[2].transform();
						OpenMatrix4f coordReverse = OpenMatrix4f.createRotatorDeg(90F, Vec3f.X_AXIS);
						Vec3f jointCoord = OpenMatrix4f.transform3v(coordReverse, new Vec3f(jt0.translation().x, verticalDistance, horizontalDistance), null);
						jt0.translation().set(jointCoord);
						jt1.translation().set(MathUtils.lerpVector(jt0.translation(), jt2.translation(), transform.getKeyframes()[1].time()));
						transformSheet.readFrom(transform);
					}
				})
				.addProperty(StaticAnimationProperty.EVENTS, new Event[]{Event.create(0.3F, ReuseableEvents.WING_FLAP, Side.CLIENT), Event.create(1.1F, (entitypatch) -> {
					entitypatch.playSound(SoundEvents.STONE_FALL, 0, 0);
				}, Side.CLIENT), Event.create(1.1F, (entitypatch) -> {
					LivingEntity original = entitypatch.getOriginal();
					DamageSource damageSource = DamageSource.mobAttack(original);

					for (Entity entity : original.level.getEntities(original, original.getBoundingBox().deflate(3.0D, 0.0D, 3.0D))) {
						entity.hurt(damageSource, 6.0F);
					}
				}, Side.SERVER)});

		DRAGON_ATTACK1 = new EnderDragonAttackAnimation(0.35F, 0.4F, 0.45F, 0.76F, 1.9F, ColliderPreset.DRAGON_LEG, "Leg_Front_R3", "dragon/attack1", dragon, new IKInfo[]{
				IKInfo.make("Leg_Front_L1", "Leg_Front_L3", null, Pair.of(2, 4), 0.12F, 0, new boolean[]{true, true}),
				IKInfo.make("Leg_Front_R1", "Leg_Front_R3", null, Pair.of(0, 5), 0.12F, 0, new boolean[]{false, false, false, false, true}),
				IKInfo.make("Leg_Back_L1", "Leg_Back_L3", null, null, 0.1344F, 0, new boolean[]{}),
				IKInfo.make("Leg_Back_R1", "Leg_Back_R3", null, Pair.of(1, 4), 0.1344F, 0, new boolean[]{true, false, true})
		}).addProperty(StaticAnimationProperty.EVENTS, new Event[]{Event.create(0.3F, (entitypatch) -> {
					LivingEntity original = entitypatch.getOriginal();
					Entity target = entitypatch.getTarget();
					if(original.distanceTo(target)<=8) {
						original.doHurtTarget(target);
					}}, Side.SERVER)});

		DRAGON_ATTACK2 = new EnderDragonAttackAnimation(0.35F, 0.25F, 0.45F, 0.66F, 0.75F, ColliderPreset.DRAGON_LEG, "Leg_Front_R3", "dragon/attack2", dragon, new IKInfo[]{
				IKInfo.make("Leg_Front_L1", "Leg_Front_L3", null, Pair.of(1, 4), 0.12F, 0, new boolean[]{true, true, true}),
				IKInfo.make("Leg_Back_L1", "Leg_Back_L3", null, null, 0.1344F, 0, new boolean[]{}),
				IKInfo.make("Leg_Back_R1", "Leg_Back_R3", null, null, 0.1344F, 0, new boolean[]{})
		}).addProperty(StaticAnimationProperty.EVENTS, new Event[]{Event.create(0.3F, (entitypatch) -> {
			LivingEntity original = entitypatch.getOriginal();
			Entity target = entitypatch.getTarget();
			if(original.distanceTo(target)<=8) {
				original.doHurtTarget(target);
			}}, Side.SERVER)});

		DRAGON_ATTACK3 = new EnderDragonAttackAnimation(0.35F, 0.25F, 0.45F, 0.66F, 0.75F, ColliderPreset.DRAGON_LEG, "Leg_Front_L3", "dragon/attack3", dragon, new IKInfo[]{
				IKInfo.make("Leg_Front_R1", "Leg_Front_R3", null, Pair.of(1, 4), 0.12F, 0, new boolean[]{true, true, true}),
				IKInfo.make("Leg_Back_L1", "Leg_Back_L3", null, null, 0.1344F, 0, new boolean[]{}),
				IKInfo.make("Leg_Back_R1", "Leg_Back_R3", null, null, 0.1344F, 0, new boolean[]{})
		}).addProperty(StaticAnimationProperty.EVENTS, new Event[]{Event.create(0.5F, (entitypatch) -> {
			LivingEntity original = entitypatch.getOriginal();
			Entity target = entitypatch.getTarget();
			if(original.distanceTo(target)<=8) {
				original.doHurtTarget(target);
			}}, Side.SERVER)});

		DRAGON_ATTACK4 = new EnderDragonAttackAnimation(0.35F, 0.3F, 0.5F, 0.76F, 0.9F, ColliderPreset.DRAGON_BODY, "Root", "dragon/attack4", dragon, new IKInfo[]{
				IKInfo.make("Leg_Front_L1", "Leg_Front_L3", null, Pair.of(0, 7), 0.12F, 0, new boolean[]{false, false, false, false, true, true, true}),
				IKInfo.make("Leg_Front_R1", "Leg_Front_R3", null, Pair.of(0, 7), 0.12F, 0, new boolean[]{false, false, false, false, true, true, true}),
				IKInfo.make("Leg_Back_L1", "Leg_Back_L3", null, Pair.of(3, 8), 0.1344F, 0, new boolean[]{false, false, false, false, true}),
				IKInfo.make("Leg_Back_R1", "Leg_Back_R3", null, Pair.of(3, 8), 0.1344F, 0, new boolean[]{false, false, false, false, true})
		}).addProperty(ActionAnimationProperty.MOVE_VERTICAL, true)
		   .addProperty(StaticAnimationProperty.EVENTS, new Event[]{Event.create(1.5F, (entitypatch) -> {
			LivingEntity original = entitypatch.getOriginal();

				for (Entity entity : original.level.getEntities(original, original.getBoundingBox().deflate(8.0D, 0.0D, 8.0D))) {
					original.doHurtTarget(entity);

			}}, Side.SERVER)});

		DRAGON_FIREBALL1 = new EnderDragonActionAnimation(0.16F, "dragon/fireball", dragon, new IKInfo[]{
				IKInfo.make("Leg_Front_L1", "Leg_Front_L3", null, Pair.of(0, 5), 0.12F, 0, new boolean[]{true, true, true, true, true}),
				IKInfo.make("Leg_Front_R1", "Leg_Front_R3", null, Pair.of(0, 5), 0.12F, 0, new boolean[]{true, true, true, true, true}),
				IKInfo.make("Leg_Back_L1", "Leg_Back_L3", null, Pair.of(0, 5), 0.1344F, 0, new boolean[]{true, true, true, true, true}),
				IKInfo.make("Leg_Back_R1", "Leg_Back_R3", null, Pair.of(0, 5), 0.1344F, 0, new boolean[]{true, true, true, true, true})
		}).addProperty(StaticAnimationProperty.EVENTS, new Event[]{Event.create(0.65F, (entitypatch) -> {
			LivingEntity original = entitypatch.getOriginal();
			Entity target = entitypatch.getTarget();
			Vec3 pos = original.getParts()[0].position();
			Vec3 toTarget = target.position().subtract(original.position()).normalize().scale(original.getBbWidth() * 0.5D);
			double d6 = (float) (pos.x + toTarget.x);
			double d7 = (float) (pos.y + 3.0F);
			double d8 = (float) (pos.z + toTarget.z);
			double d9 = target.getX() - d6;
			double d10 = target.getY(0.5D) - d7;
			double d11 = target.getZ() - d8;
			if (!original.isSilent()) {
				original.level.levelEvent((Player) null, 1017, original.blockPosition(), 0);
			}
			DragonBreathball dragonBreathball = new DragonBreathball(original.level, original, d9, d10, d11, 2.5f);
			dragonBreathball.moveTo(d6, d7, d8, 0.0F, 0.0F);
			original.level.addFreshEntity(dragonBreathball);
		}, Side.SERVER)});
		DRAGON_FIREBALL2 = new EnderDragonActionAnimation(0.16F, "dragon/fireball", dragon, new IKInfo[]{
				IKInfo.make("Leg_Front_L1", "Leg_Front_L3", null, Pair.of(0, 5), 0.12F, 0, new boolean[]{true, true, true, true, true}),
				IKInfo.make("Leg_Front_R1", "Leg_Front_R3", null, Pair.of(0, 5), 0.12F, 0, new boolean[]{true, true, true, true, true}),
				IKInfo.make("Leg_Back_L1", "Leg_Back_L3", null, Pair.of(0, 5), 0.1344F, 0, new boolean[]{true, true, true, true, true}),
				IKInfo.make("Leg_Back_R1", "Leg_Back_R3", null, Pair.of(0, 5), 0.1344F, 0, new boolean[]{true, true, true, true, true})
		}).addProperty(StaticAnimationProperty.EVENTS, new Event[]{Event.create(0.65F, (entitypatch) -> {
			LivingEntity original = entitypatch.getOriginal();
			Entity target = entitypatch.getTarget();
			Vec3 pos = original.getParts()[0].position();
			Vec3 toTarget = target.position().subtract(original.position()).normalize().scale(original.getBbWidth() * 0.5D);
			double d6 = (float) (pos.x + toTarget.x);
			double d7 = (float) (pos.y + 3.0F);
			double d8 = (float) (pos.z + toTarget.z);
			double d9 = target.getX() - d6;
			double d10 = target.getY(0.5D) - d7;
			double d11 = target.getZ() - d8;
			if (!original.isSilent()) {
				original.level.levelEvent((Player) null, 1017, original.blockPosition(), 0);
			}
			DragonFireball dragonFireball = new DragonFireball(original.level, original, d9, d10, d11);
			dragonFireball.moveTo(d6, d7, d8, 0.0F, 0.0F);
			original.level.addFreshEntity(dragonFireball);
		}, Side.SERVER)});
		DRAGON_AIRSTRIKE = new StaticAnimation(0.35F, true, "dragon/airstrike", dragon)
				.addProperty(StaticAnimationProperty.EVENTS, new Event[]{Event.create(0.3F, ReuseableEvents.WING_FLAP, Side.CLIENT)});

		DRAGON_BACKJUMP_PREPARE = new EnderDragonActionAnimation(0.35F, "dragon/backjump_prepare", dragon, new IKInfo[]{
				IKInfo.make("Leg_Front_L1", "Leg_Front_L3", null, Pair.of(0, 4), 0.12F, 0, new boolean[]{true, true, true, true}),
				IKInfo.make("Leg_Front_R1", "Leg_Front_R3", null, Pair.of(0, 4), 0.12F, 0, new boolean[]{true, true, true, true}),
				IKInfo.make("Leg_Back_L1", "Leg_Back_L3", null, Pair.of(0, 4), 0.1344F, 0, new boolean[]{true, true, true, true}),
				IKInfo.make("Leg_Back_R1", "Leg_Back_R3", null, Pair.of(0, 4), 0.1344F, 0, new boolean[]{true, true, true, true})
		}).addProperty(StaticAnimationProperty.EVENTS, new Event[]{Event.create(0.3F, (entitypatch) -> {
			entitypatch.getAnimator().reserveAnimation(DRAGON_BACKJUMP_MOVE);
		}, Side.BOTH)});
		DRAGON_BACKJUMP_MOVE = new AttackAnimation(0.0F, 10.0F, 10.0F, 10.0F, 10.0F, ColliderPreset.DRAGON_FIST, "Root", "dragon/backjump_move", dragon)
				.addProperty(AttackAnimationProperty.FIXED_MOVE_DISTANCE, true)
				.addProperty(StaticAnimationProperty.EVENTS, new Event[]{Event.create(1.0F, (entitypatch) -> {
					entitypatch.getAnimator().reserveAnimation(DRAGON_BACKJUMP_RECOVERY);
				}, Side.BOTH)});

		DRAGON_BACKJUMP_RECOVERY = new EnderDragonActionAnimation(0.0F, "dragon/backjump_recovery", dragon, new IKInfo[]{
				IKInfo.make("Leg_Front_L1", "Leg_Front_L3", null, Pair.of(0, 4), 0.12F, 0, new boolean[]{false, true, true, true}),
				IKInfo.make("Leg_Front_R1", "Leg_Front_R3", null, Pair.of(0, 4), 0.12F, 0, new boolean[]{false, true, true, true}),
				IKInfo.make("Leg_Back_L1", "Leg_Back_L3", null, Pair.of(0, 4), 0.1344F, 0, new boolean[]{true, true, true, true}),
				IKInfo.make("Leg_Back_R1", "Leg_Back_R3", null, Pair.of(0, 4), 0.1344F, 0, new boolean[]{true, true, true, true})
		})
				.addProperty(ActionAnimationProperty.MOVE_VERTICAL, true)
				.addProperty(StaticAnimationProperty.EVENTS, new Event[]{Event.create(0.15F, (entitypatch) -> {
					entitypatch.playSound(SoundEvents.STONE_FALL, 0, 0);

				}, Side.CLIENT)});

		DRAGON_CRYSTAL_LINK = new EnderDragonActionAnimation(0.5F, "dragon/crystal_link", dragon, new IKInfo[]{
				IKInfo.make("Leg_Front_L1", "Leg_Front_L3", null, Pair.of(0, 2), 0.12F, 0, new boolean[]{true, true}),
				IKInfo.make("Leg_Front_R1", "Leg_Front_R3", null, Pair.of(0, 2), 0.12F, 0, new boolean[]{true, true}),
				IKInfo.make("Leg_Back_L1", "Leg_Back_L3", null, Pair.of(0, 2), 0.1344F, 0, new boolean[]{true, true}),
				IKInfo.make("Leg_Back_R1", "Leg_Back_R3", null, Pair.of(0, 2), 0.1344F, 0, new boolean[]{true, true})
		})
				.addProperty(StaticAnimationProperty.EVENTS, new Event[]{Event.create(7.0F, (entitypatch) -> {
					entitypatch.getOriginal().playSound(SoundEvents.ENDER_DRAGON_GROWL, 7.0F, 0.8F + entitypatch.getOriginal().getRandom().nextFloat() * 0.3F);
					entitypatch.getOriginal().setHealth(entitypatch.getOriginal().getMaxHealth());

					if (entitypatch instanceof EnderDragonPatch) {
						((EnderDragonPatch) entitypatch).getOriginal().getPhaseManager().setPhase(PatchedPhases.GROUND_BATTLE);
					}
				}, Side.SERVER), Event.create(7.0F, (entitypatch) -> {
					Entity original = entitypatch.getOriginal();
					original.level.addParticle(ParticleTypes.EXPLOSION, original.getX(), original.getY() + 2.0D, original.getZ(), 0, 0, 0);
				}, Side.CLIENT)});

		DRAGON_NEUTRALIZED = new EnderDragonActionAnimation(0.1F, "dragon/neutralized", dragon, new IKInfo[]{
				IKInfo.make("Leg_Front_L1", "Leg_Front_L3", null, Pair.of(0, 4), 0.12F, 0, new boolean[]{true, true, true, true}),
				IKInfo.make("Leg_Front_R1", "Leg_Front_R3", null, Pair.of(0, 4), 0.12F, 0, new boolean[]{true, true, true, true}),
				IKInfo.make("Leg_Back_L1", "Leg_Back_L3", null, Pair.of(0, 4), 0.1344F, 0, new boolean[]{true, true, true, true}),
				IKInfo.make("Leg_Back_R1", "Leg_Back_R3", null, Pair.of(0, 4), 0.1344F, 0, new boolean[]{true, true, true, true})
		})
				.addProperty(StaticAnimationProperty.EVENTS, new Event[]{Event.create(3.95F, (entitypatch) -> {
					entitypatch.getAnimator().playAnimation(DRAGON_NEUTRALIZED_RECOVERY, 0);
				}, Side.BOTH)});

		DRAGON_NEUTRALIZED_RECOVERY = new EnderDragonActionAnimation(0.05F, "dragon/neutralized_recovery", dragon, new IKInfo[]{
				IKInfo.make("Leg_Front_L1", "Leg_Front_L3", null, Pair.of(0, 5), 0.12F, 0, new boolean[]{true, true, true, false, true}),
				IKInfo.make("Leg_Front_R1", "Leg_Front_R3", null, Pair.of(0, 5), 0.12F, 0, new boolean[]{true, false, true, true, true}),
				IKInfo.make("Leg_Back_L1", "Leg_Back_L3", null, Pair.of(0, 5), 0.1344F, 0, new boolean[]{true, true, true, true, true}),
				IKInfo.make("Leg_Back_R1", "Leg_Back_R3", null, Pair.of(0, 4), 0.1344F, 0, new boolean[]{true, true, true, true})
		})
				.addProperty(StaticAnimationProperty.EVENTS, new Event[]{Event.create(1.6F, (entitypatch) -> {
					if (entitypatch instanceof EnderDragonPatch) {
						((EnderDragonPatch) entitypatch).getOriginal().getPhaseManager().getPhase(PatchedPhases.GROUND_BATTLE).fly();
					}
				}, Side.SERVER)});
	}

	private static class ReuseableEvents {
		private static final Consumer<LivingEntityPatch<?>> WING_FLAP = (entitypatch) -> {
			if (entitypatch instanceof EnderDragonPatch) {
				((EnderDragonPatch) entitypatch).getOriginal().onFlap();
			}
		};
	}
}