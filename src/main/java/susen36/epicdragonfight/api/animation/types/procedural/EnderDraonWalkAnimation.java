package susen36.epicdragonfight.api.animation.types.procedural;

import net.minecraft.server.packs.resources.ResourceManager;
import susen36.epicdragonfight.api.animation.types.StaticAnimation;
import susen36.epicdragonfight.api.model.Model;

public class EnderDraonWalkAnimation extends StaticAnimation implements ProceduralAnimation {

	public EnderDraonWalkAnimation(float convertTime, String path, Model model, IKInfo[] ikInfos) {
		super(convertTime, true, path, model);
	}
	
	@Override
	public void loadAnimation(ResourceManager resourceManager) {
		loadBothSide(resourceManager, this);
		this.onLoaded();
	}

}