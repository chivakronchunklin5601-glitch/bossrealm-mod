package com.bossrealm.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

/**
 * MEDIUM boss - a fire demon with a flame blade. Faster, mixes melee, ranged and area attacks.
 *
 * Stats : 350 HP, armor 8 (about iron level), immune to fire.
 * Skills: Blade Combo / Fireball Volley / Flame Dash / Fire Nova / Inferno Rain (phase 2 only).
 */
public class InfernalReaver extends BossBase {
    public static final int COMBO = 1;
    public static final int FIREBALLS = 2;
    public static final int DASH = 3;
    public static final int NOVA = 4;
    public static final int RAIN = 5;

    private final Set<UUID> hitSet = new HashSet<>();
    private final List<Vec3> pillars = new ArrayList<>();
    private Vec3 dashDir = Vec3.ZERO;

    public InfernalReaver(EntityType<? extends InfernalReaver> type, Level level) {
        super(type, level, BossEvent.BossBarColor.RED, 250);
        this.setPathfindingMalus(BlockPathTypes.LAVA, 8.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        // health, armor, toughness, attack damage, speed, knockback resistance
        return bossAttributes(350.0D, 8.0D, 0.0D, 12.0D, 0.29D, 0.7D);
    }

    @Override
    public double preferredRange() {
        return 2.8D;
    }

    // ------------------------------------------------------------------ skill selection

    @Override
    protected int chooseSkill(LivingEntity target, double distanceSqr) {
        double d = Math.sqrt(distanceSqr);
        int roll = this.getRandom().nextInt(100);

        if (this.isEnraged() && d > 3.0D && d < 18.0D && roll < 8) {
            return RAIN;
        }
        if (d <= 4.0D) {
            if (roll < 55) {
                return COMBO;
            }
            return roll < 80 ? NOVA : 0;
        }
        boolean los = this.hasLineOfSight(target);
        if (d <= 9.0D) {
            if (d <= 7.0D && roll < 15) {
                return NOVA;
            }
            if (roll < 50) {
                return DASH;
            }
            return los && roll < 80 ? FIREBALLS : 0;
        }
        if (roll < 45) {
            return DASH;
        }
        return los && roll < 85 ? FIREBALLS : 0;
    }

    @Override
    protected int skillDuration(int skill) {
        return switch (skill) {
            case COMBO -> 30;
            case FIREBALLS -> 32;
            case DASH -> 40;
            case NOVA -> 50;
            case RAIN -> 60;
            default -> 20;
        };
    }

    @Override
    protected int skillCooldown(int skill) {
        return switch (skill) {
            case COMBO -> 18;
            case FIREBALLS -> 40;
            case DASH -> 45;
            case NOVA -> 50;
            case RAIN -> 70;
            default -> 30;
        };
    }

    @Override
    protected int enrageSkill() {
        return RAIN;
    }

    @Override
    protected void onSkillStart(int skill, LivingEntity target) {
        this.hitSet.clear();
        this.pillars.clear();
        this.dashDir = Vec3.ZERO;
    }

    // ------------------------------------------------------------------ skills

    @Override
    protected void onSkillTick(int skill, int t, LivingEntity target) {
        switch (skill) {
            case COMBO -> tickCombo(t, target);
            case FIREBALLS -> tickFireballs(t, target);
            case DASH -> tickDash(t, target);
            case NOVA -> tickNova(t, target);
            case RAIN -> tickRain(t, target);
            default -> {
            }
        }
    }

    /** Two slashes: animation hit frames at tick 10 (0.5 s) and tick 22 (1.1 s). */
    private void tickCombo(int t, LivingEntity target) {
        if (t < 9 || (t >= 14 && t < 21)) {
            this.faceTarget(target);
        }
        if (t == 9 || t == 21) {
            Vec3 f = this.forward();
            this.setDeltaMovement(this.getDeltaMovement().add(f.x * 0.45D, 0.0D, f.z * 0.45D));
        }
        if (t == 10 || t == 22) {
            boolean second = t == 22;
            this.playSound(SoundEvents.PLAYER_ATTACK_STRONG, 1.5F, second ? 0.6F : 0.8F);
            this.playSound(SoundEvents.BLAZE_SHOOT, 1.0F, 0.7F);
            Vec3 f = this.forward();
            this.particles(ParticleTypes.FLAME, this.getX() + f.x * 2.4D, this.getY() + 1.7D, this.getZ() + f.z * 2.4D,
                    30, 1.0D, 0.5D, 1.0D, 0.05D);
            this.particles(ParticleTypes.SWEEP_ATTACK, this.getX() + f.x * 2.2D, this.getY() + 1.6D,
                    this.getZ() + f.z * 2.2D, 2, 0.8D, 0.3D, 0.8D, 0.0D);
            this.hitCone(4.2D, second ? 80.0D : 70.0D, this.dmg(0.85F), second ? 1.1D : 0.6D, second ? 0.45D : 0.2D,
                    e -> e.setSecondsOnFire(4));
        }
    }

    /** Three fans of 3 fireballs, thrown at ticks 14, 20 and 26 (the pumping frames of the animation). */
    private void tickFireballs(int t, LivingEntity target) {
        if (t < 30) {
            this.faceTarget(target);
        }
        if (t == 4) {
            this.playSound(SoundEvents.BLAZE_AMBIENT, 1.5F, 0.5F);
        }
        if (t < 14 && t % 3 == 0) {
            this.particles(ParticleTypes.FLAME, this.getX(), this.getY() + 3.0D, this.getZ(), 6, 0.6D, 0.4D, 0.6D, 0.03D);
        }
        if (t == 14 || t == 20 || t == 26) {
            Vec3 origin = new Vec3(this.getX(), this.getEyeY() - 0.3D, this.getZ()).add(this.forward().scale(1.2D));
            Vec3 dir = target.getEyePosition().subtract(origin).normalize();
            for (int i = -1; i <= 1; i++) {
                double angle = Math.toRadians(i * 12.0D);
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);
                Vec3 shot = new Vec3(dir.x * cos - dir.z * sin, dir.y, dir.x * sin + dir.z * cos);
                SmallFireball fireball = new SmallFireball(this.level(), this, shot.x, shot.y, shot.z);
                fireball.setPos(origin.x, origin.y, origin.z);
                this.level().addFreshEntity(fireball);
            }
            this.playSound(SoundEvents.BLAZE_SHOOT, 1.6F, 0.8F);
        }
    }

