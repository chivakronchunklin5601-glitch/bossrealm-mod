package com.bossrealm.client;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Keyframe animations for all bosses, built with vanilla's animation system (no GeckoLib needed).
 *
 * Keyframes are ADDITIVE on top of the model's rest pose. Rotations are in degrees. Conventions:
 *  - xRot negative  = arm swings forward / up, body xRot positive = leaning forward
 *  - right_arm zRot positive = arm goes outwards, left_arm zRot negative = arm goes outwards
 *  - pos y negative = up, pos z negative = forward
 *
 * The length of every skill clip equals the skill duration in the entity class (seconds * 20 ticks), and the
 * "strike" keyframes line up with the tick where the entity actually deals damage.
 */
public record BossAnimations(AnimationDefinition idle, AnimationDefinition[] skills, float legSwing, float armSwing) {

    public AnimationDefinition skill(int index) {
        return index >= 0 && index < this.skills.length ? this.skills[index] : null;
    }

    // ------------------------------------------------------------------ tiny keyframe DSL

    private static float[] f(float t, float x, float y, float z) {
        return new float[] {t, x, y, z};
    }

    private static float[][] mirror(float[]... frames) {
        float[][] out = new float[frames.length][];
        for (int i = 0; i < frames.length; i++) {
            out[i] = new float[] {frames[i][0], frames[i][1], -frames[i][2], -frames[i][3]};
        }
        return out;
    }

    private static AnimationChannel rot(float[]... fr) {
        Keyframe[] keys = new Keyframe[fr.length];
        for (int i = 0; i < fr.length; i++) {
            keys[i] = new Keyframe(fr[i][0], KeyframeAnimations.degreeVec(fr[i][1], fr[i][2], fr[i][3]),
                    AnimationChannel.Interpolations.LINEAR);
        }
        return new AnimationChannel(AnimationChannel.Targets.ROTATION, keys);
    }

    private static AnimationChannel smooth(float[]... fr) {
        Keyframe[] keys = new Keyframe[fr.length];
        for (int i = 0; i < fr.length; i++) {
            keys[i] = new Keyframe(fr[i][0], KeyframeAnimations.degreeVec(fr[i][1], fr[i][2], fr[i][3]),
                    AnimationChannel.Interpolations.CATMULL_ROM);
        }
        return new AnimationChannel(AnimationChannel.Targets.ROTATION, keys);
    }

    private static AnimationChannel pos(float[]... fr) {
        Keyframe[] keys = new Keyframe[fr.length];
        for (int i = 0; i < fr.length; i++) {
            keys[i] = new Keyframe(fr[i][0], KeyframeAnimations.posVec(fr[i][1], fr[i][2], fr[i][3]),
                    AnimationChannel.Interpolations.LINEAR);
        }
        return new AnimationChannel(AnimationChannel.Targets.POSITION, keys);
    }

    private static AnimationChannel scale(float[]... fr) {
        Keyframe[] keys = new Keyframe[fr.length];
        for (int i = 0; i < fr.length; i++) {
            keys[i] = new Keyframe(fr[i][0], KeyframeAnimations.scaleVec(fr[i][1], fr[i][2], fr[i][3]),
                    AnimationChannel.Interpolations.LINEAR);
        }
        return new AnimationChannel(AnimationChannel.Targets.SCALE, keys);
    }

    // ================================================================== BONE WARDEN (easy)

