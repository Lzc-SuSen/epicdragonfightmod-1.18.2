package susen36.epicdragonfight.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import susen36.epicdragonfight.EpicDragonFight;

import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class DragonFightRenderTypes extends RenderType {
	private DragonFightRenderTypes(String p_173178_, VertexFormat p_173179_, Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
		super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
	}
	
	private static final Function<ResourceLocation, RenderType> ANIMATED_MODEL = Util.memoize((textureLocation) -> {
		CompositeState state = CompositeState.builder()
				.setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
				.setTextureState(new TextureStateShard(textureLocation, false, false))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setCullState(NO_CULL)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
			.createCompositeState(true);
	    return create(EpicDragonFight.MODID + ":animated_model", DefaultVertexFormat.NEW_ENTITY, Mode.TRIANGLES, 256, true, false, state);
	});


	private static final Function<ResourceLocation, RenderType> ENTITY_DECAL_TRIANGLES = Util.memoize((p_173194_) -> {
		CompositeState state = CompositeState.builder()
				.setShaderState(RENDERTYPE_ENTITY_DECAL_SHADER)
				.setTextureState(new TextureStateShard(p_173194_, false, false))
				.setDepthTestState(EQUAL_DEPTH_TEST)
				.setCullState(NO_CULL)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(false);
		return create(EpicDragonFight.MODID + ":entity_decal_triangles", DefaultVertexFormat.NEW_ENTITY, Mode.TRIANGLES, 256, false, false, state);
	});
	
	private static final Function<ResourceLocation, RenderType> DRAGON_EXPLOSION_ALPHA_TRIANGLES = Util.memoize((textureLocation) -> {
		CompositeState state = CompositeState.builder()
				.setShaderState(RENDERTYPE_ENTITY_ALPHA_SHADER)
				.setTextureState(new TextureStateShard(textureLocation, false, false))
				.setCullState(NO_CULL)
				.createCompositeState(true);
		return create(EpicDragonFight.MODID + ":dragon_explosion_triangles", DefaultVertexFormat.NEW_ENTITY, Mode.TRIANGLES, 256, false, false, state);
	});

	private static final RenderType DEBUG_QUADS = create(EpicDragonFight.MODID + ":debug_quad", DefaultVertexFormat.POSITION_COLOR, Mode.QUADS, 256, false, false,
			CompositeState.builder()
				.setShaderState(POSITION_COLOR_SHADER)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.setTransparencyState(NO_TRANSPARENCY)
				.setWriteMaskState(COLOR_DEPTH_WRITE)
				.setCullState(NO_CULL)
				.createCompositeState(false)
	);


	private static final Function<ResourceLocation, RenderType> EYES = Util.memoize((textureLocation) -> {
		CompositeState state = CompositeState.builder()
				.setShaderState(RenderStateShard.RENDERTYPE_EYES_SHADER)
				.setTextureState(new TextureStateShard(textureLocation, false, false))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setCullState(NO_CULL)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(true);
		return create(EpicDragonFight.MODID + ":eyes", DefaultVertexFormat.NEW_ENTITY, Mode.TRIANGLES, 256, true, false, state);
	});
	public static RenderType animatedModel(ResourceLocation locationIn) {
		return ANIMATED_MODEL.apply(locationIn);
	}


	public static RenderType eyes(ResourceLocation locationIn) {
		return EYES.apply(locationIn);
	}
	public static RenderType dragonExplosionAlphaTriangles(ResourceLocation locationIn) {
		return DRAGON_EXPLOSION_ALPHA_TRIANGLES.apply(locationIn);
	}

	public static RenderType entityDecalTriangles(ResourceLocation locationIn) {
		return ENTITY_DECAL_TRIANGLES.apply(locationIn);
	}

	public static RenderType debugQuads() {
		return DEBUG_QUADS;
	}
}