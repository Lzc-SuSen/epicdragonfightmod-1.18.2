package susen36.epicdragonfight.api.animation.types;

import net.minecraft.world.damagesource.DamageSource;
import susen36.epicdragonfight.api.utils.TypeFlexibleHashMap;

import java.util.function.Function;

public class EntityState {
	public static class StateFactor<T> implements TypeFlexibleHashMap.TypeKey {
		String name;
		T defaultValue;
		
		public StateFactor(String name, T defaultValue) {
			this.name = name;
			this.defaultValue = defaultValue;
		}
		
		public T getDefaultVal() {
			return this.defaultValue;
		}
	}
	
	public static final StateFactor<Boolean> TURNING_LOCKED = new StateFactor<>("turningLocked", false);
	public static final StateFactor<Boolean> MOVEMENT_LOCKED = new StateFactor<>("movementLocked", false);
	public static final StateFactor<Boolean> ATTACKING = new StateFactor<>("attacking", false);
	public static final StateFactor<Boolean> CAN_BASIC_ATTACK = new StateFactor<>("canBasicAttack", true);
	public static final StateFactor<Boolean> CAN_SKILL_EXECUTION = new StateFactor<>("canExecuteSkill", true);
	public static final StateFactor<Boolean> INACTION = new StateFactor<>("inaction", false);
	public static final StateFactor<Boolean> HURT = new StateFactor<>("hurt", false);
	public static final StateFactor<Boolean> KNOCKDOWN = new StateFactor<>("knockdown", false);
	public static final StateFactor<Boolean> COUNTER_ATTACKABLE = new StateFactor<>("counterAttackable", false);
	public static final StateFactor<Integer> PHASE_LEVEL = new StateFactor<>("phaseLevel", 0);
	public static final StateFactor<Function<DamageSource, Boolean>> INVULNERABILITY_PREDICATE = new StateFactor<>("invulnerabilityPredicate", (damagesource) -> false);

	final boolean turningLocked;
	final boolean attacking;
	final boolean canBasicAttack;
	final boolean inaction;
	final boolean hurt;
	final boolean knockDown;
	final boolean counterAttackable;
	// free : 0, preDelay : 1, contact : 2, recovery : 3
	final int phaseLevel;
	final Function<DamageSource, Boolean> invulnerabilityChecker;
	
	public static final EntityState DEFAULT = new EntityState(false, false, true, false, false, false, false, 0, (damagesource) -> false);
	
	EntityState(boolean turningLocked, boolean attacking, boolean basicAttackPossible,
			boolean inaction, boolean hurt, boolean knockDown, boolean counterAttackable, int phaseLevel, Function<DamageSource, Boolean> invulnerabilityChecker) {
		this.turningLocked = turningLocked;
		this.attacking = attacking;
		this.canBasicAttack = basicAttackPossible;
		this.inaction = inaction;
		this.hurt = hurt;
		this.knockDown = knockDown;
		this.counterAttackable = counterAttackable;
		this.phaseLevel = phaseLevel;
		this.invulnerabilityChecker = invulnerabilityChecker;
	}

	public boolean turningLocked() {
		return this.turningLocked;
	}
	
	public boolean attacking() {
		return this.attacking;
	}

	public boolean canBasicAttack() {
		return this.canBasicAttack;
	}

	
	public boolean inaction() {
		return this.inaction;
	}

	
	public int getLevel() {
		return this.phaseLevel;
	}
}