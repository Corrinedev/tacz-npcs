package com.corrinedev.serialize;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

public interface SerializedEntry<I> {
    I build(JsonObject obj);

    ResourceLocation getLocation();

    JsonObject getObject();
}
