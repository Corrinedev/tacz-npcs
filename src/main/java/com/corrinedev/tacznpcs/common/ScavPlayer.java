package com.corrinedev.tacznpcs.common;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;

import java.util.UUID;

public class ScavPlayer extends FakePlayer {
    public ScavPlayer(ServerLevel pLevel) {
        super(pLevel, new GameProfile(UUID.randomUUID(), "Scav"));

    }
}
