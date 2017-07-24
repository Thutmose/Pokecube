package com.mcf.davidee.nbteditpqb.packets;

import org.apache.logging.log4j.Level;

import com.mcf.davidee.nbteditpqb.NBTEdit;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CustomRequestPacket implements IMessage
{
    /** The id of the entity being requested. */
    private int    entityID;
    /** the custom data type being requested */
    private String customName;

    /** Required default constructor. */
    public CustomRequestPacket()
    {
    }

    public CustomRequestPacket(int entityID, String customName)
    {
        this.entityID = entityID;
        this.customName = customName;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.entityID = buf.readInt();
        customName = new PacketBuffer(buf).readString(30);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(this.entityID);
        new PacketBuffer(buf).writeString(customName);
    }

    public static class Handler implements IMessageHandler<CustomRequestPacket, IMessage>
    {

        @Override
        public IMessage onMessage(CustomRequestPacket packet, MessageContext ctx)
        {
            EntityPlayerMP player = ctx.getServerHandler().player;
            NBTEdit.log(Level.TRACE, player.getName() + " requested entity with Id #" + packet.entityID);
            NBTEdit.NETWORK.sendCustomTag(player, packet.entityID, packet.customName);
            return null;
        }
    }
}
