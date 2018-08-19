package pokecube.adventures.network;

import java.util.Random;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.blocks.afa.ContainerAFA;
import pokecube.adventures.blocks.afa.TileEntityAFA;
import pokecube.adventures.items.ItemTarget;
import pokecube.adventures.network.packets.PacketAFA;
import pokecube.adventures.network.packets.PacketBag;
import pokecube.adventures.network.packets.PacketCommander;
import pokecube.adventures.network.packets.PacketTrainer;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.pc.InventoryPC;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;

public class PacketPokeAdv
{

    public static class MessageClient implements IMessage
    {
        public static final byte TELEPORTEFFECTS = 9;

        public static class MessageHandlerClient implements IMessageHandler<MessageClient, MessageServer>
        {
            public void handleClientSide(EntityPlayer player, PacketBuffer buffer)
            {
                byte channel = buffer.readByte();
                byte[] message = new byte[buffer.array().length - 1];
                for (int i = 0; i < message.length; i++)
                {
                    message[i] = buffer.array()[i + 1];
                }
                if (channel == 2)
                {
                    try
                    {
                        NBTTagCompound tag = buffer.readCompoundTag();
                        if (tag == null) return;
                        NBTTagList list = (NBTTagList) tag.getTag("pc");
                        InventoryPC.loadFromNBT(list, true);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                if (channel == TELEPORTEFFECTS)
                {
                    Vector3 v = Vector3.readFromBuff(buffer);
                    Random rand = new Random();
                    for (int i = 0; i < 32; ++i)
                    {
                        player.getEntityWorld().spawnParticle(EnumParticleTypes.PORTAL, v.x + 0.5,
                                v.y + rand.nextDouble() * 2.0D, v.z + 0.5, rand.nextGaussian(), 0.0D,
                                rand.nextGaussian());
                    }
                    player.getEntityWorld().playSound(v.x, v.y, v.z, SoundEvents.ENTITY_ENDERMEN_TELEPORT,
                            SoundCategory.BLOCKS, 1, 1, false);
                }
                if (channel == MESSAGEGUIAFA && player.openContainer instanceof ContainerAFA)
                {
                    ContainerAFA cont = (ContainerAFA) player.openContainer;
                    TileEntityAFA tile = cont.tile;
                    int energy = buffer.readInt();
                    // System.out.println(tile.getEnergyStored(EnumFacing.DOWN)+"
                    // "+energy);
                    tile.setField(0, energy);
                }
            }

            @Override
            public MessageServer onMessage(MessageClient message, MessageContext ctx)
            {
                handleClientSide(PokecubeCore.getPlayer(null), message.buffer);
                return null;
            }

        }

        public static final byte MESSAGEGUIAFA = 11;

        PacketBuffer             buffer;;

        public MessageClient()
        {
        }

        public MessageClient(byte channel, NBTTagCompound nbt)
        {
            this.buffer = new PacketBuffer(Unpooled.buffer());
            buffer.writeByte(channel);
            buffer.writeCompoundTag(nbt);
            // System.out.println(buffer.array().length);
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
                byte channel = buffer.readByte();
                byte[] message = new byte[buffer.array().length - 1];
                for (int i = 0; i < message.length; i++)
                {
                    message[i] = buffer.array()[i + 1];
                }
                if (channel == MESSAGEBIOMESETTER)
                {
                    try
                    {
                        NBTTagCompound tag = buffer.readCompoundTag();
                        String biome = tag.getString("biome");
                        if (player.getHeldItemMainhand() != null
                                && player.getHeldItemMainhand().getItem() instanceof ItemTarget
                                && player.getHeldItemMainhand().getItemDamage() == 3)
                        {
                            player.getHeldItemMainhand().setTagCompound(tag);
                            BiomeType type = BiomeType.getBiome(biome);
                            player.getHeldItemMainhand().setStackDisplayName(type.readableName + " Setter");
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            public IMessage onMessage(MessageServer message, MessageContext ctx)
            {
                EntityPlayer player = ctx.getServerHandler().player;

                return handleServerSide(player, message.buffer);
            }

        }

        public static final byte MESSAGEBIOMESETTER = 9;

        PacketBuffer             buffer;;

        public MessageServer()
        {
        }

        public MessageServer(byte channel, NBTTagCompound nbt)
        {
            this.buffer = new PacketBuffer(Unpooled.buffer());
            buffer.writeByte(channel);
            buffer.writeCompoundTag(nbt);
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

    public static void init()
    {
        PokecubeMod.packetPipeline.registerMessage(PacketBag.class, PacketBag.class, PokecubeCore.getMessageID(),
                Side.CLIENT);
        PokecubeMod.packetPipeline.registerMessage(PacketBag.class, PacketBag.class, PokecubeCore.getMessageID(),
                Side.SERVER);
        PokecubeMod.packetPipeline.registerMessage(PacketTrainer.class, PacketTrainer.class,
                PokecubeCore.getMessageID(), Side.CLIENT);
        PokecubeMod.packetPipeline.registerMessage(PacketTrainer.class, PacketTrainer.class,
                PokecubeCore.getMessageID(), Side.SERVER);
        PokecubeMod.packetPipeline.registerMessage(PacketAFA.class, PacketAFA.class, PokecubeCore.getMessageID(),
                Side.CLIENT);
        PokecubeMod.packetPipeline.registerMessage(PacketAFA.class, PacketAFA.class, PokecubeCore.getMessageID(),
                Side.SERVER);

        PokecubeMod.packetPipeline.registerMessage(PacketCommander.class, PacketCommander.class,
                PokecubeCore.getMessageID(), Side.SERVER);
    }

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
