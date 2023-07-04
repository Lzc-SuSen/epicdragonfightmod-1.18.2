package susen36.epicdragonfight.world.entity;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class DragonBreathBall extends Fireball {

    private float explosionPower = 2F;
    public DragonBreathBall(EntityType<DragonBreathBall> type, Level level) {
        super(type,level);
    }
    public DragonBreathBall(Level level, LivingEntity entity, double p_181153_, double p_181154_, double p_181155_, float explosionPower) {
        super(DragonFightEntities.DRAGON_BREATH_BALL.get(), entity, p_181153_, p_181154_, p_181155_, level);
        this.explosionPower = explosionPower;
    }
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level.isClientSide) {
            this.level.explode((Entity)null, this.getX(), this.getY(), this.getZ(), (float)this.explosionPower, Explosion.BlockInteraction.NONE);
            this.discard();
        }

    }
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level.isClientSide) {
            Entity entity = result.getEntity();
            Entity entity1 = this.getOwner();
            entity.hurt(DamageSource.DRAGON_BREATH.setExplosion(), 8.0F);
            if (entity1 instanceof LivingEntity) {
                this.doEnchantDamageEffects((LivingEntity)entity1, entity);
            }
        }
    }

    protected ParticleOptions getTrailParticle() {
        return ParticleTypes.DRAGON_BREATH;
    }
}
