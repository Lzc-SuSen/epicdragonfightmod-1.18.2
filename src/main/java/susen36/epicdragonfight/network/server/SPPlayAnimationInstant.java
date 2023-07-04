package susen36.epicdragonfight.network.server;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import susen36.epicdragonfight.world.capabilities.DragonFightCapabilities;
import susen36.epicdragonfight.world.capabilities.entitypatch.LivingEntityPatch;

public class SPPlayAnimationInstant extends SPPlayAnimation {
	public SPPlayAnimationInstant(int namespaceId, int animation, int entityId, float convertTimeModifier) {
		super(namespaceId, animation, entityId, convertTimeModifier);
	}

	
	public static SPPlayAnimationInstant fromBytes(FriendlyByteBuf buf) {
		return new SPPlayAnimationInstant(buf.readInt(), buf.readInt(), buf.readInt(), buf.readFloat());
	}
	
	@Override
	public void onArrive() {
		Minecraft mc = Minecraft.getInstance();
		Entity entity = mc.player.level.getEntity(this.entityId);
		
		if (entity == null) {
			return;
		}
		
		LivingEntityPatch<?> entitypatch = (LivingEntityPatch<?>)entity.getCapability(DragonFightCapabilities.CAPABILITY_ENTITY, null).orElse(null);
		entitypatch.getAnimator().playAnimationInstantly(this.namespaceId, this.animationId);
		entitypatch.getAnimator().poseTick();
		entitypatch.getAnimator().poseTick();
	}
}