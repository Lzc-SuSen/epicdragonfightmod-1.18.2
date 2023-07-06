package susen36.epicdragonfight;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DataSerializerEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import susen36.epicdragonfight.api.animation.*;
import susen36.epicdragonfight.api.client.animation.ClientAnimator;
import susen36.epicdragonfight.api.client.model.ClientModels;
import susen36.epicdragonfight.client.ClientEngine;
import susen36.epicdragonfight.events.CapabilityEvent;
import susen36.epicdragonfight.events.EntityEvents;
import susen36.epicdragonfight.gameasset.Animations;
import susen36.epicdragonfight.gameasset.Models;
import susen36.epicdragonfight.network.DraagonFightDataSerializers;
import susen36.epicdragonfight.network.DragoFightNetworkManager;
import susen36.epicdragonfight.world.capabilities.DragonFightCapabilities;
import susen36.epicdragonfight.world.capabilities.entitypatch.LivingEntityPatch;
import susen36.epicdragonfight.world.capabilities.provider.ProviderEntity;
import susen36.epicdragonfight.world.entity.DragonFightEntities;

import java.util.function.Function;

@Mod("epicdragonfight")
public class EpicDragonFight {
	public static final String MODID = "epicdragonfight";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	private static EpicDragonFight instance;
	
	public static EpicDragonFight getInstance() {
		return instance;
	}
	
	public final AnimationManager animationManager;
	private Function<LivingEntityPatch<?>, Animator> animatorProvider;

	
    public EpicDragonFight() {
    	this.animationManager = new AnimationManager();
    	instance = this;
    	IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    	bus.addListener(this::doClientStuff);
    	bus.addListener(this::doCommonStuff);
    	bus.addListener(this::doServerStuff);
    	bus.addListener(DragonFightCapabilities::registerCapabilities);
    	bus.addListener(Animations::registerAnimations);
    	bus.addGenericListener(DataSerializerEntry.class, DraagonFightDataSerializers::register);

    	LivingMotion.ENUM_MANAGER.loadPreemptive(LivingMotions.class);

		DragonFightEntities.ENTITIES.register(bus);

        MinecraftForge.EVENT_BUS.register(EntityEvents.class);
        MinecraftForge.EVENT_BUS.register(CapabilityEvent.class);
        
     }
    
	private void doClientStuff(final FMLClientSetupEvent event) {
    	new ClientEngine();

		this.animatorProvider = ClientAnimator::getAnimator;
		ProviderEntity.registerEntityPatchesClient();
		ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
		ClientModels.LOGICAL_CLIENT.loadModels(resourceManager);
		ClientModels.LOGICAL_CLIENT.loadArmatures(resourceManager);
		Models.LOGICAL_SERVER.loadArmatures(resourceManager);
		this.animationManager.loadAnimationsInit(resourceManager);
        ((ReloadableResourceManager)resourceManager).registerReloadListener(ClientModels.LOGICAL_CLIENT);
        ((ReloadableResourceManager)resourceManager).registerReloadListener(this.animationManager);
    }
	
	private void doServerStuff(final FMLDedicatedServerSetupEvent event) {
		Models.LOGICAL_SERVER.loadArmatures(null);
		this.animationManager.loadAnimationsInit(null);
		this.animatorProvider = ServerAnimator::getAnimator;
	}
	
	private void doCommonStuff(final FMLCommonSetupEvent event) {
		event.enqueueWork(this.animationManager::registerAnimations);
		event.enqueueWork(DragoFightNetworkManager::registerPackets);
		event.enqueueWork(ProviderEntity::registerEntityPatches);
    }

	
	public static Animator getAnimator(LivingEntityPatch<?> entitypatch) {
		return EpicDragonFight.getInstance().animatorProvider.apply(entitypatch);
	}
	
	public static boolean isPhysicalClient() {
    	return FMLEnvironment.dist == Dist.CLIENT;
    }
}