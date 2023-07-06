package susen36.epicdragonfight.gameasset;

import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import susen36.epicdragonfight.EpicDragonFight;
import susen36.epicdragonfight.api.model.Model;

import java.util.Map;

public abstract class Models<T extends Model> {
	public static final ServerModels LOGICAL_SERVER = new ServerModels();
	protected final Map<ResourceLocation, T> models = Maps.newHashMap();

	public T dragon;

	public static class ServerModels extends Models<Model> {
		public ServerModels() {
		this.dragon = register(new ResourceLocation(EpicDragonFight.MODID, "entity/dragon"));
		}
		
		@Override
		public Model register(ResourceLocation rl) {
			Model model = new Model(rl);
			this.models.put(rl, model);
			return model;
		}
	}
	
	public abstract T register(ResourceLocation rl);
	
	public T get(ResourceLocation location) {
		return this.models.get(location);
	}
	
	public void loadArmatures(ResourceManager resourceManager) {
		this.dragon.loadArmatureData(resourceManager);
	}
}