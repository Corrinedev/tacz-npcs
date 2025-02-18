package com.corrinedev.tacznpcs.common.entity;

import com.corrinedev.tacznpcs.common.registry.EntityTypeRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class DutyPatrol extends PatrolSpawnerEntity<DutyEntity>{

    public DutyPatrol(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel, new DutyEntity(EntityTypeRegistry.DUTY.get(), pLevel));
    }

}
