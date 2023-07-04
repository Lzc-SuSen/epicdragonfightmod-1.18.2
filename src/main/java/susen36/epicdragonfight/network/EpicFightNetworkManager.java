package susen36.epicdragonfight.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.PacketTarget;
import net.minecraftforge.network.simple.SimpleChannel;
import susen36.epicdragonfight.EpicDragonFight;
import susen36.epicdragonfight.network.server.*;

public class EpicFightNetworkManager {
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(EpicDragonFight.MODID, "network_manager"),
			() -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

	public static <MSG> void sendToServer(MSG message) {
		INSTANCE.sendToServer(message);
	}
	
	public static <MSG> void sendToClient(MSG message, PacketTarget packetTarget) {
		INSTANCE.send(packetTarget, message);
	}

	public static <MSG> void sendToAllPlayerTrackingThisEntity(MSG message, Entity entity) {
		sendToClient(message, PacketDistributor.TRACKING_ENTITY.with(() -> entity));
	}
	
	public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
		sendToClient(message, PacketDistributor.PLAYER.with(() -> player));
	}

	
	public static void registerPackets() {
		int id = 0;
		INSTANCE.registerMessage(id++, SPSpawnData.class, SPSpawnData::toBytes, SPSpawnData::fromBytes, SPSpawnData::handle);
		INSTANCE.registerMessage(id++, SPChangeLivingMotion.class, SPChangeLivingMotion::toBytes, SPChangeLivingMotion::fromBytes, SPChangeLivingMotion::handle);
		INSTANCE.registerMessage(id++, SPPlayAnimation.class, SPPlayAnimation::toBytes, SPPlayAnimation::fromBytes, SPPlayAnimation::handle);
		INSTANCE.registerMessage(id++, SPPlayAnimationInstant.class, SPPlayAnimation::toBytes, SPPlayAnimationInstant::fromBytes, SPPlayAnimation::handle);
		INSTANCE.registerMessage(id++, SPPlayAnimationAndSetTarget.class, SPPlayAnimationAndSetTarget::toBytes, SPPlayAnimationAndSetTarget::fromBytes, SPPlayAnimationAndSetTarget::handle);
		INSTANCE.registerMessage(id++, SPPlayAnimationAndSyncTransform.class, SPPlayAnimationAndSyncTransform::toBytes, SPPlayAnimationAndSyncTransform::fromBytes, SPPlayAnimationAndSyncTransform::handle);
		INSTANCE.registerMessage(id++, SPSetAttackTarget.class, SPSetAttackTarget::toBytes, SPSetAttackTarget::fromBytes, SPSetAttackTarget::handle);
	}
}