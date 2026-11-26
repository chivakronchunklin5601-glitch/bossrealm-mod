package com.bossrealm.client;

import com.bossrealm.BossRealm;
import com.bossrealm.entity.BossBase;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BossRenderer<T extends BossBase> extends MobRenderer<T, BossModel<T>> {
    private final ResourceLocation texture;
    private final float hover;

    /**
     * @param name   texture file name without extension (textures/entity/NAME.png and NAME_glow.png)
     * @param hover  how many blocks the model is lifted off the ground (floating bosses)
     */
    public BossRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer, BossAnimations animations,
                        String name, float shadowRadius, float hover) {
        super(context, new BossModel<>(context.bakeLayer(layer), animations), shadowRadius);
        this.texture = new ResourceLocation(BossRealm.MODID, "textures/entity/" + name + ".png");
        this.hover = hover;
        this.addLayer(new BossGlowLayer<>(this, new ResourceLocation(BossRealm.MODID, "textures/entity/" + name + "_glow.png")));
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return this.texture;
    }

    @Override
    protected void scale(T entity, PoseStack poseStack, float partialTick) {
        // The pose stack is already flipped (-1, -1, 1) at this point, so "up" is a negative y translation.
        if (this.hover > 0.0F) {
            poseStack.translate(0.0F, -this.hover, 0.0F);
        }
    }
}
