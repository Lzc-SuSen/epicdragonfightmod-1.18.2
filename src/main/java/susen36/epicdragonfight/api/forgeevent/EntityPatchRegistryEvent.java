package susen36.epicdragonfight.api.forgeevent;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import susen36.epicdragonfight.world.capabilities.entitypatch.EntityPatch;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class EntityPatchRegistryEvent extends Event implements IModBusEvent {
	private final Map<EntityType<?>, Function<Entity, Supplier<EntityPatch<?>>>> typeEntry;
	
	public EntityPatchRegistryEvent(Map<EntityType<?>, Function<Entity, Supplier<EntityPatch<?>>>> typeEntry) {
		this.typeEntry = typeEntry;
	}
}