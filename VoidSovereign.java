package com.bossrealm.entity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * HARD boss - a floating void lord. Short cooldowns, teleports, a sweeping beam and area control.
 *
 * Stats : 500 HP, armor 12 + toughness 2 (well below full diamond armor), very high knockback resistance.
 * Skills: Void Slash / Blink Strike / Void Beam / Gravity Well / Summon Wraiths / Void Rain (phase 2 opener).
 */
public class VoidSovereign extends BossBase {
    public static final int SLASH = 1;
    public static final int BLINK = 2;
    public static final int BEAM = 3;
    public static final int GRAVITY = 4;
    public static final int SUMMON = 5;
    public static final int RAIN = 6;

    private final List<Vec3> pillars = new ArrayList<>();
    private float beamYaw;
    private float beamPitch;

    public VoidSovereign(EntityType<? extends VoidSovereign> type, Level level) {
        super(type, level, BossEvent.BossBarColor.PURPLE, 500);
    }

    public static AttributeSupplier.Builder createAttributes() {
        // health, armor, toughness, attack damage, speed, knockback resistance
        return bossAttributes(500.0D, 12.0D, 2.0D, 14.0D, 0.27D, 0.85D);
    }

    @Override
    public double preferredRange() {
        return 3.2D;
    }

    // ------------------------------------------------------------------ skill selection

    @Override
    protected int chooseSkill(LivingEntity target, double distanceSqr) {
        double d = Math.sqrt(distanceSqr);
        int roll = this.getRandom().nextInt(100);
        boolean los = this.hasLineOfSight(target);

        if (this.isEnraged() && d <= 20.0D && roll < 7) {
            return RAIN;
        }
        if (d <= 4.8D) {
            if (roll < 45) {
                return SLASH;
            }
            if (roll < 65) {
                return GRAVITY;
            }
            return roll < 80 ? BLINK : 0;
        }
        if (d <= 12.0D) {
            if (roll < 30) {
                return BLINK;
            }
            if (roll < 55) {
                return los ? BEAM : 0;
            }
            if (roll < 70) {
                return GRAVITY;
            }
            return roll < 80 && this.countMinions() < 3 ? SUMMON : 0;
        }
        if (roll < 35) {
            return los ? BEAM : 0;
        }
        if (roll < 70) {
            return BLINK;
        }
        return roll < 85 && this.countMinions() < 3 ? SUMMON : 0;
    }

    @Override
    protected int skillDuration(int skill) {
        return switch (skill) {
            case SLASH -> 22;
            case BLINK -> 36;
            case BEAM -> 56;
            case GRAVITY -> 56;
            case SUMMON -> 50;
            case RAIN -> 60;
            default -> 20;
        };
    }

    @Override
    protected int skillCooldown(int skill) {
        return switch (skill) {
            case SLASH -> 14;
            case BLINK -> 24;
            case BEAM -> 30;
            case GRAVITY -> 40;
            case SUMMON -> 70;
            case RAIN -> 60;
            default -> 25;
        };
    }

    @Override
    protected int enrageSkill() {
        return RAIN;
    }

    @Override
    protected void onSkillStart(int skill, LivingEntity target) {
        this.pillars.clear();
    }

    // ------------------------------------------------------------------ skills

    @Override
    protected void onSkillTick(int skill, int t, LivingEntity target) {
        switch (skill) {
            case SLASH -> tickSlash(t, target);
            case BLINK -> tickBlink(t, target);
            case BEAM -> tickBeam(t, target);
            case GRAVITY -> tickGravity(t, target);
            case SUMMON -> tickSummon(t, target);
            case RAIN -> tickRain(t, target);
            default -> {
            }
        }
    }

