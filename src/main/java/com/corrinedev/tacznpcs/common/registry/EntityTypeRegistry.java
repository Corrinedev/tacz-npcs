package com.corrinedev.tacznpcs.common.registry;

import com.corrinedev.tacznpcs.common.entity.BanditEntity;
import com.corrinedev.tacznpcs.common.entity.DutyEntity;
import com.corrinedev.tacznpcs.common.entity.DutyPatrol;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.corrinedev.tacznpcs.NPCS.MODID;

public class EntityTypeRegistry {
    public static final DeferredRegister<EntityType<?>> TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    public static final RegistryObject<EntityType<BanditEntity>> BANDIT = TYPES.register("bandit", () -> BanditEntity.BANDIT);
    public static final RegistryObject<EntityType<DutyEntity>> DUTY = TYPES.register("duty", () -> DutyEntity.DUTY);
    public static final RegistryObject<EntityType<DutyPatrol>> DUTYSPAWNER = TYPES.register("duty_patrol", () -> EntityType.Builder.of(DutyPatrol::new, MobCategory.MISC).sized(0.5F, 0.5F).canSpawnFarFromPlayer().noSave().updateInterval(10).build("duty_patrol"));
}
