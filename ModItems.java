package com.bossrealm.registry;

import com.bossrealm.BossRealm;

import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BossRealm.MODID);

    public static final RegistryObject<ForgeSpawnEggItem> BONE_WARDEN_SPAWN_EGG = ITEMS.register("bone_warden_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.BONE_WARDEN, 0xD8D2BC, 0x3AF0DC, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> INFERNAL_REAVER_SPAWN_EGG = ITEMS.register("infernal_reaver_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.INFERNAL_REAVER, 0x3A1612, 0xFF7A1E, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> VOID_SOVEREIGN_SPAWN_EGG = ITEMS.register("void_sovereign_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.VOID_SOVEREIGN, 0x1A0E2E, 0xB070FF, new Item.Properties()));

    private ModItems() {
    }
}
