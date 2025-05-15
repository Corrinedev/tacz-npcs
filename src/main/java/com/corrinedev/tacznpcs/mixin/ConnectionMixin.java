package com.corrinedev.tacznpcs.mixin;

import com.corrinedev.tacznpcs.common.ClientConnectionInterface;
import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Connection.class)
public abstract class ConnectionMixin implements ClientConnectionInterface {
    @Shadow private Channel channel;

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
