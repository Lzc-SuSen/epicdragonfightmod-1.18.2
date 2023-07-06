package susen36.epicdragonfight.api.animation;

public enum LivingMotions implements LivingMotion {
	IDLE, WALK, FLY, CHASE,DEATH, NONE;
	
	final int id;
	
	LivingMotions() {
		this.id = LivingMotion.ENUM_MANAGER.assign(this);
	}
	
	public int universalOrdinal() {
		return id;
	}
}