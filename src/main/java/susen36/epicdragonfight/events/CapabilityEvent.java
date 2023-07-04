package susen36.epicdragonfight.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import susen36.epicdragonfight.EpicDragonFight;
import susen36.epicdragonfight.world.capabilities.DragonFightCapabilities;
import susen36.epicdragonfight.world.capabilities.entitypatch.EntityPatch;
import susen36.epicdragonfight.world.capabilities.provider.ProviderEntity;

@Mod.EventBusSubscriber(modid= EpicDragonFight.MODID)
public class CapabilityEvent {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@SubscribeEvent
	public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject().getCapability(DragonFightCapabilities.CAPABILITY_ENTITY).orElse(null) == null) {
			ProviderEntity prov = new ProviderEntity(event.getObject());
			if (prov.hasCapability()) {
				EntityPatch entityCap = prov.getCapability(DragonFightCapabilities.CAPABILITY_ENTITY).orElse(null);
				entityCap.onConstructed(event.getObject());
				event.addCapability(new ResourceLocation(EpicDragonFight.MODID, "entity_cap"), prov);
			}
		}
	}
}