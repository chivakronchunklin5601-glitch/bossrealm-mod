package com.bossrealm.entity;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * EASY boss - a giant skeleton. Slow, heavily telegraphed attacks and long pauses between them.
 *
 * Stats : 200 HP, armor 4 (about leather/chain level), no toughness.
 * Skills: Swipe (melee) / Ground Slam (shockwave) / Bone Volley (arrows) / Bone Call (summons skeletons, phase 2).
 */
public class BoneWarden extends BossBase {
    public static final int SWIPE = 1;
    public static final int SLAM = 2;
    public static final int VOLLEY = 3;
    public static final int CALL = 4;

    public BoneWarden(EntityType<? extends BoneWarden> type, Level level) {
        super(type, level, BossEvent.BossBarColor.WHITE, 120);
    }

    public static AttributeSupplier.Builder createAttributes() {
        // health, armor, toughness, attack damage, speed, knockback resistance
        return bossAttributes(200.0D, 4.0D, 0.0D, 9.0D, 0.25D, 0.6D);
    }

    @Override
    public double preferredRange() {
        return 2.6D;
    }

    // ------------------------------------------------------------------ skill selection

    @Override
    protected int chooseSkill(LivingEntity target, double distanceSqr) {
        double d = Math.sqrt(distanceSqr);
        int roll = this.getRandom().nextInt(100);

        if (d <= 3.6D) {
            return roll < 60 ? SWIPE : SLAM;
        }
        if (d <= 5.5D) {
            return roll < 35 ? SLAM : 0;
        }
        if (this.hasLineOfSight(target)) {
            if (this.isEnraged() && roll < 25 && this.countMinions() < 3) {
                return CALL;
            }
            if (roll < 55) {
                return VOLLEY;
            }
        }
        return 0;
    }

    @Override
    protected int skillDuration(int skill) {
        return switch (skill) {
            case SWIPE -> 24;
            case SLAM -> 40;
            case VOLLEY -> 40;
            case CALL -> 50;
            default -> 20;
        };
    }

    @Override
    protected int skillCooldown(int skill) {
        return switch (skill) {
            case SWIPE -> 30;
            case SLAM -> 50;
            case VOLLEY -> 60;
            case CALL -> 80;
            default -> 40;
        };
    }

    @Override
    protected int enrageSkill() {
        return CALL;
    }

    // ------------------------------------------------------------------ skills

    @Override
    protected void onSkillTick(int skill, int t, LivingEntity target) {
        switch (skill) {
            case SWIPE -> tickSwipe(t, target);
            case SLAM -> tickSlam(t, target);
            case VOLLEY -> tickVolley(t, target);
            case CALL -> tickCall(t, target);
            default -> {
            }
        }
    }

