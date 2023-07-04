package susen36.epicdragonfight.api.animation.types;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import susen36.epicdragonfight.api.client.animation.ClientAnimationProperties;
import susen36.epicdragonfight.api.client.animation.Layer;
import susen36.epicdragonfight.api.model.Model;
import susen36.epicdragonfight.world.capabilities.entitypatch.LivingEntityPatch;

public class MainFrameAnimation extends StaticAnimation {
	public MainFrameAnimation(float convertTime, String path, Model model) {
		super(convertTime, false, path, model);
	}
	
	@Override
	public void begin(LivingEntityPatch<?> entitypatch) {
		super.begin(entitypatch);
		
		entitypatch.updateEntityState();
		
		if (entitypatch.isLogicalClient()) {
			entitypatch.getClientAnimator().resetMotion();
			entitypatch.getClientAnimator().resetCompositeMotion();
			entitypatch.getClientAnimator().getPlayerFor(this).setReversed(false);
		}
	}
	
	@Override
	public void tick(LivingEntityPatch<?> entitypatch) {
		super.tick(entitypatch);
		entitypatch.getOriginal().animationSpeed = 0;
	}
	
	@Override
	public boolean isMainFrameAnimation() {
		return true;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public Layer.Priority getPriority() {
		return this.getProperty(ClientAnimationProperties.PRIORITY).orElse(Layer.Priority.HIGHEST);
	}
}