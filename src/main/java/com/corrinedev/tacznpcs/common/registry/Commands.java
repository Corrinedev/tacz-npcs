package com.corrinedev.tacznpcs.common.registry;

import com.corrinedev.tacznpcs.common.FakeClientConnection;
import com.corrinedev.tacznpcs.common.ScavPlayer;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class Commands {
    @SubscribeEvent
    public static void registerPlayerScavCommand(RegisterCommandsEvent e) {
        e.getDispatcher().register(
                net.minecraft.commands.Commands.literal("playerscav").executes((cmd) -> {
                    ScavPlayer.addScavPlayer(cmd.getSource().getLevel(), cmd.getSource().getPosition());
                    return 0;
                })
        );
    }
}
