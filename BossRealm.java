package com.bossrealm;

import com.bossrealm.registry.ModEntities;
import com.bossrealm.registry.ModItems;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BossRealm.MODID)
public class BossRealm {
    public static final String MODID = "bossrealm";

    public BossRealm() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITIES.register(modBus);
        ModItems.ITEMS.register(modBus);
        modBus.addListener(this::addCreativeTabContents);
    }

    private void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.BONE_WARDEN_SPAWN_EGG);
            event.accept(ModItems.INFERNAL_REAVER_SPAWN_EGG);
            event.accept(ModItems.VOID_SOVEREIGN_SPAWN_EGG);
        }
    }
}
