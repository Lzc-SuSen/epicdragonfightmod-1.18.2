package susen36.epicdragonfight.network.server;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import susen36.epicdragonfight.EpicDragonFight;
import susen36.epicdragonfight.api.animation.LivingMotion;
import susen36.epicdragonfight.api.animation.types.StaticAnimation;
import susen36.epicdragonfight.api.client.animation.ClientAnimator;
import susen36.epicdragonfight.world.capabilities.DragonFightCapabilities;
import susen36.epicdragonfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;
import java.util.function.Supplier;

public class SPChangeLivingMotion {
	private int entityId;
	private int count;
	private boolean setChangesAsDefault;
	private List<LivingMotion> motionList = Lists.newArrayList();
	private List<StaticAnimation> animationList = Lists.newArrayList();

	private SPChangeLivingMotion(int entityId, int count, boolean setChangesAsDefault) {
		this.entityId = entityId;
		this.count = count;
		this.setChangesAsDefault = setChangesAsDefault;
	}
	

	
	public static SPChangeLivingMotion fromBytes(FriendlyByteBuf buf) {
		SPChangeLivingMotion msg = new SPChangeLivingMotion(buf.readInt(), buf.readInt(), buf.readBoolean());
		List<LivingMotion> motionList = Lists.newArrayList();
		List<StaticAnimation> animationList = Lists.newArrayList();
		
		for (int i = 0; i < msg.count; i++) {
			motionList.add(LivingMotion.ENUM_MANAGER.get(buf.readInt()));
		}
		
		for (int i = 0; i < msg.count; i++) {
			animationList.add(EpicDragonFight.getInstance().animationManager.findAnimationById(buf.readInt(), buf.readInt()));
		}
		
		msg.motionList = motionList;
		msg.animationList = animationList;
		
		return msg;
	}
	
	public static void toBytes(SPChangeLivingMotion msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.entityId);
		buf.writeInt(msg.count);
		buf.writeBoolean(msg.setChangesAsDefault);
		
		for (LivingMotion motion : msg.motionList) {
			buf.writeInt(motion.universalOrdinal());
		}
		
		for (StaticAnimation anim : msg.animationList) {
			buf.writeInt(anim.getNamespaceId());
			buf.writeInt(anim.getId());
		}
	}
	
	public static void handle(SPChangeLivingMotion msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Minecraft mc = Minecraft.getInstance();
			Entity entity = mc.player.level.getEntity(msg.entityId);
			
			if (entity != null) {
				LivingEntityPatch<?> entitypatch = (LivingEntityPatch<?>) entity.getCapability(DragonFightCapabilities.CAPABILITY_ENTITY, null).orElse(null);
				ClientAnimator animator = entitypatch.getClientAnimator();
				animator.resetMotions();
				animator.resetCompositeMotion();
				
				for (int i = 0; i < msg.count; i++) {
					entitypatch.getClientAnimator().addLivingAnimation(msg.motionList.get(i), msg.animationList.get(i));
				}
				
				if (msg.setChangesAsDefault) {
					animator.setCurrentMotionsAsDefault();
				}
			}
		});
		
		ctx.get().setPacketHandled(true);
	}
}