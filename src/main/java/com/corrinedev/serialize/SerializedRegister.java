package com.corrinedev.serialize;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegisterEvent;

import java.util.Collection;

public record SerializedRegister<I>(ResourceKey resourceKey) {
    @SubscribeEvent
    public void register(RegisterEvent event, Collection<? extends SerializedEntry<I>> entries) {
        for(SerializedEntry<I> entry : entries) {
            event.register(resourceKey, entry.getLocation(), ()-> entry.build(entry.getObject()));
        }
    }
}
