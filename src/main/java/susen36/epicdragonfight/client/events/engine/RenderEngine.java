package susen36.epicdragonfight.client.events.engine;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.common.Mod;
import susen36.epicdragonfight.EpicDragonFight;
import susen36.epicdragonfight.api.client.forgeevent.PatchedRenderersEvent;
import susen36.epicdragonfight.api.client.forgeevent.RenderEnderDragonEvent;
import susen36.epicdragonfight.api.utils.math.Vec3f;
import susen36.epicdragonfight.client.renderer.AimHelperRenderer;
import susen36.epicdragonfight.client.renderer.patched.entity.PEnderDragonRenderer;
import susen36.epicdragonfight.client.renderer.patched.entity.PatchedEntityRenderer;
import susen36.epicdragonfight.world.capabilities.DragonFightCapabilities;
import susen36.epicdragonfight.world.capabilities.entitypatch.LivingEntityPatch;
import susen36.epicdragonfight.world.capabilities.entitypatch.boss.enderdragon.EnderDragonPatch;

import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("rawtypes")
@OnlyIn(Dist.CLIENT)
public class RenderEngine {
	private static final Vec3f AIMING_CORRECTION = new Vec3f(-1.5F, 0.0F, 1.25F);
	
	public AimHelperRenderer aimHelper;

	private Minecraft minecraft;
	private Map<EntityType<?>, Supplier<PatchedEntityRenderer>> entityRendererProvider;
	private Map<EntityType<?>, PatchedEntityRenderer> entityRendererCache;

	private boolean aiming;
	private int zoomOutTimer = 0;
	private int zoomCount;
	private int zoomMaxCount = 20;
	private float cameraXRot;
	private float cameraYRot;
	private float cameraXRotO;
	private float cameraYRotO;
	private boolean isPlayerRotationLocked;
	
	public RenderEngine() {
		Events.renderEngine = this;
		this.minecraft = Minecraft.getInstance();
		this.entityRendererProvider = Maps.newHashMap();
		this.entityRendererCache = Maps.newHashMap();
	}
	
	public void registerRenderer() {
		this.entityRendererProvider.put(EntityType.ENDER_DRAGON, PEnderDragonRenderer::new);

		this.aimHelper = new AimHelperRenderer();
		
		ModLoader.get().postEvent(new PatchedRenderersEvent.Add(this.entityRendererProvider));
		
		for (Map.Entry<EntityType<?>, Supplier<PatchedEntityRenderer>> entry : this.entityRendererProvider.entrySet()) {
			this.entityRendererCache.put(entry.getKey(), entry.getValue().get());
		}
		
	}
	

	@SuppressWarnings("unchecked")
	public void renderEntityArmatureModel(LivingEntity livingEntity, LivingEntityPatch<?> entitypatch, LivingEntityRenderer<? extends Entity, ?> renderer, MultiBufferSource buffer, PoseStack matStack, int packedLightIn, float partialTicks) {
		this.getEntityRenderer(livingEntity).render(livingEntity, entitypatch, renderer, buffer, matStack, packedLightIn, partialTicks);
	}
	
	public PatchedEntityRenderer getEntityRenderer(Entity entity) {
		return this.entityRendererCache.get(entity.getType());
	}
	
	public boolean hasRendererFor(Entity entity) {
		return this.entityRendererCache.computeIfAbsent(entity.getType(), (key) -> this.entityRendererProvider.containsKey(key) ? this.entityRendererProvider.get(entity.getType()).get() : null) != null;
	}
	
	public void clearCustomEntityRenerer() {
		this.entityRendererCache.clear();
	}

	
	@Mod.EventBusSubscriber(modid = EpicDragonFight.MODID, value = Dist.CLIENT)
	public static class Events {
		static RenderEngine renderEngine;
		
		@SubscribeEvent
		public static void renderLivingEvent(RenderLivingEvent.Pre<? extends LivingEntity, ? extends EntityModel<? extends LivingEntity>> event) {
			LivingEntity livingentity = event.getEntity();
			
			if (renderEngine.hasRendererFor(livingentity)) {
				if (livingentity instanceof LocalPlayer && event.getPartialTick() == 1.0F) {
					return;
				}
				
				LivingEntityPatch<?> entitypatch = (LivingEntityPatch<?>) livingentity.getCapability(DragonFightCapabilities.CAPABILITY_ENTITY, null).orElse(null);
				
				if (entitypatch != null && !entitypatch.shouldSkipRender()) {
					event.setCanceled(true);
					renderEngine.renderEntityArmatureModel(livingentity, entitypatch, event.getRenderer(), event.getMultiBufferSource(), event.getPoseStack(), event.getPackedLight(), event.getPartialTick());
				}
			}
		}

		@SubscribeEvent
		public static void renderWorldLast(RenderLevelStageEvent event) {
			if (renderEngine.zoomCount > 0 && renderEngine.minecraft.options.getCameraType() == CameraType.THIRD_PERSON_BACK
					&& event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
				renderEngine.aimHelper.doRender(event.getPoseStack(), event.getPartialTick());
			}
		}
		
		@SuppressWarnings("unchecked")
		@SubscribeEvent
		public static void renderEnderDragonEvent(RenderEnderDragonEvent event) {
			EnderDragon livingentity = event.getEntity();
			
			if (renderEngine.hasRendererFor(livingentity)) {
				EnderDragonPatch entitypatch = (EnderDragonPatch) livingentity.getCapability(DragonFightCapabilities.CAPABILITY_ENTITY, null).orElse(null);
				
				if (entitypatch != null) {
					event.setCanceled(true);
					renderEngine.getEntityRenderer(livingentity).render(livingentity, entitypatch, event.getRenderer(), event.getBuffers(), event.getPoseStack(), event.getLight(), event.getPartialRenderTick());
				}
			}
		}
	}
}