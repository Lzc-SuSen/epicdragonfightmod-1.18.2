package susen36.epicdragonfight.network.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import susen36.epicdragonfight.api.animation.types.StaticAnimation;
import susen36.epicdragonfight.world.capabilities.DragonFightCapabilities;
import susen36.epicdragonfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.function.Supplier;

public class SPPlayAnimation {
	protected int namespaceId;
	protected int animationId;
	protected int entityId;
	protected float convertTimeModifier;

	
	public SPPlayAnimation(StaticAnimation animation, float convertTimeModifier, LivingEntityPatch<?> entitypatch) {
		this(animation.getNamespaceId(), animation.getId(), entitypatch.getOriginal().getId(), convertTimeModifier);
	}
	
	public SPPlayAnimation(int namespaceId, int animation, int entityId, float convertTimeModifier) {
		this.namespaceId = namespaceId;
		this.animationId = animation;
		this.entityId = entityId;
		this.convertTimeModifier = convertTimeModifier;
	}
	
	public void onArrive() {
		Minecraft mc = Minecraft.getInstance();
		Entity entity = mc.player.level.getEntity(this.entityId);
		
		if (entity == null) {
			return;
		}
		
		LivingEntityPatch<?> entitypatch = (LivingEntityPatch<?>)entity.getCapability(DragonFightCapabilities.CAPABILITY_ENTITY, null).orElse(null);
		
		if (entitypatch != null) {
			entitypatch.getAnimator().playAnimation(this.namespaceId, this.animationId, this.convertTimeModifier);
		}
	}
	
	public static SPPlayAnimation fromBytes(FriendlyByteBuf buf) {
		return new SPPlayAnimation(buf.readInt(), buf.readInt(), buf.readInt(), buf.readFloat());
	}
	
	public static void toBytes(SPPlayAnimation msg, ByteBuf buf) {
		buf.writeInt(msg.namespaceId);
		buf.writeInt(msg.animationId);
		buf.writeInt(msg.entityId);
		buf.writeFloat(msg.convertTimeModifier);
	}
	
	public static void handle(SPPlayAnimation msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			msg.onArrive();
		});
		
		ctx.get().setPacketHandled(true);
	}
}