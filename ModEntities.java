package com.bossrealm.registry;

import com.bossrealm.BossRealm;
import com.bossrealm.entity.BoneWarden;
import com.bossrealm.entity.InfernalReaver;
import com.bossrealm.entity.VoidSovereign;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, BossRealm.MODID);

    public static final RegistryObject<EntityType<BoneWarden>> BONE_WARDEN = ENTITIES.register("bone_warden",
            () -> EntityType.Builder.of(BoneWarden::new, MobCategory.MONSTER)
                    .sized(1.6F, 3.6F)
                    .clientTrackingRange(10)
                    .build("bone_warden"));

    public static final RegistryObject<EntityType<InfernalReaver>> INFERNAL_REAVER = ENTITIES.register("infernal_reaver",
            () -> EntityType.Builder.of(InfernalReaver::new, MobCategory.MONSTER)
                    .sized(1.7F, 4.0F)
                    .fireImmune()
                    .clientTrackingRange(10)
                    .build("infernal_reaver"));

    public static final RegistryObject<EntityType<VoidSovereign>> VOID_SOVEREIGN = ENTITIES.register("void_sovereign",
            () -> EntityType.Builder.of(VoidSovereign::new, MobCategory.MONSTER)
                    .sized(1.6F, 4.6F)
                    .clientTrackingRange(12)
                    .build("void_sovereign"));

    private ModEntities() {
    }
}
