package susen36.epicdragonfight.network.server;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SPSetAttackTarget {
	private int entityId;
	private int targetEntityId;

	
	public SPSetAttackTarget(int entityId, int targetEntityId) {
		this.entityId = entityId;
		this.targetEntityId = targetEntityId;
	}
	
	public static SPSetAttackTarget fromBytes(FriendlyByteBuf buf) {
		return new SPSetAttackTarget(buf.readInt(), buf.readInt());
	}
	
	public static void toBytes(SPSetAttackTarget msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.entityId);
		buf.writeInt(msg.targetEntityId);
	}
	
	public static void handle(SPSetAttackTarget msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(()->{
			Minecraft minecraft = Minecraft.getInstance();
			Entity entity = minecraft.level.getEntity(msg.entityId);
			Entity targetEntity = minecraft.level.getEntity(msg.targetEntityId);
			
			if (entity != null && entity instanceof Mob) {
				if (targetEntity == null || !(targetEntity instanceof LivingEntity)) {
					((Mob)entity).setTarget((LivingEntity)null);
				} else {
					((Mob)entity).setTarget((LivingEntity)targetEntity);
				}
			}
		});
		
		ctx.get().setPacketHandled(true);
	}
}