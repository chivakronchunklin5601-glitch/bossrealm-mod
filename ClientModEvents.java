package com.bossrealm.client;

import com.bossrealm.BossRealm;
import com.bossrealm.registry.ModEntities;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BossRealm.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BossModelLayers.BONE_WARDEN, BossModelLayers::boneWarden);
        event.registerLayerDefinition(BossModelLayers.INFERNAL_REAVER, BossModelLayers::infernalReaver);
        event.registerLayerDefinition(BossModelLayers.VOID_SOVEREIGN, BossModelLayers::voidSovereign);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BONE_WARDEN.get(), context -> new BossRenderer<>(context,
                BossModelLayers.BONE_WARDEN, BossAnimations.boneWarden(), "bone_warden", 1.6F, 0.0F));
        event.registerEntityRenderer(ModEntities.INFERNAL_REAVER.get(), context -> new BossRenderer<>(context,
                BossModelLayers.INFERNAL_REAVER, BossAnimations.infernalReaver(), "infernal_reaver", 1.7F, 0.0F));
        event.registerEntityRenderer(ModEntities.VOID_SOVEREIGN.get(), context -> new BossRenderer<>(context,
                BossModelLayers.VOID_SOVEREIGN, BossAnimations.voidSovereign(), "void_sovereign", 1.5F, 0.4F));
    }

    private ClientModEvents() {
    }
}
