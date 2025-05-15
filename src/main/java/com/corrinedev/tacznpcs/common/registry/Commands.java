package com.corrinedev.tacznpcs.common.registry;

import com.corrinedev.tacznpcs.common.FakeClientConnection;
import com.corrinedev.tacznpcs.common.ScavPlayer;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
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
                    var scav = new ScavPlayer(cmd.getSource().getLevel());
                    scav.setPos(cmd.getSource().getPosition());

                    cmd.getSource().getServer().getPlayerList().placeNewPlayer(new FakeClientConnection(PacketFlow.SERVERBOUND), scav);
                    cmd.getSource().getLevel().addFreshEntity(scav);

                    return 0;
                })
        );
    }
}
