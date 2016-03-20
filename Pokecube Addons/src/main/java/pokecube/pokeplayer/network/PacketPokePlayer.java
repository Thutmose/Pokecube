package pokecube.pokeplayer.network;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.core.PokecubeCore;
import pokecube.pokeplayer.PokePlayer;
import pokecube.pokeplayer.Proxy.PokeInfo;

public class PacketPokePlayer
{
    public static class MessageClient implements IMessage
    {
        public static final byte SETPOKE = 1;

        static class PacketHandler
        {
            final EntityPlayer player;
            final PacketBuffer buffer;

            public PacketHandler(EntityPlayer p, PacketBuffer b)
            {
                this.player = p;
                this.buffer = b;
                Runnable toRun = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        byte channel = buffer.readByte();
                        if (channel == SETPOKE)
                        {
                            try
                            {
                                boolean isPokemob = buffer.readBoolean();
                                player.getEntityData().setBoolean("isPokemob", isPokemob);
                                if (isPokemob)
                                {
                                    float h = buffer.readFloat();
                                    float w = buffer.readFloat();
                                    NBTTagCompound tag = buffer.readNBTTagCompoundFromBuffer();
                                    player.getEntityData().setTag("Pokemob", tag);
                                    PokePlayer.proxy.getPokemob(player);
                                    PokeInfo info = PokePlayer.proxy.playerMap.get(player.getUniqueID());
                                    info.originalHeight = h;
                                    info.originalWidth = w;
                                }
                                else
                                {
                                    PokePlayer.proxy.setPokemob(player, null);
                                }
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                PokecubeCore.proxy.getMainThreadListener().addScheduledTask(toRun);
            }
        }

        public static class MessageHandlerClient implements IMessageHandler<MessageClient, MessageServer>
        {
            @Override
            public MessageServer onMessage(MessageClient message, MessageContext ctx)
            {
                new PacketHandler(PokecubeCore.getPlayer(null), message.buffer);
                return null;
            }

        }

        PacketBuffer buffer;;

        public MessageClient()
        {
        }

        public MessageClient(byte channel, NBTTagCompound nbt)
        {
            this.buffer = new PacketBuffer(Unpooled.buffer());
            buffer.writeByte(channel);
            buffer.writeNBTTagCompoundToBuffer(nbt);
        }

        public MessageClient(byte[] data)
        {
            this.buffer = new PacketBuffer(Unpooled.buffer());
            buffer.writeBytes(data);
        }

        public MessageClient(PacketBuffer buffer)
        {
            this.buffer = buffer;
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            if (buffer == null)
            {
                buffer = new PacketBuffer(Unpooled.buffer());
            }
            buffer.writeBytes(buf);
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            if (buffer == null)
            {
                buffer = new PacketBuffer(Unpooled.buffer());
            }
            buf.writeBytes(buffer);
        }

    }

    public static class MessageServer implements IMessage
    {
        public static class MessageHandlerServer implements IMessageHandler<MessageServer, IMessage>
        {
            public IMessage handleServerSide(EntityPlayer player, PacketBuffer buffer)
            {
                return null;
            }

            @Override
            public IMessage onMessage(MessageServer message, MessageContext ctx)
            {
                EntityPlayer player = ctx.getServerHandler().playerEntity;

                return handleServerSide(player, message.buffer);
            }

        }

        public static final byte MESSAGEGUIAFA = 11;

        PacketBuffer             buffer;;

        public MessageServer()
        {
        }

        public MessageServer(byte channel, NBTTagCompound nbt)
        {
            this.buffer = new PacketBuffer(Unpooled.buffer());
            buffer.writeByte(channel);
            buffer.writeNBTTagCompoundToBuffer(nbt);
            // System.out.println(buffer.array().length);
        }

        public MessageServer(byte[] data)
        {
            this.buffer = new PacketBuffer(Unpooled.buffer());
            buffer.writeBytes(data);
        }

        public MessageServer(PacketBuffer buffer)
        {
            this.buffer = buffer;
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            if (buffer == null)
            {
                buffer = new PacketBuffer(Unpooled.buffer());
            }
            buffer.writeBytes(buf);
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            if (buffer == null)
            {
                buffer = new PacketBuffer(Unpooled.buffer());
            }
            buf.writeBytes(buffer);
        }

    }

    public static byte TYPESETPUBLIC  = 0;

    public static byte TYPEADDLAND    = 1;

    public static byte TYPEREMOVELAND = 2;

    public static MessageClient makeClientPacket(byte channel, byte[] data)
    {
        byte[] packetData = new byte[data.length + 1];
        packetData[0] = channel;

        for (int i = 1; i < packetData.length; i++)
        {
            packetData[i] = data[i - 1];
        }
        return new MessageClient(packetData);
    }

    public static MessageServer makeServerPacket(byte channel, byte[] data)
    {
        byte[] packetData = new byte[data.length + 1];
        packetData[0] = channel;

        for (int i = 1; i < packetData.length; i++)
        {
            packetData[i] = data[i - 1];
        }
        return new MessageServer(packetData);
    }
}
