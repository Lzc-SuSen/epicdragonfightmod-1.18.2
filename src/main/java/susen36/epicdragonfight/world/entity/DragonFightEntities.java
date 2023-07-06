package susen36.epicdragonfight.world.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import susen36.epicdragonfight.EpicDragonFight;

public class DragonFightEntities {
	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, EpicDragonFight.MODID);
	
	public static final RegistryObject<EntityType<AreaEffectBreath>> AREA_EFFECT_BREATH = ENTITIES.register("area_effect_breath", () ->
		EntityType.Builder.<AreaEffectBreath>of(AreaEffectBreath::new, MobCategory.MISC)
			.fireImmune().sized(6.0F, 0.5F).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE).noSummon().build("area_effect_breath")
		);
	public static final RegistryObject<EntityType<DragonBreathball>> DRAGON_BREATH_BALL = ENTITIES.register("dragon_breath_ball", () ->
			EntityType.Builder.<DragonBreathball>of(DragonBreathball::new, MobCategory.MISC)
					.fireImmune().sized(0.8F, 0.8F).clientTrackingRange(5).updateInterval(10).noSummon().build("dragon_breath_ball")
	);
}