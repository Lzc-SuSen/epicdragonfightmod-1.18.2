package susen36.epicdragonfight.events;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import susen36.epicdragonfight.EpicDragonFight;
import susen36.epicdragonfight.api.animation.types.StaticAnimation;
import susen36.epicdragonfight.network.EpicFightNetworkManager;
import susen36.epicdragonfight.network.client.CPPlayAnimation;
import susen36.epicdragonfight.world.capabilities.DragonFightCapabilities;
import susen36.epicdragonfight.world.capabilities.entitypatch.EntityPatch;
import susen36.epicdragonfight.world.capabilities.entitypatch.LivingEntityPatch;

@Mod.EventBusSubscriber(modid= EpicDragonFight.MODID)
public class EntityEvents {
	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public static void spawnEvent(EntityJoinWorldEvent event) {
		EntityPatch<Entity> entitypatch = event.getEntity().getCapability(DragonFightCapabilities.CAPABILITY_ENTITY, null).orElse(null);

		if (entitypatch != null && !entitypatch.isInitialized()) {
			entitypatch.onJoinWorld(event.getEntity(), event);
		}

		Entity entity = event.getEntity();
		if(entity instanceof EnderMan) {
			if (entity.level.dimension() == Level.END) {
				if (entity.position().horizontalDistanceSqr() < 40000) {
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	public static void updateEvent(LivingUpdateEvent event) {
		LivingEntityPatch<?> entitypatch = (LivingEntityPatch<?>) event.getEntityLiving().getCapability(DragonFightCapabilities.CAPABILITY_ENTITY, null).orElse(null);
		
		if (entitypatch != null && entitypatch.getOriginal() != null) {
			entitypatch.tick(event);
		}
	}
	
	@SubscribeEvent
	public static void knockBackEvent(LivingKnockBackEvent event) {
		EntityPatch<?> cap = event.getEntityLiving().getCapability(DragonFightCapabilities.CAPABILITY_ENTITY).orElse(null);
		
		if (cap != null) {
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void hurtEvent(LivingHurtEvent event) {
		DamageSource extendedDamageSource = null;
		Entity trueSource = event.getSource().getEntity();
		
		if (trueSource != null) {

			
			if (extendedDamageSource != null) {
				LivingEntity hitEntity = event.getEntityLiving();
				float totalDamage = event.getAmount();

				
				float ignoreDamage = totalDamage  * 0.01F;
				float calculatedDamage = ignoreDamage;
				
			    if (hitEntity.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
			    	int i = (hitEntity.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1) * 5;
			        int j = 25 - i;
			        float f = calculatedDamage * (float)j;
			        float f1 = calculatedDamage;
			        calculatedDamage = Math.max(f / 25.0F, 0.0F);
			        float f2 = f1 - calculatedDamage;
			        
					if (f2 > 0.0F && f2 < 3.4028235E37F) {
			        	if (hitEntity instanceof ServerPlayer) {
			        		((ServerPlayer)hitEntity).awardStat(Stats.DAMAGE_RESISTED, Math.round(f2 * 10.0F));
			        	} else if (event.getSource().getEntity() instanceof ServerPlayer) {
			                ((ServerPlayer)event.getSource().getEntity()).awardStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(f2 * 10.0F));
			        	}
			        }
			    }

				if (calculatedDamage > 0.0F) {
					int k = EnchantmentHelper.getDamageProtection(hitEntity.getArmorSlots(), event.getSource());
					if (k > 0) {
			        	calculatedDamage = CombatRules.getDamageAfterMagicAbsorb(calculatedDamage, (float)k);
			        }
			    }
			    
			    float absorpAmount = hitEntity.getAbsorptionAmount() - calculatedDamage;
			    hitEntity.setAbsorptionAmount(Math.max(absorpAmount, 0.0F));
		        float realHealthDamage = Math.max(-absorpAmount, 0.0F);
		        
		        if (realHealthDamage > 0.0F && realHealthDamage < 3.4028235E37F && event.getSource().getEntity() instanceof ServerPlayer) {
		        	((ServerPlayer)event.getSource().getEntity()).awardStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(realHealthDamage * 10.0F));
		        }
		        
				if (absorpAmount < 0.0F) {
					hitEntity.setHealth(hitEntity.getHealth() + absorpAmount);
		        	LivingEntityPatch<?> attacker = (LivingEntityPatch<?>)trueSource.getCapability(DragonFightCapabilities.CAPABILITY_ENTITY, null).orElse(null);
		        	
					if (attacker != null) {
						attacker.gatherDamageDealt(extendedDamageSource, calculatedDamage);
					}
		        }
		        
				event.setAmount(totalDamage - ignoreDamage);
				if (event.getAmount() + ignoreDamage > 0.0F) {
					LivingEntityPatch<?> hitentitypatch = (LivingEntityPatch<?>)hitEntity.getCapability(DragonFightCapabilities.CAPABILITY_ENTITY, null).orElse(null);
					
					if (hitentitypatch != null) {
						StaticAnimation hitAnimation = null;

						float knockBackAmount = 0.0F;


						Vec3 sourcePosition = ((DamageSource)extendedDamageSource).getSourcePosition();
						
						if (sourcePosition != null) {
							if (hitAnimation != null) {
								if (!(hitEntity instanceof Player)) {
									hitEntity.lookAt(EntityAnchorArgument.Anchor.FEET, sourcePosition);
								}
								hitentitypatch.playAnimationSynchronized(hitAnimation, 0);
							}
							
							if (knockBackAmount != 0.0F) {
								hitentitypatch.knockBackEntity(((DamageSource)extendedDamageSource).getSourcePosition(), knockBackAmount);
							}
						}
					}
				}
			}
		}
		
		if (event.getEntityLiving().isUsingItem() && event.getEntityLiving().getUseItem().getItem() == Items.SHIELD) {
			if (event.getEntityLiving() instanceof Player) {
				event.getEntityLiving().level.playSound((Player)event.getEntityLiving(), event.getEntityLiving().blockPosition(), SoundEvents.SHIELD_BLOCK, event.getEntityLiving().getSoundSource(), 1.0F, 0.8F + event.getEntityLiving().getRandom().nextFloat() * 0.4F);
			}
		}
	}
	
	@SubscribeEvent
	public static void damageEvent(LivingDamageEvent event) {
		Entity trueSource = event.getSource().getEntity();
		

			if (trueSource != null) {
				LivingEntityPatch<?> attacker = (LivingEntityPatch<?>) trueSource.getCapability(DragonFightCapabilities.CAPABILITY_ENTITY, null).orElse(null);
				
				if (attacker != null) {
					attacker.gatherDamageDealt(event.getSource(), event.getAmount());
				}

		}
	}
	
	@SubscribeEvent
	public static void attackEvent(LivingAttackEvent event) {
		if (event.getEntity().level.isClientSide()) {
			return;
		}

		    if (event.getAmount() != event.getAmount()) {
				event.setCanceled(true);
				
				DamageSource damagesource = new DamageSource( event.getSource().getMsgId() );
				damagesource.bypassInvul();
				
				event.getEntity().hurt(damagesource, event.getAmount());
			}
		}

	
	@SubscribeEvent
	public static void dropEvent(LivingDropsEvent event) {
		LivingEntityPatch<?> entitypatch = (LivingEntityPatch<?>)event.getEntityLiving().getCapability(DragonFightCapabilities.CAPABILITY_ENTITY).orElse(null);
		
		if (entitypatch != null) {
			if (entitypatch.onDrop(event)) {
				event.setCanceled(true);
			}
		}
	}
	

	@SubscribeEvent
	public static void sizingEvent(EntityEvent.Size event) {
		if (event.getEntity() instanceof EnderDragon) {
			event.setNewSize(EntityDimensions.scalable(3.0F, 5.0F));
		}
	}

	@SubscribeEvent
	public static void jumpEvent(LivingJumpEvent event) {
		LivingEntityPatch<?> entitypatch = (LivingEntityPatch<?>) event.getEntity().getCapability(DragonFightCapabilities.CAPABILITY_ENTITY, null).orElse(null);
		
		if (entitypatch != null && entitypatch.isLogicalClient()) {
			if (!entitypatch.getEntityState().inaction() && !event.getEntity().isInWater()) {
				StaticAnimation jumpAnimation = entitypatch.getClientAnimator().getJumpAnimation();
				entitypatch.getAnimator().playAnimation(jumpAnimation, 0);
				EpicFightNetworkManager.sendToServer(new CPPlayAnimation(jumpAnimation.getNamespaceId(), jumpAnimation.getId(), 0, true, false));
			}
		}
	}
	
	@SubscribeEvent
	public static void deathEvent(LivingDeathEvent event) {
		LivingEntityPatch<?> entitypatch = (LivingEntityPatch<?>)event.getEntityLiving().getCapability(DragonFightCapabilities.CAPABILITY_ENTITY, null).orElse(null);
		
		if (entitypatch != null) {
			entitypatch.onDeath();
		}
	}

}