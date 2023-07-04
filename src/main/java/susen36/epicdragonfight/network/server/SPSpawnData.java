package susen36.epicdragonfight.network.server;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import susen36.epicdragonfight.world.capabilities.DragonFightCapabilities;
import susen36.epicdragonfight.world.capabilities.entitypatch.EntityPatch;

import java.util.function.Supplier;

public class SPSpawnData {
	private int entityId;
	private FriendlyByteBuf buffer;
	
	public SPSpawnData(int entityId) {
		this.entityId = entityId;
		this.buffer = new FriendlyByteBuf(Unpooled.buffer());
	}
	
	public FriendlyByteBuf getBuffer() {
		return this.buffer;
	}
	
	public static SPSpawnData fromBytes(FriendlyByteBuf buf) {
		SPSpawnData msg = new SPSpawnData(buf.readInt());

		while (buf.isReadable()) {
			msg.buffer.writeByte(buf.readByte());
		}

		return msg;
	}
	
	public static void toBytes(SPSpawnData msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.entityId);

		while (msg.buffer.isReadable()) {
			buf.writeByte(msg.buffer.readByte());
		}
	}
	
	public static void handle(SPSpawnData msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Minecraft mc = Minecraft.getInstance();
			Entity entity = mc.player.level.getEntity(msg.entityId);
			
			if (entity != null) {
				EntityPatch<?> playerpatch = entity.getCapability(DragonFightCapabilities.CAPABILITY_ENTITY, null).orElse(null);
				playerpatch.processSpawnData(msg.getBuffer());
			}
		});
		
		ctx.get().setPacketHandled(true);
	}
}