    public static BossAnimations boneWarden() {
        AnimationDefinition idle = AnimationDefinition.Builder.withLength(4.0F).looping()
                .addAnimation("body", smooth(f(0, 0, 0, 0), f(2, 3, 0, 0), f(4, 0, 0, 0)))
                .addAnimation("head", smooth(f(0, 0, 0, 0), f(1, -2, 4, 0), f(3, 2, -4, 0), f(4, 0, 0, 0)))
                .addAnimation("right_arm", smooth(f(0, 0, 0, 3), f(2, 0, 0, 7), f(4, 0, 0, 3)))
                .addAnimation("left_arm", smooth(f(0, 0, 0, -3), f(2, 0, 0, -7), f(4, 0, 0, -3)))
                .build();

        // 0: SWIPE (1.2 s) - hit frame 0.5 s
        AnimationDefinition swipe = AnimationDefinition.Builder.withLength(1.2F)
                .addAnimation("body", rot(f(0, 0, 0, 0), f(0.35F, -6, -28, 0), f(0.5F, 14, 35, 0), f(0.7F, 10, 30, 0), f(1.2F, 0, 0, 0)))
                .addAnimation("body", pos(f(0, 0, 0, 0), f(0.35F, 0, 0, 2), f(0.5F, 0, 0, -3), f(1.2F, 0, 0, 0)))
                .addAnimation("right_arm", rot(f(0, 0, 0, 0), f(0.35F, -30, -20, 85), f(0.5F, -85, 10, -25), f(0.75F, -60, 0, -10), f(1.2F, 0, 0, 0)))
                .addAnimation("left_arm", rot(f(0, 0, 0, 0), f(0.35F, -20, 0, -15), f(0.5F, 20, 0, -10), f(1.2F, 0, 0, 0)))
                .addAnimation("head", rot(f(0, 0, 0, 0), f(0.5F, 10, -10, 0), f(1.2F, 0, 0, 0)))
                .build();

        // 1: SLAM (2.0 s) - hit frame 1.2 s
        AnimationDefinition slam = AnimationDefinition.Builder.withLength(2.0F)
                .addAnimation("body", rot(f(0, 0, 0, 0), f(0.8F, -20, 0, 0), f(1.2F, 32, 0, 0), f(1.4F, 26, 0, 0), f(2.0F, 0, 0, 0)))
                .addAnimation("body", pos(f(0, 0, 0, 0), f(0.8F, 0, -4, 0), f(1.2F, 0, 3, -4), f(1.4F, 0, 2, -3), f(2.0F, 0, 0, 0)))
                .addAnimation("right_arm", rot(f(0, 0, 0, 0), f(0.8F, -175, 0, 12), f(1.2F, -35, 0, 5), f(1.4F, -30, 0, 5), f(2.0F, 0, 0, 0)))
                .addAnimation("left_arm", rot(f(0, 0, 0, 0), f(0.8F, -175, 0, -12), f(1.2F, -35, 0, -5), f(1.4F, -30, 0, -5), f(2.0F, 0, 0, 0)))
                .addAnimation("head", rot(f(0, 0, 0, 0), f(0.8F, -25, 0, 0), f(1.2F, 30, 0, 0), f(2.0F, 0, 0, 0)))
                .addAnimation("left_leg", rot(f(0, 0, 0, 0), f(1.2F, -10, 0, 0), f(2.0F, 0, 0, 0)))
                .addAnimation("right_leg", rot(f(0, 0, 0, 0), f(1.2F, -10, 0, 0), f(2.0F, 0, 0, 0)))
                .build();

        // 2: VOLLEY (2.0 s) - arrows at 0.7 / 0.9 / 1.1 / 1.3 / 1.5 s
        AnimationDefinition volley = AnimationDefinition.Builder.withLength(2.0F)
                .addAnimation("right_arm", rot(f(0, 0, 0, 0), f(0.3F, -100, 0, 10), f(0.7F, -100, 0, 10), f(0.75F, -118, 0, 10),
                        f(0.85F, -100, 0, 10), f(0.9F, -100, 0, 10), f(0.95F, -118, 0, 10), f(1.05F, -100, 0, 10),
                        f(1.1F, -100, 0, 10), f(1.15F, -118, 0, 10), f(1.25F, -100, 0, 10), f(1.3F, -100, 0, 10),
                        f(1.35F, -118, 0, 10), f(1.45F, -100, 0, 10), f(1.5F, -100, 0, 10), f(1.55F, -118, 0, 10),
                        f(1.65F, -100, 0, 10), f(2.0F, 0, 0, 0)))
                .addAnimation("left_arm", rot(f(0, 0, 0, 0), f(0.3F, -70, 0, -30), f(1.7F, -70, 0, -30), f(2.0F, 0, 0, 0)))
                .addAnimation("body", rot(f(0, 0, 0, 0), f(0.3F, -10, 0, 0), f(1.7F, -10, 0, 0), f(2.0F, 0, 0, 0)))
                .addAnimation("head", rot(f(0, 0, 0, 0), f(0.3F, -8, 0, 0), f(1.7F, -8, 0, 0), f(2.0F, 0, 0, 0)))
                .build();

        // 3: CALL (2.5 s) - summon at 1.5 s
        AnimationDefinition call = AnimationDefinition.Builder.withLength(2.5F)
                .addAnimation("right_arm", rot(f(0, 0, 0, 0), f(0.9F, -150, 0, 35), f(1.5F, -160, 0, 30), f(2.5F, 0, 0, 0)))
                .addAnimation("left_arm", rot(f(0, 0, 0, 0), f(0.9F, -150, 0, -35), f(1.5F, -160, 0, -30), f(2.5F, 0, 0, 0)))
                .addAnimation("head", rot(f(0, 0, 0, 0), f(0.9F, -30, 0, 0), f(1.5F, -35, 0, 0), f(2.5F, 0, 0, 0)))
                .addAnimation("body", rot(f(0, 0, 0, 0), f(0.9F, -15, 0, 0), f(1.5F, -18, 0, 0), f(2.5F, 0, 0, 0)))
                .addAnimation("body", pos(f(0, 0, 0, 0), f(0.9F, 0, -4, 0), f(1.0F, 1.2F, -4, 0), f(1.1F, -1.2F, -4, 0),
                        f(1.2F, 1.2F, -4, 0), f(1.3F, -1.2F, -4, 0), f(1.4F, 1.2F, -4, 0), f(1.5F, 0, -5, 0), f(2.5F, 0, 0, 0)))
                .build();

        return new BossAnimations(idle, new AnimationDefinition[] {swipe, slam, volley, call}, 0.9F, 0.45F);
    }

