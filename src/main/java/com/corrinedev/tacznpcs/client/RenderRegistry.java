package com.corrinedev.tacznpcs.client;

import com.corrinedev.tacznpcs.client.renderer.HumanoidScavModel;
import com.corrinedev.tacznpcs.client.renderer.HumanoidScavRenderer;
import com.corrinedev.tacznpcs.common.registry.EntityTypeRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static com.corrinedev.tacznpcs.NPCS.MODID;

@Mod.EventBusSubscriber(value = Dist.CLIENT,modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RenderRegistry {
    @SubscribeEvent
    public static void register(FMLClientSetupEvent event) {
        EntityRenderers.register(EntityTypeRegistry.BANDIT.get(), (ctx) -> new HumanoidScavRenderer(ctx, new HumanoidScavModel(true)));
        EntityRenderers.register(EntityTypeRegistry.DUTY.get(),  (ctx) -> new HumanoidScavRenderer(ctx, new HumanoidScavModel(true)));
    }
}
