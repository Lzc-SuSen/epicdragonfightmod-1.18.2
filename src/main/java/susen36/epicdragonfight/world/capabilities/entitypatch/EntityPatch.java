package susen36.epicdragonfight.world.capabilities.entitypatch;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import susen36.epicdragonfight.api.utils.math.OpenMatrix4f;

public abstract class EntityPatch<T extends Entity> {
	protected T original;
	protected boolean initialized = false;
	
	public abstract void tick(LivingUpdateEvent event);
	protected abstract void clientTick(LivingUpdateEvent event);
	protected abstract void serverTick(LivingUpdateEvent event);

	
	public void onConstructed(T entityIn) {
		this.original = entityIn;
	}
	
	public void onJoinWorld(T entityIn, EntityJoinWorldEvent event) {
		this.initialized = true;
	}

	public final T getOriginal() {
		return this.original;
	}
	
	public boolean isInitialized() {
		return this.initialized;
	}
	
	public boolean isLogicalClient() {
		return this.original.level.isClientSide();
	}

	
	public double getAngleTo(Entity entityIn) {
		Vec3 a = this.original.getLookAngle();
		Vec3 b = new Vec3(entityIn.getX() - this.original.getX(), entityIn.getY() - this.original.getY(), entityIn.getZ() - this.original.getZ()).normalize();
		double cos = (a.x * b.x + a.y * b.y + a.z * b.z);
		
		return Math.toDegrees(Math.acos(cos));
	}
	
	public double getAngleToHorizontal(Entity entityIn) {
		Vec3 a = this.original.getLookAngle();
		Vec3 b = new Vec3(entityIn.getX() - this.original.getX(), 0.0D, entityIn.getZ() - this.original.getZ()).normalize();
		double cos = (a.x * b.x + a.y * b.y + a.z * b.z);
		
		return Math.toDegrees(Math.acos(cos));
	}
	
	public abstract OpenMatrix4f getModelMatrix(float partialTicks);
}