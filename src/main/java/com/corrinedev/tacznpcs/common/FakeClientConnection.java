package com.corrinedev.tacznpcs.common;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;

public class FakeClientConnection extends Connection
{
    public FakeClientConnection(PacketFlow p)
    {
        super(p);
        ((ClientConnectionInterface)this).setChannel(new EmbeddedChannel());
    }

    @Override
    public void setReadOnly()
    {
    }

    @Override
    public void handleDisconnection()
    {
    }

    @Override
    public void setListener(PacketListener packetListener)
    {
    }
}