    /** Crouches until tick 14, then charges in a straight line until tick 30, burning everything it touches. */
    private void tickDash(int t, LivingEntity target) {
        if (t < 14) {
            this.faceTarget(target);
            if (t == 2) {
                this.playSound(SoundEvents.RAVAGER_ROAR, 1.5F, 0.8F);
            }
            if (t % 3 == 0) {
                this.particles(ParticleTypes.FLAME, this.getX(), this.getY() + 0.2D, this.getZ(), 8, 0.8D, 0.1D, 0.8D, 0.03D);
            }
            return;
        }
        if (t == 14) {
            Vec3 to = new Vec3(target.getX() - this.getX(), 0.0D, target.getZ() - this.getZ());
            this.dashDir = to.lengthSqr() < 1.0E-4D ? this.forward() : to.normalize();
            this.playSound(SoundEvents.BLAZE_SHOOT, 2.0F, 0.5F);
        }
        if (t < 30 && this.dashDir != Vec3.ZERO) {
            this.setDeltaMovement(this.dashDir.x * 1.05D, this.getDeltaMovement().y, this.dashDir.z * 1.05D);
            this.faceYaw((float) (Math.atan2(this.dashDir.z, this.dashDir.x) * (180.0D / Math.PI)) - 90.0F);
            this.particles(ParticleTypes.FLAME, this.getX(), this.getY() + 0.5D, this.getZ(), 10, 0.6D, 0.5D, 0.6D, 0.02D);
            this.particles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 4, 0.5D, 0.6D, 0.5D, 0.02D);
            for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(0.6D, 0.2D, 0.6D), this::canHarm)) {
                if (this.hitSet.add(victim.getUUID()) && this.damageEntity(victim, this.dmg(1.1F))) {
                    victim.setSecondsOnFire(5);
                    this.launch(victim, this.position(), 1.5D, 0.5D);
                }
            }
        }
    }

    /** Slam at tick 30 (1.5 s); a ring of fire then expands outwards for 16 ticks. Jump over it to avoid it. */
    private void tickNova(int t, LivingEntity target) {
        if (t < 24) {
            this.faceTarget(target);
        }
        if (t == 4) {
            this.playSound(SoundEvents.BLAZE_AMBIENT, 2.0F, 0.4F);
        }
        if (t < 30 && t % 3 == 0) {
            this.particles(ParticleTypes.FLAME, this.getX(), this.getY() + 1.0D + t * 0.07D, this.getZ(),
                    6, 0.7D, 0.5D, 0.7D, 0.02D);
        }
        if (t == 30) {
            this.playSound(SoundEvents.GENERIC_EXPLODE, 1.6F, 0.8F);
            this.playSound(SoundEvents.FIRECHARGE_USE, 2.0F, 0.6F);
            this.particles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.3D, this.getZ(), 6, 1.5D, 0.2D, 1.5D, 0.0D);
        }
        if (t >= 30 && t <= 46) {
            double radius = (t - 30) * 0.55D;
            this.ring(ParticleTypes.FLAME, this.getX(), this.getY() + 0.15D, this.getZ(), radius, 44);
            this.ring(ParticleTypes.LAVA, this.getX(), this.getY() + 0.25D, this.getZ(), radius, 8);
            for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(10.0D, 3.0D, 10.0D), this::canHarm)) {
                double dx = victim.getX() - this.getX();
                double dz = victim.getZ() - this.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                double rise = victim.getY() - this.getY();
                if (Math.abs(dist - radius) <= 1.0D && rise < 1.0D && rise > -2.5D
                        && this.hitSet.add(victim.getUUID()) && this.damageEntity(victim, this.dmg(1.0F))) {
                    victim.setSecondsOnFire(5);
                    this.launch(victim, this.position(), 0.9D, 0.6D);
                }
            }
        }
    }

    /** Picks 6 spots at tick 8, shows warnings, and everything erupts at tick 40 (2.0 s). */
    private void tickRain(int t, LivingEntity target) {
        if (t < 36) {
            this.faceTarget(target);
        }
        if (t == 4) {
            this.playSound(SoundEvents.RAVAGER_ROAR, 2.0F, 0.6F);
        }
        if (t == 8) {
            this.pillars.addAll(this.pickPillars(target, 6, 6.0D));
        }
        if (t >= 8 && t < 40 && t % 2 == 0) {
            for (Vec3 p : this.pillars) {
                this.ring(ParticleTypes.FLAME, p.x, p.y + 0.1D, p.z, 2.0D, 16);
                this.particles(ParticleTypes.SMOKE, p.x, p.y + 0.3D, p.z, 3, 0.5D, 0.2D, 0.5D, 0.01D);
            }
        }
        if (t == 40) {
            this.playSound(SoundEvents.GENERIC_EXPLODE, 2.0F, 0.7F);
            for (Vec3 p : this.pillars) {
                this.particles(ParticleTypes.LAVA, p.x, p.y + 0.5D, p.z, 30, 1.0D, 0.5D, 1.0D, 0.0D);
                this.particles(ParticleTypes.FLAME, p.x, p.y + 2.0D, p.z, 80, 0.6D, 2.5D, 0.6D, 0.08D);
                this.particles(ParticleTypes.EXPLOSION, p.x, p.y + 0.5D, p.z, 2, 0.5D, 0.2D, 0.5D, 0.0D);
                this.hitRadius(p, 2.4D, 3.0D, this.dmg(1.3F), 0.2D, 0.9D, e -> e.setSecondsOnFire(6));
            }
        }
    }

    @Override
    protected void onEnrage() {
        this.particles(ParticleTypes.FLAME, this.getX(), this.getY() + 1.5D, this.getZ(), 120, 1.0D, 1.8D, 1.0D, 0.12D);
        this.particles(ParticleTypes.LAVA, this.getX(), this.getY() + 1.0D, this.getZ(), 30, 1.0D, 1.0D, 1.0D, 0.0D);
    }

    // ------------------------------------------------------------------ sounds

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BLAZE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BLAZE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BLAZE_DEATH;
    }
}
