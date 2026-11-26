package com.bossrealm.client;

import com.bossrealm.entity.BossBase;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Draws the "_glow" texture at full brightness (eyes, lava cracks, crystals) like the spider's eyes layer. */
public class BossGlowLayer<T extends BossBase> extends RenderLayer<T, BossModel<T>> {
    private final ResourceLocation glowTexture;

    public BossGlowLayer(RenderLayerParent<T, BossModel<T>> parent, ResourceLocation glowTexture) {
        super(parent);
        this.glowTexture = glowTexture;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (entity.isInvisible()) {
            return;
        }
        VertexConsumer consumer = buffer.getBuffer(RenderType.eyes(this.glowTexture));
        this.getParentModel().renderToBuffer(poseStack, consumer, LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}
