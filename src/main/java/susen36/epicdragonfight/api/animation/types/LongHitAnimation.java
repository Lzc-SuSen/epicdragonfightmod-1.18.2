package susen36.epicdragonfight.api.animation.types;

import susen36.epicdragonfight.api.animation.property.AnimationProperty.ActionAnimationProperty;
import susen36.epicdragonfight.api.model.Model;

public class LongHitAnimation extends ActionAnimation {
	public LongHitAnimation(float convertTime, String path, Model model) {
		super(convertTime, path, model);
		this.addProperty(ActionAnimationProperty.STOP_MOVEMENT, true);
		
		this.stateSpectrumBlueprint.clear()
			.newTimePair(0.0F, Float.MAX_VALUE)
			.addState(EntityState.TURNING_LOCKED, true)
			.addState(EntityState.MOVEMENT_LOCKED, true)
			.addState(EntityState.CAN_BASIC_ATTACK, false)
			.addState(EntityState.CAN_SKILL_EXECUTION, false)
			.addState(EntityState.INACTION, true)
			.addState(EntityState.HURT,	true);
	}
}