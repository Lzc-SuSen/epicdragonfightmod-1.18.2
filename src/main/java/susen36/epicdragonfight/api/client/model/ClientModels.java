package susen36.epicdragonfight.api.client.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.commons.compress.utils.Lists;
import susen36.epicdragonfight.EpicDragonFight;
import susen36.epicdragonfight.gameasset.Models;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ClientModels extends Models<ClientModel> implements PreparableReloadListener {
	public static final ClientModels LOGICAL_CLIENT = new ClientModels();

	
	public ClientModels() {
		this.dragon = register(new ResourceLocation(EpicDragonFight.MODID, "entity/dragon"));
	}
	
	@Override
	public ClientModel register(ResourceLocation rl) {
		ClientModel model = new ClientModel(rl);
		this.register(rl, model);
		return model;
	}
	
	public void register(ResourceLocation rl, ClientModel model) {
		this.models.put(rl, model);
	}
	
	public void loadModels(ResourceManager resourceManager) {
		List<ResourceLocation> emptyResourceLocations = Lists.newArrayList();
		
		this.models.entrySet().forEach((entry) -> {
			if (!entry.getValue().loadMeshAndProperties(resourceManager)) {
				emptyResourceLocations.add(entry.getKey());
			}
		});
		
		emptyResourceLocations.forEach(this.models::remove);
	}

	@Override
	public CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
		return CompletableFuture.runAsync(() -> {
			this.loadModels(resourceManager);
			this.loadArmatures(resourceManager);
		}, gameExecutor).thenCompose(stage::wait);
	}
}