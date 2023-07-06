package susen36.epicdragonfight.client.events;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import susen36.epicdragonfight.EpicDragonFight;
import susen36.epicdragonfight.client.ClientEngine;
import susen36.epicdragonfight.world.capabilities.provider.ProviderEntity;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = EpicDragonFight.MODID, value = Dist.CLIENT)
public class ClientEvents {

	@SubscribeEvent
	public static void clientLogoutEvent(ClientPlayerNetworkEvent.LoggedOutEvent event) {
		if (event.getPlayer() != null) {
			ProviderEntity.clear();
			ClientEngine.instance.renderEngine.clearCustomEntityRenerer();
		}
	}
}