    /** Animation hit frame: tick 10 (0.5 s). */
    private void tickSwipe(int t, LivingEntity target) {
        if (t < 10) {
            this.faceTarget(target);
        }
        if (t == 4) {
            this.playSound(SoundEvents.SKELETON_AMBIENT, 1.2F, 0.5F);
        }
        if (t == 10) {
            this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.5F, 0.6F);
            Vec3 f = this.forward();
            this.particles(ParticleTypes.SWEEP_ATTACK, this.getX() + f.x * 2.2D, this.getY() + 1.6D,
                    this.getZ() + f.z * 2.2D, 3, 0.8D, 0.3D, 0.8D, 0.0D);
            this.hitCone(3.9D, 70.0D, this.dmg(1.0F), 0.9D, 0.35D, null);
        }
    }

    /** Animation hit frame: tick 24 (1.2 s). The shockwave ring expands for the next 7 ticks. */
    private void tickSlam(int t, LivingEntity target) {
        if (t < 20) {
            this.faceTarget(target);
        }
        if (t == 8) {
            this.playSound(SoundEvents.SKELETON_AMBIENT, 1.5F, 0.4F);
        }
        if (t < 24 && t % 4 == 0) {
            this.particles(ParticleTypes.CRIT, this.getX(), this.getY() + 3.2D, this.getZ(), 6, 0.8D, 0.3D, 0.8D, 0.1D);
        }
        if (t == 24) {
            this.playSound(SoundEvents.GENERIC_EXPLODE, 1.6F, 0.7F);
            BlockState ground = this.level().getBlockState(this.blockPosition().below());
            if (ground.isAir()) {
                ground = Blocks.STONE.defaultBlockState();
            }
            this.particles(new BlockParticleOption(ParticleTypes.BLOCK, ground), this.getX(), this.getY() + 0.1D,
                    this.getZ(), 70, 2.5D, 0.2D, 2.5D, 0.15D);
            this.hitRadius(this.position(), 5.5D, 3.0D, this.dmg(1.3F), 1.2D, 0.75D, null);
        }
        if (t > 24 && t <= 31) {
            this.ring(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.15D, this.getZ(), (t - 24) * 0.85D, 28);
        }
    }

    /** Fires 5 arrows in a fan at ticks 14, 18, 22, 26, 30 (recoil frames of the animation). */
    private void tickVolley(int t, LivingEntity target) {
        if (t < 32) {
            this.faceTarget(target);
        }
        if (t >= 14 && t <= 30 && (t - 14) % 4 == 0) {
            this.shootArrow(target, (t - 14) / 4);
        }
    }

    private void shootArrow(LivingEntity target, int index) {
        Vec3 origin = new Vec3(this.getX(), this.getEyeY() - 0.2D, this.getZ()).add(this.forward().scale(1.0D));
        Vec3 dir = target.getEyePosition().subtract(origin).normalize();

        double angle = Math.toRadians((index - 2) * 9.0D);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double dx = dir.x * cos - dir.z * sin;
        double dz = dir.x * sin + dir.z * cos;

        Arrow arrow = new Arrow(this.level(), this);
        arrow.setPos(origin.x, origin.y, origin.z);
        arrow.shoot(dx, dir.y + 0.04D, dz, 1.35F, 1.5F);
        arrow.setBaseDamage(2.2D);
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        this.level().addFreshEntity(arrow);
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.4F, 0.7F + index * 0.05F);
    }

    /** Raises both arms, summons 2 skeletons at tick 30 (1.5 s). */
    private void tickCall(int t, LivingEntity target) {
        if (t < 30) {
            this.faceTarget(target);
        }
        if (t == 6) {
            this.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 1.5F, 0.7F);
        }
        if (t < 30 && t % 5 == 0) {
            this.particles(ParticleTypes.SOUL, this.getX(), this.getY() + 0.5D, this.getZ(), 6, 1.2D, 0.3D, 1.2D, 0.05D);
        }
        if (t == 30) {
            this.callSkeletons(target);
        }
    }

    private void callSkeletons(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        this.playSound(SoundEvents.EVOKER_CAST_SPELL, 1.5F, 0.6F);
        for (int i = 0; i < 2; i++) {
            Skeleton skeleton = EntityType.SKELETON.create(serverLevel);
            if (skeleton == null) {
                continue;
            }
            double angle = this.getRandom().nextDouble() * Math.PI * 2.0D;
            double x = this.getX() + Math.cos(angle) * 3.0D;
            double z = this.getZ() + Math.sin(angle) * 3.0D;
            skeleton.moveTo(x, this.getY(), z, this.getRandom().nextFloat() * 360.0F, 0.0F);
            skeleton.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(skeleton.blockPosition()),
                    MobSpawnType.MOB_SUMMONED, null, null);
            skeleton.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            skeleton.setDropChance(EquipmentSlot.HEAD, 0.0F);
            skeleton.setTarget(target);
            this.markMinion(skeleton);
            serverLevel.addFreshEntity(skeleton);
            this.particles(ParticleTypes.SOUL_FIRE_FLAME, x, this.getY() + 0.5D, z, 20, 0.4D, 0.6D, 0.4D, 0.05D);
        }
    }

    @Override
    protected void onEnrage() {
        this.particles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 1.5D, this.getZ(),
                60, 1.0D, 1.5D, 1.0D, 0.08D);
    }

    // ------------------------------------------------------------------ sounds

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WITHER_SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_SKELETON_DEATH;
    }
}
