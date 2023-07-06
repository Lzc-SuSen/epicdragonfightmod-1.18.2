package susen36.epicdragonfight.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import susen36.epicdragonfight.client.events.engine.RenderEngine;

@OnlyIn(Dist.CLIENT)
public class ClientEngine {
	public static ClientEngine instance;
	public Minecraft minecraft;
	public RenderEngine renderEngine;

	public ClientEngine() {
		instance = this;
		this.minecraft = Minecraft.getInstance();
		this.renderEngine = new RenderEngine();

	}

}