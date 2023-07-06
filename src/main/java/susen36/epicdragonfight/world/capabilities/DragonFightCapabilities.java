package susen36.epicdragonfight.world.capabilities;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import susen36.epicdragonfight.world.capabilities.entitypatch.EntityPatch;

@SuppressWarnings("rawtypes")
public class DragonFightCapabilities {
	public static final Capability<EntityPatch> CAPABILITY_ENTITY = CapabilityManager.get(new CapabilityToken<>(){});

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.register(EntityPatch.class);
	}

}