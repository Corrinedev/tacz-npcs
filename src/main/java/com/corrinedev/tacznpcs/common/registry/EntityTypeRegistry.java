package com.corrinedev.tacznpcs.common.registry;

import com.corrinedev.tacznpcs.common.entity.BanditEntity;
import com.corrinedev.tacznpcs.common.entity.DutyEntity;
import com.corrinedev.tacznpcs.common.entity.DutyPatrol;
import com.corrinedev.tacznpcs.common.entity.JScavEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

import static com.corrinedev.tacznpcs.NPCS.MODID;
@Mod.EventBusSubscriber
public class EntityTypeRegistry {
    public static final DeferredRegister<EntityType<?>> TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    public static final RegistryObject<EntityType<BanditEntity>> BANDIT = TYPES.register("bandit", () -> BanditEntity.BANDIT);
    public static final RegistryObject<EntityType<DutyEntity>> DUTY = TYPES.register("duty", () -> DutyEntity.DUTY);

    @SubscribeEvent
    public static void register(RegisterEvent event) {

    }
}
