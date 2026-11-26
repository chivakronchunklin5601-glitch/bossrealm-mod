package com.bossrealm.entity;

import java.util.EnumSet;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/** Walks the boss towards its target until it is inside {@link BossBase#preferredRange()}. Pauses during skills. */
public class BossChaseGoal extends Goal {
    private final BossBase boss;
    private final double speed;
    private int repathDelay;

    public BossChaseGoal(BossBase boss, double speed) {
        this.boss = boss;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.boss.getTarget();
        return target != null && target.isAlive() && !this.boss.isUsingSkill();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void start() {
        this.repathDelay = 0;
    }

    @Override
    public void stop() {
        this.boss.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.boss.getTarget();
        if (target == null) {
            return;
        }
        this.boss.getLookControl().setLookAt(target, 40.0F, 40.0F);
        double range = this.boss.preferredRange();
        if (this.boss.distanceToSqr(target) > range * range) {
            if (--this.repathDelay <= 0) {
                this.repathDelay = 4 + this.boss.getRandom().nextInt(7);
                this.boss.getNavigation().moveTo(target, this.speed);
            }
        } else {
            this.boss.getNavigation().stop();
        }
    }
}