    private static void slow(LivingEntity e, int ticks, int amplifier) {
        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, amplifier));
    }

    /** Two quick slashes: animation hit frames at tick 8 (0.4 s) and tick 16 (0.8 s). */
    private void tickSlash(int t, LivingEntity target) {
        if (t < 7 || (t >= 10 && t < 15)) {
            this.faceTarget(target);
        }
        if (t == 8 || t == 16) {
            this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.6F, 0.5F);
            this.playSound(SoundEvents.ENDERMAN_SCREAM, 0.6F, 1.6F);
            Vec3 f = this.forward();
            this.particles(ParticleTypes.SWEEP_ATTACK, this.getX() + f.x * 2.5D, this.getY() + 2.2D,
                    this.getZ() + f.z * 2.5D, 3, 0.9D, 0.3D, 0.9D, 0.0D);
            this.particles(ParticleTypes.PORTAL, this.getX() + f.x * 2.5D, this.getY() + 2.0D,
                    this.getZ() + f.z * 2.5D, 30, 0.8D, 0.6D, 0.8D, 0.3D);
            this.hitCone(4.6D, 75.0D, this.dmg(0.9F), 0.8D, 0.3D, e -> slow(e, 40, 0));
        }
    }

    /** Fades out, reappears behind the target at tick 12 and strikes at tick 24 (1.2 s). */
    private void tickBlink(int t, LivingEntity target) {
        if (t < 10 || (t > 12 && t < 22)) {
            this.faceTarget(target);
        }
        if (t == 2) {
            this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.5F, 0.5F);
            this.particles(ParticleTypes.PORTAL, this.getX(), this.getY() + 2.0D, this.getZ(), 80, 0.6D, 1.8D, 0.6D, 0.6D);
        }
        if (t == 12) {
            this.blinkNear(target);
            this.particles(ParticleTypes.PORTAL, this.getX(), this.getY() + 2.0D, this.getZ(), 80, 0.6D, 1.8D, 0.6D, 0.6D);
            this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.5F, 0.7F);
            this.faceTarget(target);
        }
        if (t == 24) {
            this.playSound(SoundEvents.PLAYER_ATTACK_STRONG, 1.8F, 0.5F);
            Vec3 f = this.forward();
            this.particles(ParticleTypes.SWEEP_ATTACK, this.getX() + f.x * 2.5D, this.getY() + 2.2D,
                    this.getZ() + f.z * 2.5D, 3, 0.9D, 0.3D, 0.9D, 0.0D);
            this.hitCone(4.8D, 80.0D, this.dmg(1.5F), 1.0D, 0.5D, e -> slow(e, 40, 0));
        }
    }

    /** Tries behind the target first, then each side. If every spot is blocked the boss simply stays put. */
    private void blinkNear(LivingEntity target) {
        Vec3 look = target.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0.0D, look.z);
        flat = flat.lengthSqr() < 1.0E-4D ? this.forward() : flat.normalize();
        double[] angles = {180.0D, 90.0D, -90.0D};
        for (double degrees : angles) {
            double rad = Math.toRadians(degrees);
            double rx = flat.x * Math.cos(rad) - flat.z * Math.sin(rad);
            double rz = flat.x * Math.sin(rad) + flat.z * Math.cos(rad);
            if (this.randomTeleport(target.getX() + rx * 2.8D, target.getY(), target.getZ() + rz * 2.8D, true)) {
                return;
            }
        }
    }

    /** Aims until tick 22, then fires a sweeping beam from tick 24 to 46 (1.2 s - 2.3 s). Blocks stop the beam. */
    private void tickBeam(int t, LivingEntity target) {
        if (t < 22) {
            this.faceTarget(target);
        }
        if (t == 2) {
            this.playSound(SoundEvents.WARDEN_SONIC_CHARGE, 2.0F, 1.0F);
        }
        if (t < 24 && t % 2 == 0) {
            Vec3 f = this.forward();
            this.ring(ParticleTypes.PORTAL, this.getX() + f.x * 1.3D, this.getY() + this.getBbHeight() * 0.62D,
                    this.getZ() + f.z * 1.3D, 1.6D - t * 0.06D, 10);
        }
        if (t == 22) {
            this.beamYaw = this.getYRot();
            Vec3 chest = new Vec3(this.getX(), this.getY() + this.getBbHeight() * 0.62D, this.getZ());
            Vec3 to = target.getBoundingBox().getCenter().subtract(chest);
            double horizontal = Math.sqrt(to.x * to.x + to.z * to.z);
            this.beamPitch = (float) -(Math.atan2(to.y, horizontal) * (180.0D / Math.PI));
        }
        if (t >= 24 && t <= 46) {
            this.fireBeam(t);
        }
    }

    private void fireBeam(int t) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        float yaw = this.beamYaw + Mth.lerp((t - 24) / 22.0F, -20.0F, 20.0F);
        this.faceYaw(yaw);
        if (t == 24) {
            this.playSound(SoundEvents.WARDEN_SONIC_BOOM, 2.5F, 0.8F);
        }

        Vec3 dir = Vec3.directionFromRotation(this.beamPitch, yaw);
        Vec3 origin = new Vec3(this.getX(), this.getY() + this.getBbHeight() * 0.62D, this.getZ()).add(dir.scale(1.0D));
        Vec3 end = origin.add(dir.scale(24.0D));
        HitResult hit = this.level().clip(new ClipContext(origin, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this));
        Vec3 stop = hit.getType() == HitResult.Type.MISS ? end : hit.getLocation();

        double length = origin.distanceTo(stop);
        int step = 0;
        for (double s = 0.0D; s < length; s += 0.7D, step++) {
            Vec3 p = origin.add(dir.scale(s));
            serverLevel.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 1, 0.05D, 0.05D, 0.05D, 0.0D);
            if (step % 2 == 0) {
                serverLevel.sendParticles(ParticleTypes.PORTAL, p.x, p.y, p.z, 2, 0.2D, 0.2D, 0.2D, 0.2D);
            }
        }
        if (t % 4 == 0) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, stop.x, stop.y, stop.z, 1, 0.3D, 0.3D, 0.3D, 0.0D);
        }

        if ((t - 24) % 5 == 0) {
            AABB area = new AABB(origin, stop).inflate(1.5D);
            for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, area, this::canHarm)) {
                if (victim.getBoundingBox().inflate(0.4D).clip(origin, stop).isPresent()) {
                    victim.invulnerableTime = 0;
                    if (this.damageEntity(victim, this.dmg(0.45F))) {
                        slow(victim, 30, 0);
                    }
                }
            }
        }
    }

    /** Pulls everyone in from tick 8, implodes at tick 44 (2.2 s). Run away from the boss to avoid the blast. */
    private void tickGravity(int t, LivingEntity target) {
        if (t < 44) {
            this.faceTarget(target);
        }
        if (t == 4) {
            this.playSound(SoundEvents.ENDERMAN_STARE, 2.0F, 0.5F);
        }
        if (t >= 8 && t < 44) {
            if (t % 2 == 0) {
                double radius = Math.max(2.0D, 12.0D - (t - 8) * 0.3D);
                this.ring(ParticleTypes.PORTAL, this.getX(), this.getY() + 0.6D, this.getZ(), radius, 20);
                this.ring(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY() + 1.8D, this.getZ(), radius * 0.7D, 14);
            }
            for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(16.0D), this::canHarm)) {
                Vec3 to = this.position().subtract(victim.position());
                if (to.horizontalDistanceSqr() < 6.25D) {
                    continue;
                }
                Vec3 pull = new Vec3(to.x, 0.0D, to.z).normalize().scale(0.075D);
                victim.setDeltaMovement(victim.getDeltaMovement().add(pull.x, 0.0D, pull.z));
                victim.hurtMarked = true;
            }
        }
        if (t == 44) {
            this.playSound(SoundEvents.GENERIC_EXPLODE, 2.5F, 0.5F);
            this.particles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 1.5D, this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            this.particles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.5D, this.getZ(), 220, 1.5D, 1.8D, 1.5D, 1.2D);
            this.hitRadius(this.position(), 6.5D, 4.0D, this.dmg(1.3F), 1.2D, 0.9D, e -> slow(e, 60, 1));
        }
    }

    /** Summons 3 vexes at tick 30 (1.5 s). They live for 20 seconds and fight for the boss. */
    private void tickSummon(int t, LivingEntity target) {
        if (t < 30) {
            this.faceTarget(target);
        }
        if (t == 2) {
            this.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 2.0F, 0.6F);
        }
        if (t < 30 && t % 3 == 0) {
            this.ring(ParticleTypes.PORTAL, this.getX(), this.getY() + 0.5D + t * 0.08D, this.getZ(), 2.5D, 12);
        }
        if (t == 30 && this.level() instanceof ServerLevel serverLevel) {
            this.playSound(SoundEvents.EVOKER_CAST_SPELL, 2.0F, 0.6F);
            for (int i = 0; i < 3; i++) {
                Vex vex = EntityType.VEX.create(serverLevel);
                if (vex == null) {
                    continue;
                }
                double angle = (Math.PI * 2.0D) * i / 3.0D;
                double x = this.getX() + Math.cos(angle) * 3.0D;
                double z = this.getZ() + Math.sin(angle) * 3.0D;
                vex.moveTo(x, this.getY() + 1.5D, z, this.getRandom().nextFloat() * 360.0F, 0.0F);
                vex.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(vex.blockPosition()),
                        MobSpawnType.MOB_SUMMONED, null, null);
                vex.setOwner(this);
                vex.setBoundOrigin(this.blockPosition());
                vex.setLimitedLife(20 * 20);
                vex.setTarget(target);
                this.markMinion(vex);
                serverLevel.addFreshEntity(vex);
                this.particles(ParticleTypes.PORTAL, x, this.getY() + 1.5D, z, 30, 0.4D, 0.6D, 0.4D, 0.4D);
            }
        }
    }

    /** Picks 8 spots at tick 10, warns, then all of them detonate at tick 40 (2.0 s). One is always under the target. */
    private void tickRain(int t, LivingEntity target) {
        if (t < 36) {
            this.faceTarget(target);
        }
        if (t == 4) {
            this.playSound(SoundEvents.ENDER_DRAGON_GROWL, 2.0F, 0.6F);
        }
        if (t == 10) {
            this.pillars.addAll(this.pickPillars(target, 8, 7.0D));
        }
        if (t >= 10 && t < 40 && t % 2 == 0) {
            for (Vec3 p : this.pillars) {
                this.ring(ParticleTypes.PORTAL, p.x, p.y + 0.1D, p.z, 2.6D, 18);
                this.particles(ParticleTypes.END_ROD, p.x, p.y + 0.5D + (t - 10) * 0.08D, p.z, 2, 0.4D, 0.1D, 0.4D, 0.01D);
            }
        }
        if (t == 40) {
            this.playSound(SoundEvents.GENERIC_EXPLODE, 2.0F, 0.6F);
            this.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 1.5F, 1.4F);
            for (Vec3 p : this.pillars) {
                this.particles(ParticleTypes.DRAGON_BREATH, p.x, p.y + 2.0D, p.z, 90, 0.7D, 2.5D, 0.7D, 0.03D);
                this.particles(ParticleTypes.END_ROD, p.x, p.y + 2.0D, p.z, 40, 0.4D, 2.5D, 0.4D, 0.12D);
                this.particles(ParticleTypes.EXPLOSION, p.x, p.y + 0.5D, p.z, 2, 0.5D, 0.2D, 0.5D, 0.0D);
                this.hitRadius(p, 2.6D, 3.5D, this.dmg(1.2F), 0.2D, 1.0D, e -> slow(e, 40, 0));
            }
        }
    }

    @Override
    protected void onEnrage() {
        this.particles(ParticleTypes.PORTAL, this.getX(), this.getY() + 2.0D, this.getZ(), 200, 1.2D, 2.0D, 1.2D, 1.0D);
        this.particles(ParticleTypes.END_ROD, this.getX(), this.getY() + 2.0D, this.getZ(), 60, 1.0D, 2.0D, 1.0D, 0.15D);
    }

    // ------------------------------------------------------------------ sounds

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENDERMAN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENDERMAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDERMAN_DEATH;
    }
}
