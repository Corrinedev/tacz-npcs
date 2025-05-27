package com.corrinedev.tacznpcs.common;

import com.corrinedev.tacznpcs.Config;
import com.corrinedev.tacznpcs.common.entity.AbstractScavEntity;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber
public class Events {
    @SubscribeEvent
    public static void onHitByScav(EntityHurtByGunEvent.Pre event) {
        if(event.getAttacker() instanceof AbstractScavEntity && event.getHurtEntity() instanceof Player) {
            event.setBaseAmount((float) (event.getBaseAmount() * Config.NPCDAMAGEPLAYER.get()));
        } else if (event.getAttacker() instanceof AbstractScavEntity) {
            event.setBaseAmount((float) (event.getBaseAmount() * Config.NPCDAMAGE.get()));
        }
    }
    @SubscribeEvent
    public static void openDeadScav(PlayerInteractEvent.RightClickBlock event) {
        List<AbstractScavEntity> scavs = event.getLevel().getEntitiesOfClass(AbstractScavEntity.class, AABB.ofSize(event.getHitVec().getLocation(), 1, 1, 1));
        if(!scavs.isEmpty()) {
            scavs.forEach((entity) -> {
                if(entity.deadAsContainer || entity.isDeadOrDying()) {
                    entity.openCustomInventoryScreen(event.getEntity());
                }
            });
        }
    }
}
