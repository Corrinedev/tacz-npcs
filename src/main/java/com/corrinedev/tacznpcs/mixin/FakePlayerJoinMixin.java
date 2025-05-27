package com.corrinedev.tacznpcs.mixin;

import com.corrinedev.tacznpcs.common.ScavPlayer;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(PlayerList.class)
public class FakePlayerJoinMixin {
    @Nullable
    public ServerPlayer player;

    @Inject(method = "broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V", at = @At("HEAD"), cancellable = true)
    public void cancelJoinMessage(Component pMessage, boolean pBypassHiddenChat, CallbackInfo ci) {
        if(player != null && player instanceof ScavPlayer) {
            ci.cancel();
        }
    }

    @Inject(method = "placeNewPlayer", at = @At("HEAD"))
    public void storePlayer(Connection pNetManager, ServerPlayer pPlayer, CallbackInfo ci) {
        this.player = pPlayer;
    }

    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    public void unstorePlayer(Connection pNetManager, ServerPlayer pPlayer, CallbackInfo ci) {
        this.player = null;
    }
}
