package com.bossrealm.event;

import com.bossrealm.BossRealm;
import com.bossrealm.entity.BoneWarden;
import com.bossrealm.entity.InfernalReaver;
import com.bossrealm.entity.VoidSovereign;
import com.bossrealm.registry.ModEntities;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BossRealm.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CommonModEvents {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.BONE_WARDEN.get(), BoneWarden.createAttributes().build());
        event.put(ModEntities.INFERNAL_REAVER.get(), InfernalReaver.createAttributes().build());
        event.put(ModEntities.VOID_SOVEREIGN.get(), VoidSovereign.createAttributes().build());
    }

    private CommonModEvents() {
    }
}