    // ================================================================== INFERNAL REAVER (medium)

    public static BossAnimations infernalReaver() {
        AnimationDefinition idle = AnimationDefinition.Builder.withLength(3.0F).looping()
                .addAnimation("body", smooth(f(0, 4, 0, 0), f(1.5F, 7, 0, 0), f(3, 4, 0, 0)))
                .addAnimation("head", smooth(f(0, -3, 0, 0), f(1.5F, 0, 3, 0), f(3, -3, 0, 0)))
                .addAnimation("right_arm", smooth(f(0, 0, 0, 5), f(1.5F, -6, 0, 9), f(3, 0, 0, 5)))
                .addAnimation("left_arm", smooth(f(0, 0, 0, -5), f(1.5F, -6, 0, -9), f(3, 0, 0, -5)))
                .addAnimation("mane", smooth(f(0, 0, 0, 0), f(0.5F, 5, 0, 2), f(1.0F, -3, 0, -2), f(1.5F, 6, 0, 1), f(2.0F, -4, 0, -1), f(3, 0, 0, 0)))
                .addAnimation("mane", scale(f(0, 1, 1, 1), f(0.5F, 1.1F, 1.2F, 1), f(1.5F, 0.95F, 1.1F, 1), f(3, 1, 1, 1)))
                .build();

        // 0: COMBO (1.5 s) - hits at 0.5 s and 1.1 s
        AnimationDefinition combo = AnimationDefinition.Builder.withLength(1.5F)
                .addAnimation("body", rot(f(0, 0, 0, 0), f(0.25F, -8, -30, 0), f(0.5F, 18, 40, 0), f(0.7F, 8, 10, 0),
                        f(0.85F, -6, 35, 0), f(1.1F, 18, -45, 0), f(1.5F, 0, 0, 0)))
                .addAnimation("body", pos(f(0, 0, 0, 0), f(0.5F, 0, 0, -4), f(0.7F, 0, 0, 0), f(1.1F, 0, 0, -5), f(1.5F, 0, 0, 0)))
                .addAnimation("right_arm", rot(f(0, 0, 0, 0), f(0.25F, -30, -15, 80), f(0.5F, -90, 15, -20), f(0.7F, -30, 0, 10),
                        f(1.1F, -20, 0, 10), f(1.5F, 0, 0, 0)))
                .addAnimation("left_arm", rot(f(0, 0, 0, 0), f(0.7F, 0, 0, 0), f(0.85F, -30, 15, -80), f(1.1F, -90, -15, 20),
                        f(1.3F, -30, 0, -10), f(1.5F, 0, 0, 0)))
                .addAnimation("left_leg", rot(f(0, 0, 0, 0), f(0.5F, -25, 0, 0), f(0.8F, 0, 0, 0), f(1.5F, 0, 0, 0)))
                .addAnimation("right_leg", rot(f(0, 0, 0, 0), f(0.8F, 0, 0, 0), f(1.1F, -25, 0, 0), f(1.5F, 0, 0, 0)))
                .build();

        // 1: FIREBALLS (1.6 s) - thrown at 0.7 / 1.0 / 1.3 s
        float[][] throwArm = {f(0, 0, 0, 0), f(0.4F, -150, 0, 25), f(0.7F, -60, 0, 10), f(0.85F, -150, 0, 25),
                f(1.0F, -60, 0, 10), f(1.15F, -150, 0, 25), f(1.3F, -60, 0, 10), f(1.6F, 0, 0, 0)};
        AnimationDefinition fireballs = AnimationDefinition.Builder.withLength(1.6F)
                .addAnimation("right_arm", rot(throwArm))
                .addAnimation("left_arm", rot(mirror(throwArm)))
                .addAnimation("body", rot(f(0, 0, 0, 0), f(0.4F, -14, 0, 0), f(0.7F, 16, 0, 0), f(0.85F, -14, 0, 0),
                        f(1.0F, 16, 0, 0), f(1.15F, -14, 0, 0), f(1.3F, 16, 0, 0), f(1.6F, 0, 0, 0)))
                .addAnimation("head", rot(f(0, 0, 0, 0), f(0.4F, -15, 0, 0), f(0.7F, 12, 0, 0), f(1.6F, 0, 0, 0)))
                .build();

        // 2: DASH (2.0 s) - crouch until 0.7 s, charge 0.7 s - 1.5 s
        AnimationDefinition dash = AnimationDefinition.Builder.withLength(2.0F)
                .addAnimation("body", rot(f(0, 0, 0, 0), f(0.6F, 38, 0, 0), f(0.7F, 40, 0, 0), f(0.75F, 62, 0, 0),
                        f(1.45F, 65, 0, 0), f(1.6F, 30, 0, 0), f(2.0F, 0, 0, 0)))
                .addAnimation("body", pos(f(0, 0, 0, 0), f(0.6F, 0, 4, 2), f(0.75F, 0, 6, -6), f(1.45F, 0, 6, -6), f(2.0F, 0, 0, 0)))
                .addAnimation("right_arm", rot(f(0, 0, 0, 0), f(0.6F, 60, 0, 15), f(0.75F, 80, 0, 10), f(1.45F, 80, 0, 10), f(2.0F, 0, 0, 0)))
                .addAnimation("left_arm", rot(f(0, 0, 0, 0), f(0.6F, 60, 0, -15), f(0.75F, 80, 0, -10), f(1.45F, 80, 0, -10), f(2.0F, 0, 0, 0)))
                .addAnimation("head", rot(f(0, 0, 0, 0), f(0.75F, -35, 0, 0), f(1.45F, -35, 0, 0), f(2.0F, 0, 0, 0)))
                .addAnimation("left_leg", rot(f(0, 0, 0, 0), f(0.6F, -40, 0, 0), f(0.75F, 20, 0, 0), f(0.9F, -50, 0, 0),
                        f(1.05F, 40, 0, 0), f(1.2F, -50, 0, 0), f(1.35F, 40, 0, 0), f(1.5F, -20, 0, 0), f(2.0F, 0, 0, 0)))
                .addAnimation("right_leg", rot(f(0, 0, 0, 0), f(0.6F, 40, 0, 0), f(0.75F, -20, 0, 0), f(0.9F, 50, 0, 0),
                        f(1.05F, -40, 0, 0), f(1.2F, 50, 0, 0), f(1.35F, -40, 0, 0), f(1.5F, 20, 0, 0), f(2.0F, 0, 0, 0)))
                .build();

        // 3: NOVA (2.5 s) - slam at 1.5 s
        AnimationDefinition nova = AnimationDefinition.Builder.withLength(2.5F)
                .addAnimation("body", rot(f(0, 0, 0, 0), f(1.0F, -20, 0, 0), f(1.4F, -22, 0, 0), f(1.5F, 36, 0, 0), f(1.7F, 30, 0, 0), f(2.5F, 0, 0, 0)))
                .addAnimation("body", pos(f(0, 0, 0, 0), f(1.0F, 0, -5, 0), f(1.4F, 0, -6, 0), f(1.5F, 0, 4, -3), f(2.5F, 0, 0, 0)))
                .addAnimation("right_arm", rot(f(0, 0, 0, 0), f(1.0F, -170, 0, 20), f(1.4F, -175, 0, 25), f(1.5F, -20, 0, 5), f(1.7F, -15, 0, 5), f(2.5F, 0, 0, 0)))
                .addAnimation("left_arm", rot(f(0, 0, 0, 0), f(1.0F, -170, 0, -20), f(1.4F, -175, 0, -25), f(1.5F, -20, 0, -5), f(1.7F, -15, 0, -5), f(2.5F, 0, 0, 0)))
                .addAnimation("head", rot(f(0, 0, 0, 0), f(1.0F, -30, 0, 0), f(1.4F, -35, 0, 0), f(1.5F, 25, 0, 0), f(2.5F, 0, 0, 0)))
                .addAnimation("left_leg", rot(f(0, 0, 0, 0), f(1.5F, -15, 0, 0), f(2.5F, 0, 0, 0)))
                .addAnimation("right_leg", rot(f(0, 0, 0, 0), f(1.5F, -15, 0, 0), f(2.5F, 0, 0, 0)))
                .build();

        // 4: RAIN (3.0 s) - eruption at 2.0 s
        AnimationDefinition rain = AnimationDefinition.Builder.withLength(3.0F)
                .addAnimation("body", rot(f(0, 0, 0, 0), f(0.8F, -22, 0, 0), f(1.1F, -22, 0, 3), f(1.5F, -24, 0, -3), f(1.9F, -24, 0, 2),
                        f(2.0F, 20, 0, 0), f(2.2F, 14, 0, 0), f(3.0F, 0, 0, 0)))
                .addAnimation("body", pos(f(0, 0, 0, 0), f(0.8F, 0, -6, 0), f(1.9F, 0, -7, 0), f(2.0F, 0, 2, 0), f(3.0F, 0, 0, 0)))
                .addAnimation("right_arm", rot(f(0, 0, 0, 0), f(0.8F, -165, 0, 50), f(1.9F, -170, 0, 55), f(2.0F, -30, 0, 10), f(3.0F, 0, 0, 0)))
                .addAnimation("left_arm", rot(f(0, 0, 0, 0), f(0.8F, -165, 0, -50), f(1.9F, -170, 0, -55), f(2.0F, -30, 0, -10), f(3.0F, 0, 0, 0)))
                .addAnimation("head", rot(f(0, 0, 0, 0), f(0.8F, -35, 0, 0), f(1.9F, -40, 0, 0), f(2.0F, 20, 0, 0), f(3.0F, 0, 0, 0)))
                .build();

        return new BossAnimations(idle, new AnimationDefinition[] {combo, fireballs, dash, nova, rain}, 1.1F, 0.55F);
    }

