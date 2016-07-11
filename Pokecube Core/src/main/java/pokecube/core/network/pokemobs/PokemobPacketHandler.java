package pokecube.core.network.pokemobs;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.core.PokecubeCore;
import pokecube.core.entity.pokemobs.helper.EntityMountablePokemob;
import pokecube.core.entity.pokemobs.helper.EntityMountablePokemob.MountState;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;

/** This class handles the packets sent for the IPokemob Entities.
 * 
 * @author Thutmose */
public class PokemobPacketHandler
{
    public static class MessageServer implements IMessage
    {
        public static class MessageHandlerServer implements IMessageHandler<MessageServer, IMessage>
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
                            int id = buffer.readInt();
                            IPokemob pokemob;
                            WorldServer world = FMLCommonHandler.instance().getMinecraftServerInstance()
                                    .worldServerForDimension(player.dimension);
                            Entity entity = PokecubeMod.core.getEntityProvider().getEntity(world, id, true);
                            if (entity == null || !(entity instanceof IPokemob)) { return; }
                            pokemob = (IPokemob) entity;
                            if (channel == RETURN)
                            {
                                Entity mob = (Entity) pokemob;
                                ((IPokemob) mob).returnToPokecube();
                            }
                            else if (channel == MOUNTDIR)
                            {
                                EntityMountablePokemob mob = (EntityMountablePokemob) pokemob;
                                byte mess = buffer.readByte();
                                MountState state = MountState.values()[mess];
                                mob.state = state;
                            }
                            else if (channel == MOVEINDEX)
                            {
                                byte moveIndex = buffer.readByte();
                                pokemob.setMoveIndex(moveIndex);
                            }
                            else if (channel == COME)
                            {
                                ((EntityLiving) pokemob).getNavigator().tryMoveToEntityLiving(player, 0.6);
                                ((EntityLiving) pokemob).setAttackTarget(null);
                                return;
                            }
                            else if (channel == MOVESWAP)
                            {
                                byte moveIndex0 = buffer.readByte();
                                byte moveIndex1 = buffer.readByte();
                                int num = buffer.readInt();
                                pokemob.setLeaningMoveIndex(num);
                                pokemob.exchangeMoves(moveIndex0, moveIndex1);
                            }
                            else if (channel == CANCELEVOLVE)
                            {
                                pokemob.cancelEvolve();
                            }
                        }
                    };
                    PokecubeCore.proxy.getMainThreadListener().addScheduledTask(toRun);
                }
            }

            @Override
            public IMessage onMessage(MessageServer message, MessageContext ctx)
            {
                EntityPlayer player = ctx.getServerHandler().playerEntity;
                new PacketHandler(player, message.buffer);
                return null;
            }
        }

        public static final byte RETURN       = 0;
        public static final byte MOVESWAP     = 4;
        public static final byte MOVEINDEX    = 5;
        public static final byte ALIVECHECK   = 7;
        public static final byte COME         = 10;
        public static final byte MOUNTDIR     = 11;
        public static final byte CANCELEVOLVE = 12;

        PacketBuffer             buffer;;

        public MessageServer()
        {
        }

        public MessageServer(byte messageid, int entityId)
        {
            this.buffer = new PacketBuffer(Unpooled.buffer(9));
            buffer.writeByte(messageid);
            buffer.writeInt(entityId);
        }

        public MessageServer(byte channel, int id, NBTTagCompound nbt)
        {
            this.buffer = new PacketBuffer(Unpooled.buffer(9));
            buffer.writeByte(channel);
            buffer.writeInt(id);
            buffer.writeNBTTagCompoundToBuffer(nbt);
        }

        public MessageServer(byte[] data)
        {
            this.buffer = new PacketBuffer(Unpooled.copiedBuffer(data));
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
