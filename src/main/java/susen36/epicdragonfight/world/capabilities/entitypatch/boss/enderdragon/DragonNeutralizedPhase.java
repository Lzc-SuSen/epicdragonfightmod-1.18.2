package susen36.epicdragonfight.world.capabilities.entitypatch.boss.enderdragon;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import susen36.epicdragonfight.gameasset.Animations;

public class DragonNeutralizedPhase extends PatchedDragonPhase {
	public DragonNeutralizedPhase(EnderDragon dragon) {
		super(dragon);
	}
	
	@Override
	public void begin() {
		this.dragonpatch.getAnimator().playAnimation(Animations.DRAGON_NEUTRALIZED, 0.0F);
		
		if (this.dragonpatch.isLogicalClient()) {
			Minecraft.getInstance().getSoundManager().stop(SoundEvents.ENDER_DRAGON_SHOOT.getLocation(), SoundSource.HOSTILE);
			this.dragon.level.addParticle(ParticleTypes.END_ROD, this.dragon.getX(), this.dragon.getY(), this.dragon.getZ(), 0, 0, 0);
		}
	}
	
	@Override
	public EnderDragonPhase<? extends DragonPhaseInstance> getPhase() {
		return PatchedPhases.NEUTRALIZED;
	}
	
	@Override
	public boolean isSitting() {
		return true;
	}
}