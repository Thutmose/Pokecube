package pokecube.pokeplayer.network;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.templates.Move_Explode;
import pokecube.core.moves.templates.Move_Utility;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.PokecubeClientPacket;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.pokeplayer.PokeInfo;
import pokecube.pokeplayer.PokePlayer;
import pokecube.pokeplayer.client.gui.GuiAsPokemob;
import thut.api.maths.Vector3;

public class PacketPokePlayer
{
    public static class MessageClient implements IMessage
    {
        public static final byte SETPOKE   = 1;
        public static final byte MOVEINDEX = 2;

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
                                int id = buffer.readInt();
                                boolean isPokemob = buffer.readBoolean();

                                Entity e = player.worldObj.getEntityByID(id);
                                EntityPlayer temp = player;
                                EntityPlayer player = temp;
                                if (e instanceof EntityPlayer)
                                {
                                    player = (EntityPlayer) e;
                                }

                                player.getEntityData().setBoolean("isPokemob", isPokemob);
                                if (isPokemob)
                                {
                                    float h = buffer.readFloat();
                                    float w = buffer.readFloat();
                                    NBTTagCompound tag = buffer.readNBTTagCompoundFromBuffer();
                                    player.getEntityData().setTag("Pokemob", tag);
                                    PokePlayer.PROXY.getPokemob(player);
                                    PokeInfo info = PokePlayer.PROXY.playerMap.get(player.getUniqueID());
                                    info.originalHeight = h;
                                    info.originalWidth = w;
                                    info.pokemob.setPokemonOwner(player);
                                    info.pokemob.setPokemonNickname(player.getName());
                                }
                                else
                                {
                                    PokePlayer.PROXY.setPokemob(player, null);
                                }
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else if (channel == MOVEINDEX)
                        {
                            int index = buffer.readByte();
                            GuiAsPokemob.moveIndex = index;
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
                        if (channel == MOVEUSE)
                        {
                            handleMoveUse(PokePlayer.PROXY.getPokemob(player));
                        }
                        else if (channel == MOVEINDEX)
                        {
                            IPokemob pokemob = PokePlayer.PROXY.getPokemob(player);
                            if (pokemob != null)
                            {
                                byte index = buffer.readByte();
                                pokemob.setMoveIndex(index);
                                PacketBuffer bufferRet = new PacketBuffer(Unpooled.buffer(2));
                                bufferRet.writeByte(MessageClient.MOVEINDEX);
                                bufferRet.writeByte(index);
                                MessageClient message = new MessageClient(bufferRet);
                                PokecubePacketHandler.sendToClient(message, player);
                            }

                        }
                    }
                };
                PokecubeCore.proxy.getMainThreadListener().addScheduledTask(toRun);
            }

            private void handleMoveUse(IPokemob pokemob)
            {
                PacketBuffer dat = buffer;
                int id = dat.readInt();
                Vector3 v = Vector3.getNewVector();

                if (pokemob != null)
                {
                    int currentMove = pokemob.getMoveIndex();

                    if (currentMove == 5) { return; }

                    Move_Base move = MovesUtils.getMoveFromName(pokemob.getMoves()[currentMove]);
                    boolean teleport = dat.readBoolean();

                    if (teleport)
                    {
                        NBTTagCompound teletag = new NBTTagCompound();
                        PokecubeSerializer.getInstance().writePlayerTeleports(player.getUniqueID(), teletag);

                        PokecubeClientPacket packe = new PokecubeClientPacket(PokecubeClientPacket.TELEPORTLIST,
                                teletag);
                        PokecubePacketHandler.sendToClient(packe, player);
                    }

                    if (move instanceof Move_Explode && (id == 0))
                    {
                        pokemob.executeMove(null, v.set(pokemob), 0);
                    }
                    else if (Move_Utility.isUtilityMove(move.name) && (id == 0))
                    {
                        pokemob.setPokemonAIState(IMoveConstants.NEWEXECUTEMOVE, true);
                    }
                    else
                    {
                        Entity owner = player;
                        if (owner != null)
                        {
                            Entity closest = PokecubeMod.core.getEntityProvider().getEntity(owner.worldObj, id, false);
                            
                            if (closest != null)
                            {
                                pokemob.executeMove(closest, v.set(closest),
                                        closest.getDistanceToEntity((Entity) pokemob));
                            }
                            else if (buffer.isReadable(24))
                            {
                                v = Vector3.readFromBuff(buffer);
                                pokemob.executeMove(closest, v, (float) v.distToEntity((Entity) pokemob));
                            }
                        }
                    }
                }
            }
        }

        public static class MessageHandlerServer implements IMessageHandler<MessageServer, IMessage>
        {
            public IMessage handleServerSide(EntityPlayer player, PacketBuffer buffer)
            {
                new PacketHandler(player, buffer);
                return null;
            }

            @Override
            public IMessage onMessage(MessageServer message, MessageContext ctx)
            {
                EntityPlayer player = ctx.getServerHandler().playerEntity;

                return handleServerSide(player, message.buffer);
            }

        }

        public static final byte MOVEUSE   = 1;
        public static final byte MOVEINDEX = 2;

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
