package susen36.epicdragonfight.network.client;

import net.minecraft.network.FriendlyByteBuf;
import susen36.epicdragonfight.api.animation.types.StaticAnimation;

public class CPPlayAnimation {
	private int namespaceId;
	private int animationId;
	private float modifyTime;
	private boolean isClientSideAnimation;
	private boolean resendToSender;
	
	public CPPlayAnimation() {
		this.animationId = 0;
		this.modifyTime = 0;
		this.resendToSender = false;
	}

	public CPPlayAnimation(StaticAnimation animation, float modifyTime, boolean clinetOnly, boolean resendToSender) {
		this(animation.getNamespaceId(), animation.getId(), modifyTime, clinetOnly, resendToSender);
	}

	public CPPlayAnimation(int namespaceId, int animationId, float modifyTime, boolean clinetOnly, boolean resendToSender) {
		this.namespaceId = namespaceId;
		this.animationId = animationId;
		this.modifyTime = modifyTime;
		this.isClientSideAnimation = clinetOnly;
		this.resendToSender = resendToSender;
	}
	
	public static CPPlayAnimation fromBytes(FriendlyByteBuf buf) {
		return new CPPlayAnimation(buf.readInt(), buf.readInt(), buf.readFloat(), buf.readBoolean(), buf.readBoolean());
	}

	public static void toBytes(CPPlayAnimation msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.namespaceId);
		buf.writeInt(msg.animationId);
		buf.writeFloat(msg.modifyTime);
		buf.writeBoolean(msg.isClientSideAnimation);
		buf.writeBoolean(msg.resendToSender);
	}

}