    // ================================================================== VOID SOVEREIGN (hard)

    public static BossAnimations voidSovereign() {
        AnimationDefinition idle = AnimationDefinition.Builder.withLength(6.0F).looping()
                .addAnimation("body", pos(f(0, 0, 0, 0), f(1.5F, 0, -2, 0), f(3, 0, 0, 0), f(4.5F, 0, -2, 0), f(6, 0, 0, 0)))
                .addAnimation("left_leg", pos(f(0, 0, 0, 0), f(1.5F, 0, -2, 0), f(3, 0, 0, 0), f(4.5F, 0, -2, 0), f(6, 0, 0, 0)))
                .addAnimation("right_leg", pos(f(0, 0, 0, 0), f(1.5F, 0, -2, 0), f(3, 0, 0, 0), f(4.5F, 0, -2, 0), f(6, 0, 0, 0)))
                .addAnimation("halo", rot(f(0, 0, 0, 0), f(3, 0, 0, 180), f(6, 0, 0, 360)))
                .addAnimation("cape", smooth(f(0, 0, 0, 0), f(1.5F, 5, 0, 0), f(3, 0, 0, 0), f(4.5F, 6, 0, 0), f(6, 0, 0, 0)))
                .addAnimation("core", scale(f(0, 1, 1, 1), f(1.5F, 1.25F, 1.25F, 1.25F), f(3, 1, 1, 1), f(4.5F, 1.25F, 1.25F, 1.25F), f(6, 1, 1, 1)))
                .addAnimation("right_arm", smooth(f(0, 0, 0, 5), f(3, -5, 0, 10), f(6, 0, 0, 5)))
                .addAnimation("left_arm", smooth(f(0, 0, 0, -5), f(3, -5, 0, -10), f(6, 0, 0, -5)))
                .addAnimation("shard_left", pos(f(0, 0, 0, 0), f(1.5F, 0, -4, 0), f(3, 0, 0, 0), f(4.5F, 0, 3, 0), f(6, 0, 0, 0)))
                .addAnimation("shard_right", pos(f(0, 0, 0, 0), f(1.5F, 0, 3, 0), f(3, 0, 0, 0), f(4.5F, 0, -4, 0), f(6, 0, 0, 0)))
                .build();

        // 0: SLASH (1.1 s) - hits at 0.4 s and 0.8 s
        AnimationDefinition slash = AnimationDefinition.Builder.withLength(1.1F)
                .addAnimation("body", rot(f(0, 0, 0, 0), f(0.3F, -5, -35, 0), f(0.4F, 12, 45, 0), f(0.55F, 6, 10, 0),
                        f(0.65F, -5, 40, 0), f(0.8F, 12, -50, 0), f(1.1F, 0, 0, 0)))
                .addAnimation("body", pos(f(0, 0, 0, 0), f(0.4F, 0, 0, -5), f(0.55F, 0, 0, 0), f(0.8F, 0, 0, -6), f(1.1F, 0, 0, 0)))
                .addAnimation("right_arm", rot(f(0, 0, 0, 0), f(0.3F, -40, -20, 90), f(0.4F, -90, 15, -30), f(0.55F, -40, 0, 10),
                        f(0.8F, -20, 0, 10), f(1.1F, 0, 0, 0)))
                .addAnimation("left_arm", rot(f(0, 0, 0, 0), f(0.55F, 0, 0, 0), f(0.65F, -40, 20, -90), f(0.8F, -90, -15, 30),
                        f(1.0F, -30, 0, -10), f(1.1F, 0, 0, 0)))
                .addAnimation("cape", rot(f(0, 0, 0, 0), f(0.4F, 18, 0, 0), f(0.8F, 22, 0, 0), f(1.1F, 0, 0, 0)))
                .build();

        // 1: BLINK (1.8 s) - vanishes 0.5 s, reappears 0.7 s, strikes 1.2 s
        float[][] vanish = {f(0, 1, 1, 1), f(0.2F, 1, 1, 1), f(0.5F, 0.15F, 1.3F, 0.15F), f(0.6F, 0.15F, 1.3F, 0.15F),
                f(0.7F, 1.5F, 0.8F, 1.5F), f(0.85F, 1, 1, 1), f(1.8F, 1, 1, 1)};
        AnimationDefinition blink = AnimationDefinition.Builder.withLength(1.8F)
                .addAnimation("body", scale(vanish))
                .addAnimation("left_leg", scale(vanish))
                .addAnimation("right_leg", scale(vanish))
                .addAnimation("right_arm", rot(f(0, 0, 0, 0), f(0.85F, -30, 0, 25), f(1.1F, -110, -20, 30), f(1.2F, -70, 25, -30), f(1.5F, -20, 0, 0), f(1.8F, 0, 0, 0)))
                .addAnimation("left_arm", rot(f(0, 0, 0, 0), f(0.85F, -30, 0, -25), f(1.15F, -120, 20, -30), f(1.3F, -70, -25, 30), f(1.5F, -20, 0, 0), f(1.8F, 0, 0, 0)))
                .addAnimation("body", rot(f(0, 0, 0, 0), f(1.0F, -10, 0, 0), f(1.2F, 22, 30, 0), f(1.5F, 8, 0, 0), f(1.8F, 0, 0, 0)))
                .addAnimation("body", pos(f(0, 0, 0, 0), f(1.0F, 0, 0, 2), f(1.2F, 0, 0, -6), f(1.5F, 0, 0, 0), f(1.8F, 0, 0, 0)))
                .build();

        // 2: BEAM (2.8 s) - beam from 1.2 s to 2.3 s
        AnimationDefinition beam = AnimationDefinition.Builder.withLength(2.8F)
                .addAnimation("right_arm", rot(f(0, 0, 0, 0), f(0.8F, -88, 12, 0), f(2.3F, -88, 12, 0), f(2.8F, 0, 0, 0)))
                .addAnimation("left_arm", rot(f(0, 0, 0, 0), f(0.8F, -88, -12, 0), f(2.3F, -88, -12, 0), f(2.8F, 0, 0, 0)))
                .addAnimation("body", rot(f(0, 0, 0, 0), f(0.8F, -12, 0, 0), f(1.1F, -14, 0, 0), f(2.3F, -14, 0, 0), f(2.8F, 0, 0, 0)))
                .addAnimation("head", rot(f(0, 0, 0, 0), f(0.8F, -10, 0, 0), f(2.3F, -10, 0, 0), f(2.8F, 0, 0, 0)))
                .addAnimation("core", scale(f(0, 1, 1, 1), f(1.1F, 2.4F, 2.4F, 2.4F), f(1.2F, 3.2F, 3.2F, 3.2F), f(2.3F, 3.0F, 3.0F, 3.0F), f(2.5F, 1, 1, 1), f(2.8F, 1, 1, 1)))
                .addAnimation("core", pos(f(0, 0, 0, 0), f(0.8F, 0, 0, -8), f(2.3F, 0, 0, -8), f(2.7F, 0, 0, 0), f(2.8F, 0, 0, 0)))
                .addAnimation("cape", rot(f(0, 0, 0, 0), f(1.2F, 14, 0, 0), f(2.3F, 14, 0, 0), f(2.8F, 0, 0, 0)))
                .build();

        // 3: GRAVITY (2.8 s) - pull from 0.4 s, implosion at 2.2 s
        AnimationDefinition gravity = AnimationDefinition.Builder.withLength(2.8F)
                .addAnimation("right_arm", rot(f(0, 0, 0, 0), f(0.4F, -30, 0, 60), f(1.0F, -45, 0, 85), f(2.1F, -45, 0, 88),
                        f(2.2F, -95, 0, 5), f(2.5F, -60, 0, 20), f(2.8F, 0, 0, 0)))
                .addAnimation("left_arm", rot(f(0, 0, 0, 0), f(0.4F, -30, 0, -60), f(1.0F, -45, 0, -85), f(2.1F, -45, 0, -88),
                        f(2.2F, -95, 0, -5), f(2.5F, -60, 0, -20), f(2.8F, 0, 0, 0)))
                .addAnimation("body", pos(f(0, 0, 0, 0), f(0.4F, 0, -2, 0), f(1.0F, 0, -7, 0), f(2.1F, 0, -8, 0), f(2.2F, 0, 2, 0), f(2.8F, 0, 0, 0)))
                .addAnimation("body", rot(f(0, 0, 0, 0), f(1.0F, -12, 0, 0), f(1.3F, -12, 0, 2), f(1.6F, -13, 0, -2), f(1.9F, -13, 0, 2),
                        f(2.1F, -14, 0, 0), f(2.2F, 24, 0, 0), f(2.5F, 10, 0, 0), f(2.8F, 0, 0, 0)))
                .addAnimation("head", rot(f(0, 0, 0, 0), f(1.0F, -25, 0, 0), f(2.1F, -30, 0, 0), f(2.2F, 20, 0, 0), f(2.8F, 0, 0, 0)))
                .addAnimation("core", scale(f(0, 1, 1, 1), f(1.0F, 2, 2, 2), f(2.1F, 2.8F, 2.8F, 2.8F), f(2.2F, 0.5F, 0.5F, 0.5F), f(2.5F, 1.2F, 1.2F, 1.2F), f(2.8F, 1, 1, 1)))
                .addAnimation("cape", rot(f(0, 0, 0, 0), f(1.0F, 20, 0, 0), f(2.1F, 24, 0, 0), f(2.2F, -8, 0, 0), f(2.8F, 0, 0, 0)))
                .build();

        // 4: SUMMON (2.5 s) - vexes appear at 1.5 s
        AnimationDefinition summon = AnimationDefinition.Builder.withLength(2.5F)
                .addAnimation("right_arm", rot(f(0, 0, 0, 0), f(0.8F, -140, 0, 30), f(1.5F, -155, 0, 35), f(2.5F, 0, 0, 0)))
                .addAnimation("left_arm", rot(f(0, 0, 0, 0), f(0.8F, -140, 0, -30), f(1.5F, -155, 0, -35), f(2.5F, 0, 0, 0)))
                .addAnimation("body", pos(f(0, 0, 0, 0), f(0.8F, 0, -5, 0), f(1.5F, 0, -6, 0), f(2.5F, 0, 0, 0)))
                .addAnimation("head", rot(f(0, 0, 0, 0), f(0.8F, -30, 0, 0), f(1.5F, -35, 0, 0), f(2.5F, 0, 0, 0)))
                .addAnimation("core", scale(f(0, 1, 1, 1), f(0.8F, 1.6F, 1.6F, 1.6F), f(1.5F, 2.4F, 2.4F, 2.4F), f(1.7F, 0.8F, 0.8F, 0.8F), f(2.5F, 1, 1, 1)))
                .build();

        // 5: RAIN (3.0 s) - detonation at 2.0 s
        AnimationDefinition rain = AnimationDefinition.Builder.withLength(3.0F)
                .addAnimation("right_arm", rot(f(0, 0, 0, 0), f(0.8F, -170, 0, 40), f(1.9F, -175, 0, 45), f(2.0F, -25, 0, 8), f(3.0F, 0, 0, 0)))
                .addAnimation("left_arm", rot(f(0, 0, 0, 0), f(0.8F, -170, 0, -40), f(1.9F, -175, 0, -45), f(2.0F, -25, 0, -8), f(3.0F, 0, 0, 0)))
                .addAnimation("body", pos(f(0, 0, 0, 0), f(0.8F, 0, -9, 0), f(1.9F, 0, -10, 0), f(2.0F, 0, 3, 0), f(3.0F, 0, 0, 0)))
                .addAnimation("body", rot(f(0, 0, 0, 0), f(0.8F, -18, 0, 0), f(1.9F, -20, 0, 0), f(2.0F, 25, 0, 0), f(2.3F, 15, 0, 0), f(3.0F, 0, 0, 0)))
                .addAnimation("head", rot(f(0, 0, 0, 0), f(0.8F, -30, 0, 0), f(1.9F, -36, 0, 0), f(2.0F, 22, 0, 0), f(3.0F, 0, 0, 0)))
                .addAnimation("core", scale(f(0, 1, 1, 1), f(0.8F, 2, 2, 2), f(1.9F, 3.4F, 3.4F, 3.4F), f(2.0F, 0.6F, 0.6F, 0.6F), f(2.4F, 1.2F, 1.2F, 1.2F), f(3.0F, 1, 1, 1)))
                .addAnimation("cape", rot(f(0, 0, 0, 0), f(1.9F, 22, 0, 0), f(2.0F, -10, 0, 0), f(3.0F, 0, 0, 0)))
                .build();

        return new BossAnimations(idle, new AnimationDefinition[] {slash, blink, beam, gravity, summon, rain}, 0.35F, 0.3F);
    }
}
