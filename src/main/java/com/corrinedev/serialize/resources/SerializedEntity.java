package com.corrinedev.serialize.resources;

import com.corrinedev.serialize.SerializedEntry;
import com.corrinedev.tacznpcs.common.entity.JScavEntity;
import com.corrinedev.tacznpcs.common.entity.inventory.ScavInventory;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import static com.corrinedev.tacznpcs.NPCS.MODID;

public record SerializedEntity(String name) implements SerializedEntry<EntityType<JScavEntity>> {
    @Override
    public EntityType<JScavEntity> build(JsonObject obj) {
        return EntityType.Builder.of(JScavEntity::new, MobCategory.MISC).build(getLocation().getPath());
    }

    @Override
    public ResourceLocation getLocation() {
        return new ResourceLocation(MODID, name);
    }

    @Override
    public JsonObject getObject() {
        return null;
    }
}
