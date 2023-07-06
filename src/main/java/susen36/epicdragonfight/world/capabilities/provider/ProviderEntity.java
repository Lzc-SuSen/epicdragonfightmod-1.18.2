package susen36.epicdragonfight.world.capabilities.provider;

import com.google.common.collect.Maps;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.registries.ForgeRegistries;
import susen36.epicdragonfight.api.forgeevent.EntityPatchRegistryEvent;
import susen36.epicdragonfight.world.capabilities.DragonFightCapabilities;
import susen36.epicdragonfight.world.capabilities.entitypatch.EntityPatch;
import susen36.epicdragonfight.world.capabilities.entitypatch.boss.enderdragon.EnderDragonPatch;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class ProviderEntity implements ICapabilityProvider, NonNullSupplier<EntityPatch<?>> {
	private static final Map<EntityType<?>, Function<Entity, Supplier<EntityPatch<?>>>> CAPABILITIES = Maps.newHashMap();
	private static final Map<EntityType<?>, Function<Entity, Supplier<EntityPatch<?>>>> CUSTOM_CAPABILITIES = Maps.newHashMap();
	
	public static void registerEntityPatches() {
		Map<EntityType<?>, Function<Entity, Supplier<EntityPatch<?>>>> registry = Maps.newHashMap();
			registry.put(EntityType.ENDER_DRAGON, (entityIn) -> {
			if (entityIn instanceof EnderDragon) {
				return EnderDragonPatch::new;
			}
			return () -> null;
		});
	    EntityPatchRegistryEvent entitypatchRegistryEvent = new EntityPatchRegistryEvent(registry);
		ModLoader.get().postEvent(entitypatchRegistryEvent);

		CAPABILITIES.putAll(registry);
	}
	
	public static void registerEntityPatchesClient() {
		CAPABILITIES.put(EntityType.PLAYER, (entityIn) -> {
				return () -> null;
		});
	}
	
	public static void clear() {
		CUSTOM_CAPABILITIES.clear();
	}
	
	public static Function<Entity, Supplier<EntityPatch<?>>> get(String registryName) {
		ResourceLocation rl = new ResourceLocation(registryName);
		EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(rl);
		return CAPABILITIES.get(entityType);
	}
	
	private EntityPatch<?> capability;
	private LazyOptional<EntityPatch<?>> optional = LazyOptional.of(this);
	
	public ProviderEntity(Entity entity) {
		Function<Entity, Supplier<EntityPatch<?>>> provider = CUSTOM_CAPABILITIES.getOrDefault(entity.getType(), CAPABILITIES.get(entity.getType()));
		
		if (provider != null) {
			this.capability = provider.apply(entity).get();
		}
	}
	
	public boolean hasCapability() {
		return capability != null;
	}
	
	@Override
	public EntityPatch<?> get() {
		return this.capability;
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return cap == DragonFightCapabilities.CAPABILITY_ENTITY ? this.optional.cast() :  LazyOptional.empty();
	}
}