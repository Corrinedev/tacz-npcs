package com.corrinedev.tacznpcs.common.registry;

import com.corrinedev.tacznpcs.common.entity.AbstractScavEntity;
import com.corrinedev.tacznpcs.common.entity.BanditEntity;
import com.corrinedev.tacznpcs.common.entity.DutyEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class AttributeRegistry {
    @SubscribeEvent
    public static void register(EntityAttributeCreationEvent event) {
        event.put(EntityTypeRegistry.BANDIT.get(), BanditEntity.createLivingAttributes().build());
        event.put(EntityTypeRegistry.DUTY.get(), DutyEntity.createLivingAttributes().build());
        event.put(EntityTypeRegistry.PATHFINDER.get(), PathfinderMob.createLivingAttributes().add(Attributes.MOVEMENT_SPEED, 0.15).add(Attributes.FOLLOW_RANGE, 64.0D).build());
    }
}
