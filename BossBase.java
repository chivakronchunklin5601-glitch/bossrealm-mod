package com.bossrealm.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Shared brain of every boss in this mod.
 *
 * A boss is a tiny state machine: idle -> (cooldown) -> pick a skill -> run the skill tick by tick -> cooldown.
 * The active skill id is synced to the client, where it starts the matching keyframe animation.
 * Subclasses only describe their skills (choose / duration / cooldown / per-tick behaviour).
 */
public abstract class BossBase extends Monster {

    /** Hard design limits requested for this mod. Diamond armor = 20 armor / 8 toughness. */
    public static final double MAX_BOSS_HEALTH = 500.0D;
    public static final double MAX_BOSS_ARMOR = 20.0D;
    public static final double MAX_BOSS_TOUGHNESS = 8.0D;

    public static final int MAX_SKILLS = 6;
    public static final String MINION_TAG = "bossrealm_minion";

    private static final EntityDataAccessor<Integer> DATA_SKILL =
            SynchedEntityData.defineId(BossBase.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_ENRAGED =
            SynchedEntityData.defineId(BossBase.class, EntityDataSerializers.BOOLEAN);

    private static final UUID ENRAGE_SPEED_ID = UUID.fromString("7f1d3b52-5c64-4e0a-9d1e-0a6b5a2c9e01");
    private static final UUID ENRAGE_DAMAGE_ID = UUID.fromString("7f1d3b52-5c64-4e0a-9d1e-0a6b5a2c9e02");

    /** Client-side animation states. skillStates[i] plays while skill id (i + 1) is active. */
    public final AnimationState idleState = new AnimationState();
    public final AnimationState[] skillStates = new AnimationState[MAX_SKILLS];

    private final ServerBossEvent bossEvent;
    private int skill;
    private int skillTick;
    private int skillCooldown = 60;
    private int forcedSkill;

    protected BossBase(EntityType<? extends BossBase> type, Level level, BossEvent.BossBarColor color, int xp) {
        super(type, level);
        this.xpReward = xp;
        this.bossEvent = new ServerBossEvent(this.getDisplayName(), color, BossEvent.BossBarOverlay.NOTCHED_10);
        for (int i = 0; i < MAX_SKILLS; i++) {
            this.skillStates[i] = new AnimationState();
        }
        this.setPersistenceRequired();
    }

    // ------------------------------------------------------------------ attributes

    /** Builds boss attributes and clamps them to the design limits (500 HP, diamond-level armor). */
    public static AttributeSupplier.Builder bossAttributes(double health, double armor, double toughness,
                                                           double damage, double speed, double knockbackResistance) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, Math.min(health, MAX_BOSS_HEALTH))
                .add(Attributes.ARMOR, Math.min(armor, MAX_BOSS_ARMOR))
                .add(Attributes.ARMOR_TOUGHNESS, Math.min(toughness, MAX_BOSS_TOUGHNESS))
                .add(Attributes.ATTACK_DAMAGE, damage)
                .add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.KNOCKBACK_RESISTANCE, knockbackResistance)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    // ------------------------------------------------------------------ skill API for subclasses

    /** Return a skill id (1..MAX_SKILLS) to start now, or 0 to wait. Called every tick while off cooldown. */
    protected abstract int chooseSkill(LivingEntity target, double distanceSqr);

    /** Length of the skill in ticks. Keep it equal to the animation length (seconds * 20). */
    protected abstract int skillDuration(int skill);

    /** Ticks to wait after the skill ends. */
    protected abstract int skillCooldown(int skill);

    /** Called every server tick while a skill runs. {@code tick} counts 1..duration. */
    protected abstract void onSkillTick(int skill, int tick, LivingEntity target);

    protected void onSkillStart(int skill, LivingEntity target) {
    }

    protected void onSkillEnd(int skill) {
    }

    /** Skill that is forced as soon as the boss enters its second phase (0 = none). */
    protected int enrageSkill() {
        return 0;
    }

    protected void onEnrage() {
    }

    /** Distance at which the boss stops walking towards its target. */
    public double preferredRange() {
        return 3.0D;
    }

    // ------------------------------------------------------------------ state

    public int getSkill() {
        return this.entityData.get(DATA_SKILL);
    }

    public boolean isUsingSkill() {
        return this.getSkill() != 0;
    }

    public boolean isEnraged() {
        return this.entityData.get(DATA_ENRAGED);
    }

    private void setEnraged(boolean enraged) {
        this.entityData.set(DATA_ENRAGED, enraged);
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance damage = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (speed == null || damage == null) {
            return;
        }
        speed.removeModifier(ENRAGE_SPEED_ID);
        damage.removeModifier(ENRAGE_DAMAGE_ID);
        if (enraged) {
            speed.addTransientModifier(new AttributeModifier(ENRAGE_SPEED_ID, "Boss enrage speed", 0.2D,
                    AttributeModifier.Operation.MULTIPLY_BASE));
            damage.addTransientModifier(new AttributeModifier(ENRAGE_DAMAGE_ID, "Boss enrage damage", 0.25D,
                    AttributeModifier.Operation.MULTIPLY_BASE));
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SKILL, 0);
        this.entityData.define(DATA_ENRAGED, false);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (DATA_SKILL.equals(key) && this.level().isClientSide) {
            for (AnimationState state : this.skillStates) {
                state.stop();
            }
            int id = this.getSkill();
            if (id > 0 && id <= MAX_SKILLS) {
                this.skillStates[id - 1].start(this.tickCount);
            }
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            this.idleState.startIfStopped(this.tickCount);
        }
    }

    // ------------------------------------------------------------------ AI

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new BossChaseGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 24.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        if (!this.isEnraged() && this.getHealth() <= this.getMaxHealth() * 0.5F) {
            this.enrage();
        }
        this.tickSkills();

        if (this.getTarget() == null && this.tickCount % 40 == 0 && this.getHealth() < this.getMaxHealth()) {
            this.heal(4.0F);
        }
    }

    private void enrage() {
        this.setEnraged(true);
        this.playSound(SoundEvents.ENDER_DRAGON_GROWL, 2.0F, 0.7F);
        this.particles(ParticleTypes.POOF, this.getX(), this.getY() + 1.5D, this.getZ(), 40, 1.2D, 1.2D, 1.2D, 0.1D);
        this.onEnrage();
        this.forcedSkill = this.enrageSkill();
        this.skillCooldown = Math.min(this.skillCooldown, 10);
    }

    private void tickSkills() {
        LivingEntity target = this.getTarget();

        if (this.skill != 0) {
            if (target == null || !target.isAlive()) {
                this.finishSkill();
                return;
            }
            this.skillTick++;
            this.onSkillTick(this.skill, this.skillTick, target);
            if (this.skillTick >= this.skillDuration(this.skill)) {
                this.finishSkill();
            }
            return;
        }

        if (this.skillCooldown > 0) {
            this.skillCooldown--;
            return;
        }
        if (target == null || !target.isAlive()) {
            return;
        }

        int next = this.forcedSkill;
        if (next == 0) {
            next = this.chooseSkill(target, this.distanceToSqr(target));
        }
        if (next > 0 && next <= MAX_SKILLS) {
            this.forcedSkill = 0;
            this.beginSkill(next, target);
        }
    }

    private void beginSkill(int id, LivingEntity target) {
        this.skill = id;
        this.skillTick = 0;
        this.getNavigation().stop();
        this.entityData.set(DATA_SKILL, id);
        this.onSkillStart(id, target);
    }

    private void finishSkill() {
        int id = this.skill;
        this.onSkillEnd(id);
        float multiplier = this.isEnraged() ? 0.7F : 1.0F;
        this.skillCooldown = Math.max(8, Math.round(this.skillCooldown(id) * multiplier));
        this.skill = 0;
        this.skillTick = 0;
        this.entityData.set(DATA_SKILL, 0);
    }

    // ------------------------------------------------------------------ helpers for skills

    protected Vec3 forward() {
        float yaw = this.getYRot() * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(yaw), 0.0D, Mth.cos(yaw));
    }

    protected void faceYaw(float yaw) {
        this.setYRot(yaw);
        this.setYHeadRot(yaw);
        this.setYBodyRot(yaw);
    }

    protected void faceTarget(Entity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        this.faceYaw((float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F);
    }

    /** Attack damage attribute multiplied by a per-skill factor. */
    protected float dmg(float multiplier) {
        return (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * multiplier;
    }

    protected boolean canHarm(Entity entity) {
        if (!(entity instanceof LivingEntity living) || entity == this || !living.isAlive()) {
            return false;
        }
        if (entity instanceof BossBase || entity.getTags().contains(MINION_TAG)) {
            return false;
        }
        if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }
        return !this.isAlliedTo(entity);
    }

    protected boolean damageEntity(LivingEntity victim, float amount) {
        return victim.hurt(this.damageSources().mobAttack(this), amount);
    }

    protected void launch(LivingEntity victim, Vec3 from, double horizontal, double vertical) {
        Vec3 dir = new Vec3(victim.getX() - from.x, 0.0D, victim.getZ() - from.z);
        dir = dir.lengthSqr() < 1.0E-4D ? this.forward() : dir.normalize();
        double resistance = 1.0D - victim.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        victim.setDeltaMovement(victim.getDeltaMovement().add(
                dir.x * horizontal * resistance, vertical, dir.z * horizontal * resistance));
        victim.hurtMarked = true;
    }

    /** Damages everything inside a cylinder. Returns the number of victims that actually took damage. */
    protected int hitRadius(Vec3 center, double radius, double height, float amount, double push, double lift,
                            Consumer<LivingEntity> extra) {
        AABB box = new AABB(center.x - radius, center.y - 1.0D, center.z - radius,
                center.x + radius, center.y + height, center.z + radius);
        int hits = 0;
        for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, box, this::canHarm)) {
            double dx = victim.getX() - center.x;
            double dz = victim.getZ() - center.z;
            if (dx * dx + dz * dz > radius * radius) {
                continue;
            }
            if (this.damageEntity(victim, amount)) {
                this.launch(victim, center, push, lift);
                if (extra != null) {
                    extra.accept(victim);
                }
                hits++;
            }
        }
        return hits;
    }

    /** Damages everything in a horizontal cone in front of the boss (melee swings). */
    protected int hitCone(double range, double halfAngleDegrees, float amount, double push, double lift,
                          Consumer<LivingEntity> extra) {
        Vec3 fwd = this.forward();
        double minDot = Math.cos(Math.toRadians(halfAngleDegrees));
        AABB box = this.getBoundingBox().inflate(range, 1.5D, range);
        int hits = 0;
        for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, box, this::canHarm)) {
            Vec3 to = new Vec3(victim.getX() - this.getX(), 0.0D, victim.getZ() - this.getZ());
            double dist = to.length();
            if (dist - victim.getBbWidth() * 0.5D > range) {
                continue;
            }
            if (dist > 1.0E-3D && fwd.dot(to.normalize()) < minDot) {
                continue;
            }
            if (this.damageEntity(victim, amount)) {
                this.launch(victim, this.position(), push, lift);
                if (extra != null) {
                    extra.accept(victim);
                }
                hits++;
            }
        }
        return hits;
    }

    protected List<Vec3> pickPillars(LivingEntity target, int count, double spread) {
        List<Vec3> list = new ArrayList<>();
        list.add(target.position());
        for (int i = 1; i < count; i++) {
            double angle = this.getRandom().nextDouble() * Math.PI * 2.0D;
            double radius = 2.0D + this.getRandom().nextDouble() * spread;
            list.add(new Vec3(target.getX() + Math.cos(angle) * radius, target.getY(),
                    target.getZ() + Math.sin(angle) * radius));
        }
        return list;
    }

    protected void particles(ParticleOptions type, double x, double y, double z, int count,
                             double dx, double dy, double dz, double speed) {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(type, x, y, z, count, dx, dy, dz, speed);
        }
    }

    protected void ring(ParticleOptions type, double cx, double cy, double cz, double radius, int points) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2.0D) * i / points;
            serverLevel.sendParticles(type, cx + Math.cos(angle) * radius, cy, cz + Math.sin(angle) * radius,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    protected int countMinions() {
        return this.level().getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(40.0D),
                mob -> mob.isAlive() && mob.getTags().contains(MINION_TAG)).size();
    }

    protected void markMinion(Mob mob) {
        mob.addTag(MINION_TAG);
    }

    private void discardMinions() {
        for (Mob mob : this.level().getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(64.0D),
                m -> m.getTags().contains(MINION_TAG))) {
            mob.kill();
        }
    }

    // ------------------------------------------------------------------ boss bar, persistence, misc

    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Enraged", this.isEnraged());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.getBoolean("Enraged")) {
            this.setEnraged(true);
        }
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // No single hit may take more than 20% of max health (except /kill and void damage).
        if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            amount = Math.min(amount, this.getMaxHealth() * 0.2F);
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide) {
            this.discardMinions();
            this.playSound(SoundEvents.GENERIC_EXPLODE, 2.0F, 0.6F);
            this.particles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 1.5D, this.getZ(),
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 1.0F, 0.7F);
    }
}
