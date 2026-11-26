package com.bossrealm.client;

import com.bossrealm.entity.BossBase;

import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * One model class for every boss. All bosses share the bone names body / head / left_arm / right_arm / left_leg /
 * right_leg (extra bones such as halo or mane are only touched by the animation clips).
 */
public class BossModel<T extends BossBase> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final BossAnimations animations;

    public BossModel(ModelPart root, BossAnimations animations) {
        this.root = root;
        this.animations = animations;
        ModelPart body = root.getChild("body");
        this.head = body.getChild("head");
        this.leftArm = body.getChild("left_arm");
        this.rightArm = body.getChild("right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        // Keyframes are additive, so every frame starts from the rest pose.
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.head.yRot = Mth.clamp(netHeadYaw, -50.0F, 50.0F) * Mth.DEG_TO_RAD;
        this.head.xRot = Mth.clamp(headPitch, -35.0F, 35.0F) * Mth.DEG_TO_RAD;

        float swing = limbSwing * 0.5F;
        this.leftLeg.xRot = Mth.cos(swing) * this.animations.legSwing() * limbSwingAmount;
        this.rightLeg.xRot = Mth.cos(swing + Mth.PI) * this.animations.legSwing() * limbSwingAmount;
        this.leftArm.xRot = Mth.cos(swing + Mth.PI) * this.animations.armSwing() * limbSwingAmount;
        this.rightArm.xRot = Mth.cos(swing) * this.animations.armSwing() * limbSwingAmount;

        this.animate(entity.idleState, this.animations.idle(), ageInTicks);
        for (int i = 0; i < entity.skillStates.length; i++) {
            AnimationDefinition definition = this.animations.skill(i);
            if (definition != null) {
                this.animate(entity.skillStates[i], definition, ageInTicks);
            }
        }
    }
}
