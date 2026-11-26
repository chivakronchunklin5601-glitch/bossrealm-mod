package com.bossrealm.client;

import com.bossrealm.BossRealm;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * Model geometry of the three bosses (feet at y = 24, model space like every vanilla entity model).
 * Generated together with the textures so that every cube's UV box matches its painted texture area.
 */
public final class BossModelLayers {
    public static final ModelLayerLocation BONE_WARDEN =
            new ModelLayerLocation(new ResourceLocation(BossRealm.MODID, "bone_warden"), "main");
    public static final ModelLayerLocation INFERNAL_REAVER =
            new ModelLayerLocation(new ResourceLocation(BossRealm.MODID, "infernal_reaver"), "main");
    public static final ModelLayerLocation VOID_SOVEREIGN =
            new ModelLayerLocation(new ResourceLocation(BossRealm.MODID, "void_sovereign"), "main");

    public static LayerDefinition boneWarden() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(32, 30).addBox(-4F, 0F, -4F, 8F, 20F, 8F)
                .texOffs(84, 75).addBox(-5F, 20F, -6F, 10F, 2F, 12F)
                .texOffs(10, 102).addBox(-1F, 6F, -7F, 2F, 3F, 3F), PartPose.offset(6F, 2F, 0F));
        PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 30).addBox(-4F, 0F, -4F, 8F, 20F, 8F)
                .texOffs(40, 75).addBox(-5F, 20F, -6F, 10F, 2F, 12F)
                .texOffs(0, 102).addBox(-1F, 6F, -7F, 2F, 3F, 3F), PartPose.offset(-6F, 2F, 0F));
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 90).addBox(-7F, -4F, -4F, 14F, 4F, 8F)
                .texOffs(0, 0).addBox(-9F, -24F, -5F, 18F, 20F, 10F)
                .texOffs(64, 30).addBox(-3F, -24F, 4F, 6F, 22F, 3F)
                .texOffs(68, 90).addBox(-6F, -3F, -5F, 12F, 10F, 1F)
                .texOffs(94, 90).addBox(-3F, -17F, -7F, 6F, 6F, 3F), PartPose.offset(0F, 2F, 0F));
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(82, 30).addBox(-5F, -10F, -5F, 10F, 10F, 10F), PartPose.offset(0F, -24F, 0F));
        PartDefinition horn_left = head.addOrReplaceChild("horn_left", CubeListBuilder.create()
                .texOffs(56, 90).addBox(-1.5F, -9F, -1.5F, 3F, 9F, 3F), PartPose.offsetAndRotation(4F, -8F, 0F, 0.0000F, 0.0000F, 0.4363F));
        PartDefinition horn_right = head.addOrReplaceChild("horn_right", CubeListBuilder.create()
                .texOffs(44, 90).addBox(-1.5F, -9F, -1.5F, 3F, 9F, 3F), PartPose.offsetAndRotation(-4F, -8F, 0F, 0.0000F, 0.0000F, -0.4363F));
        PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(0, 75).addBox(-5F, -4F, -5F, 10F, 5F, 10F)
                .texOffs(120, 90).addBox(-1F, -10F, -1F, 2F, 6F, 2F)
                .texOffs(88, 0).addBox(-4F, 0F, -4F, 8F, 20F, 8F)
                .texOffs(40, 58).addBox(-5F, 19F, -5F, 10F, 7F, 10F), PartPose.offset(13F, -20F, 0F));
        PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(80, 58).addBox(-5F, -4F, -5F, 10F, 5F, 10F)
                .texOffs(112, 90).addBox(-1F, -10F, -1F, 2F, 6F, 2F)
                .texOffs(56, 0).addBox(-4F, 0F, -4F, 8F, 20F, 8F)
                .texOffs(0, 58).addBox(-5F, 19F, -5F, 10F, 7F, 10F), PartPose.offset(-13F, -20F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    public static LayerDefinition infernalReaver() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(64, 32).addBox(-4F, 0F, -4F, 8F, 12F, 8F)
                .texOffs(104, 67).addBox(-3F, 12F, -3F, 6F, 6F, 6F)
                .texOffs(162, 67).addBox(-4F, 18F, -5F, 8F, 2F, 9F)
                .texOffs(122, 52).addBox(-5F, 1F, -5F, 10F, 4F, 10F), PartPose.offset(6F, 4F, 0F));
        PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(32, 32).addBox(-4F, 0F, -4F, 8F, 12F, 8F)
                .texOffs(80, 67).addBox(-3F, 12F, -3F, 6F, 6F, 6F)
                .texOffs(128, 67).addBox(-4F, 18F, -5F, 8F, 2F, 9F)
                .texOffs(82, 52).addBox(-5F, 1F, -5F, 10F, 4F, 10F), PartPose.offset(-6F, 4F, 0F));
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(32, 67).addBox(-8F, -4F, -4F, 16F, 4F, 8F)
                .texOffs(0, 0).addBox(-10F, -26F, -5F, 20F, 22F, 10F)
                .texOffs(162, 52).addBox(-8F, -25F, -6F, 16F, 12F, 2F)
                .texOffs(0, 52).addBox(-9F, -5F, -6F, 18F, 3F, 12F)
                .texOffs(20, 80).addBox(-1F, -24F, 5F, 2F, 5F, 3F)
                .texOffs(10, 80).addBox(-1F, -17F, 5F, 2F, 5F, 3F)
                .texOffs(0, 80).addBox(-1F, -10F, 5F, 2F, 5F, 3F)
                .texOffs(196, 67).addBox(-6F, -3F, -6F, 12F, 9F, 2F), PartPose.offset(0F, 4F, 0F));
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(156, 0).addBox(-5F, -10F, -5F, 10F, 10F, 10F)
                .texOffs(74, 80).addBox(-4F, -3F, -6F, 8F, 3F, 1F), PartPose.offset(0F, -26F, 0F));
        PartDefinition horn_left = head.addOrReplaceChild("horn_left", CubeListBuilder.create()
                .texOffs(208, 32).addBox(-2F, -12F, -2F, 4F, 12F, 4F), PartPose.offsetAndRotation(4F, -9F, -1F, 0.0000F, 0.0000F, 0.5236F));
        PartDefinition horn_tip_left = horn_left.addOrReplaceChild("horn_tip_left", CubeListBuilder.create()
                .texOffs(38, 80).addBox(-1F, -6F, -1F, 2F, 6F, 2F), PartPose.offsetAndRotation(0F, -12F, 0F, 0.0000F, 0.0000F, 0.3840F));
        PartDefinition horn_right = head.addOrReplaceChild("horn_right", CubeListBuilder.create()
                .texOffs(192, 32).addBox(-2F, -12F, -2F, 4F, 12F, 4F), PartPose.offsetAndRotation(-4F, -9F, -1F, 0.0000F, 0.0000F, -0.5236F));
        PartDefinition horn_tip_right = horn_right.addOrReplaceChild("horn_tip_right", CubeListBuilder.create()
                .texOffs(30, 80).addBox(-1F, -6F, -1F, 2F, 6F, 2F), PartPose.offsetAndRotation(0F, -12F, 0F, 0.0000F, 0.0000F, -0.3840F));
        PartDefinition mane = head.addOrReplaceChild("mane", CubeListBuilder.create()
                .texOffs(60, 52).addBox(-4F, -8F, 0F, 8F, 12F, 3F)
                .texOffs(240, 67).addBox(-2F, -14F, 1F, 4F, 6F, 2F), PartPose.offset(0F, -6F, 5F));
        PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(144, 32).addBox(-6F, -6F, -6F, 12F, 6F, 12F)
                .texOffs(232, 67).addBox(-1F, -14F, -1F, 2F, 8F, 2F)
                .texOffs(0, 32).addBox(-4F, 0F, -4F, 8F, 12F, 8F)
                .texOffs(116, 0).addBox(-5F, 11F, -5F, 10F, 10F, 10F)
                .texOffs(0, 67).addBox(-4F, 21F, -4F, 8F, 5F, 8F), PartPose.offset(14F, -22F, 0F));
        PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(96, 32).addBox(-6F, -6F, -6F, 12F, 6F, 12F)
                .texOffs(224, 67).addBox(-1F, -14F, -1F, 2F, 8F, 2F)
                .texOffs(196, 0).addBox(-4F, 0F, -4F, 8F, 12F, 8F)
                .texOffs(76, 0).addBox(-5F, 11F, -5F, 10F, 10F, 10F)
                .texOffs(198, 52).addBox(-4F, 21F, -4F, 8F, 5F, 8F), PartPose.offset(-14F, -22F, 0F));
        PartDefinition blade = right_arm.addOrReplaceChild("blade", CubeListBuilder.create()
                .texOffs(60, 0).addBox(-1F, 0F, -3F, 2F, 16F, 6F)
                .texOffs(46, 80).addBox(-5F, -1F, -2F, 10F, 2F, 4F), PartPose.offset(0F, 24F, 0F));
        return LayerDefinition.create(mesh, 256, 256);
    }

    public static LayerDefinition voidSovereign() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(196, 34).addBox(-4F, 0F, -4F, 8F, 8F, 8F)
                .texOffs(0, 68).addBox(-3F, 8F, -3F, 6F, 6F, 6F)
                .texOffs(114, 68).addBox(-2F, 14F, -2F, 4F, 2F, 4F), PartPose.offset(4F, 8F, 0F));
        PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(164, 34).addBox(-4F, 0F, -4F, 8F, 8F, 8F)
                .texOffs(228, 52).addBox(-3F, 8F, -3F, 6F, 6F, 6F)
                .texOffs(98, 68).addBox(-2F, 14F, -2F, 4F, 2F, 4F), PartPose.offset(-4F, 8F, 0F));
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(32, 34).addBox(-8F, -6F, -5F, 16F, 6F, 10F)
                .texOffs(40, 0).addBox(-9F, -26F, -5F, 18F, 20F, 10F)
                .texOffs(124, 52).addBox(-7F, -24F, -6F, 14F, 12F, 2F)
                .texOffs(16, 52).addBox(-9F, -8F, -6F, 18F, 2F, 12F), PartPose.offset(0F, 8F, 0F));
        PartDefinition core = body.addOrReplaceChild("core", CubeListBuilder.create()
                .texOffs(204, 52).addBox(-3F, -3F, -3F, 6F, 6F, 6F), PartPose.offset(0F, -16F, -7F));
        PartDefinition cape = body.addOrReplaceChild("cape", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-9F, 0F, 0F, 18F, 32F, 2F), PartPose.offsetAndRotation(0F, -24F, 5F, 0.0873F, 0.0000F, 0.0000F));
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(96, 0).addBox(-5F, -10F, -5F, 10F, 10F, 10F)
                .texOffs(48, 68).addBox(-4F, -8F, -6F, 8F, 7F, 1F)
                .texOffs(76, 52).addBox(-6F, -11F, -6F, 12F, 2F, 12F), PartPose.offset(0F, -26F, 0F));
        PartDefinition crown = head.addOrReplaceChild("crown", CubeListBuilder.create()
                .texOffs(24, 68).addBox(-1F, -9F, -1F, 2F, 9F, 2F)
                .texOffs(90, 68).addBox(-1F, -6F, -5F, 2F, 6F, 2F)
                .texOffs(82, 68).addBox(-1F, -6F, 3F, 2F, 6F, 2F)
                .texOffs(74, 68).addBox(-5F, -6F, -1F, 2F, 6F, 2F)
                .texOffs(66, 68).addBox(3F, -6F, -1F, 2F, 6F, 2F), PartPose.offset(0F, -11F, 0F));
        PartDefinition halo = head.addOrReplaceChild("halo", CubeListBuilder.create()
                .texOffs(200, 68).addBox(9.5F, -1.5F, -1F, 3F, 3F, 2F)
                .texOffs(190, 68).addBox(6.28F, 6.28F, -1F, 3F, 3F, 2F)
                .texOffs(180, 68).addBox(-1.5F, 9.5F, -1F, 3F, 3F, 2F)
                .texOffs(170, 68).addBox(-9.28F, 6.28F, -1F, 3F, 3F, 2F)
                .texOffs(160, 68).addBox(-12.5F, -1.5F, -1F, 3F, 3F, 2F)
                .texOffs(150, 68).addBox(-9.28F, -9.28F, -1F, 3F, 3F, 2F)
                .texOffs(140, 68).addBox(-1.5F, -12.5F, -1F, 3F, 3F, 2F)
                .texOffs(130, 68).addBox(6.28F, -9.28F, -1F, 3F, 3F, 2F), PartPose.offset(0F, -5F, 9F));
        PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(124, 34).addBox(-5F, -5F, -5F, 10F, 6F, 10F)
                .texOffs(40, 68).addBox(-1F, -12F, -1F, 2F, 7F, 2F)
                .texOffs(0, 34).addBox(-4F, 0F, -4F, 8F, 10F, 8F)
                .texOffs(176, 0).addBox(-5F, 10F, -5F, 10F, 8F, 10F)
                .texOffs(180, 52).addBox(-3F, 18F, -3F, 6F, 6F, 6F), PartPose.offset(13F, -22F, 0F));
        PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(84, 34).addBox(-5F, -5F, -5F, 10F, 6F, 10F)
                .texOffs(32, 68).addBox(-1F, -12F, -1F, 2F, 7F, 2F)
                .texOffs(216, 0).addBox(-4F, 0F, -4F, 8F, 10F, 8F)
                .texOffs(136, 0).addBox(-5F, 10F, -5F, 10F, 8F, 10F)
                .texOffs(156, 52).addBox(-3F, 18F, -3F, 6F, 6F, 6F), PartPose.offset(-13F, -22F, 0F));
        PartDefinition shard_left = body.addOrReplaceChild("shard_left", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-2F, -6F, -2F, 4F, 12F, 4F), PartPose.offset(22F, -30F, 0F));
        PartDefinition shard_right = body.addOrReplaceChild("shard_right", CubeListBuilder.create()
                .texOffs(228, 34).addBox(-2F, -6F, -2F, 4F, 12F, 4F), PartPose.offset(-22F, -30F, 0F));
        return LayerDefinition.create(mesh, 256, 256);
    }

    private BossModelLayers() {
    }
}
