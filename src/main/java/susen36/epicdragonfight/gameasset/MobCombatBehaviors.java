package susen36.epicdragonfight.gameasset;

import susen36.epicdragonfight.world.capabilities.entitypatch.boss.enderdragon.DragonFlyingPhase;
import susen36.epicdragonfight.world.capabilities.entitypatch.boss.enderdragon.EnderDragonPatch;
import susen36.epicdragonfight.world.capabilities.entitypatch.boss.enderdragon.PatchedPhases;
import susen36.epicdragonfight.world.entity.ai.goal.CombatBehaviors;
import susen36.epicdragonfight.world.entity.ai.goal.CombatBehaviors.Behavior;
import susen36.epicdragonfight.world.entity.ai.goal.CombatBehaviors.BehaviorSeries;
import susen36.epicdragonfight.world.entity.ai.goal.CombatBehaviors.Health.Comparator;

public class MobCombatBehaviors {

	public static final CombatBehaviors.Builder<EnderDragonPatch> ENDER_DRAGON = CombatBehaviors.<EnderDragonPatch>builder()
		.newBehaviorSeries(
			BehaviorSeries.<EnderDragonPatch>builder().weight(50.0F).canBeInterrupted(false).looping(false)
				.nextBehavior(Behavior.<EnderDragonPatch>builder().animationBehavior(Animations.DRAGON_ATTACK1).randomChance(0.1F).withinDistance(0.0D, 7.0D).withinAngle(0.0F, 60.0F))
				.nextBehavior(Behavior.<EnderDragonPatch>builder().animationBehavior(Animations.DRAGON_ATTACK3).withinDistance(0.0D, 7.0D))
				.nextBehavior(Behavior.<EnderDragonPatch>builder().animationBehavior(Animations.DRAGON_ATTACK2))
		).newBehaviorSeries(
			BehaviorSeries.<EnderDragonPatch>builder().weight(50.0F).canBeInterrupted(false).looping(false)
				.nextBehavior(Behavior.<EnderDragonPatch>builder().animationBehavior(Animations.DRAGON_ATTACK2).withinDistance(0.0D, 5.0D).withinAngle(0.0F, 60.0F))
				.nextBehavior(Behavior.<EnderDragonPatch>builder().animationBehavior(Animations.DRAGON_ATTACK3))
				.nextBehavior(Behavior.<EnderDragonPatch>builder().animationBehavior(Animations.DRAGON_ATTACK1).randomChance(0.4F).withinDistance(0.0D, 7.0D))
		).newBehaviorSeries(
			BehaviorSeries.<EnderDragonPatch>builder().weight(50.0F).cooldown(200).simultaneousCooldown(3).canBeInterrupted(false).looping(false)
				.nextBehavior(Behavior.<EnderDragonPatch>builder().animationBehavior(Animations.DRAGON_ATTACK4).withinDistance(10.0D, 15.0D).withinAngle(0.0F, 40.0F))
		).newBehaviorSeries(
			BehaviorSeries.<EnderDragonPatch>builder().weight(100.0F).cooldown(100).simultaneousCooldown(2).canBeInterrupted(false).looping(false)
				.nextBehavior(Behavior.<EnderDragonPatch>builder().animationBehavior(Animations.DRAGON_BACKJUMP_PREPARE).withinDistance(0.0D, 4.0D).withinAngle(90.0F, 180.0F))
		).newBehaviorSeries(
			BehaviorSeries.<EnderDragonPatch>builder().weight(100.0F).canBeInterrupted(false).looping(false)
				.nextBehavior(Behavior.<EnderDragonPatch>builder().animationBehavior(Animations.DRAGON_FIREBALL1).withinDistance(15.0D, 30.0D).withinAngleHorizontal(0.0F, 10.0F))
				.nextBehavior(Behavior.<EnderDragonPatch>builder().animationBehavior(Animations.DRAGON_FIREBALL2).withinDistance(15.0D, 30.0D).withinAngleHorizontal(0.0F, 10.0F))
				.nextBehavior(Behavior.<EnderDragonPatch>builder().animationBehavior(Animations.DRAGON_FIREBALL3).randomChance(0.6F).withinDistance(15.0D, 30.0D).withinAngleHorizontal(0.0F, 10.0F))
			).newBehaviorSeries(
			BehaviorSeries.<EnderDragonPatch>builder().weight(1000.0F).cooldown(0).canBeInterrupted(false).looping(false)
				.nextBehavior(Behavior.<EnderDragonPatch>builder().health(0.3F, Comparator.LESS_RATIO).custom((mobpatch) -> mobpatch.getOriginal().getDragonFight().getCrystalsAlive() > 0)
				.behavior((mobpatch) -> {
					mobpatch.getOriginal().getPhaseManager().setPhase(PatchedPhases.CRYSTAL_LINK);
				}))
		).newBehaviorSeries(
			BehaviorSeries.<EnderDragonPatch>builder().weight(10.0F).cooldown(1600).canBeInterrupted(false).looping(false)
				.nextBehavior(Behavior.<EnderDragonPatch>builder().health(0.5F, Comparator.LESS_RATIO).custom((mobpatch) -> mobpatch.getOriginal().getDragonFight().getCrystalsAlive() > 0)
				.behavior((mobpatch) -> {
					mobpatch.playAnimationSynchronized(Animations.DRAGON_GROUND_TO_FLY, 0.0F);
					mobpatch.getOriginal().getPhaseManager().setPhase(PatchedPhases.FLYING);
					((DragonFlyingPhase)mobpatch.getOriginal().getPhaseManager().getCurrentPhase()).enableAirstrike();
				}))
		);
}