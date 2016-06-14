package pokecube.adventures.network;

import java.util.Random;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.afa.ContainerAFA;
import pokecube.adventures.blocks.afa.TileEntityAFA;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.adventures.handlers.TeamManager;
import pokecube.adventures.items.ItemTarget;
import pokecube.adventures.items.bags.ContainerBag;
import pokecube.adventures.items.bags.InventoryBag;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.pc.InventoryPC;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.utils.ChunkCoordinate;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;

public class PacketPokeAdv
{
    public static class MessageClient implements IMessage
    {
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
                        NBTTagCompound tag = buffer.readNBTTagCompoundFromBuffer();
                        if (tag == null) return;
                        NBTTagList list = (NBTTagList) tag.getTag("pc");
                        InventoryPC.loadFromNBT(list, true);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                if (channel == 3)
                {
                    handleTrainerEditPacket(buffer, player);
                }
                if (channel == 6)
                {
                    byte type = buffer.readByte();

                    int x = buffer.readInt();
                    int y = buffer.readInt();
                    int z = buffer.readInt();
                    int dim = buffer.readInt();

                    ChunkCoordinate c = new ChunkCoordinate(x, y, z, dim);

                    if (type == TYPESETPUBLIC)
                    {
                        if (TeamManager.getInstance().isPublic(c)) TeamManager.getInstance().unsetPublic(c);
                        else TeamManager.getInstance().setPublic(c);
                    }
                    else if (type == TYPEADDLAND)
                    {
                        String team = buffer.readStringFromBuffer(20);
                        TeamManager.getInstance().addTeamLand(team, c);
                    }
                    else if (type == TYPEREMOVELAND)
                    {
                        String team = buffer.readStringFromBuffer(20);
                        TeamManager.getInstance().removeTeamLand(team, c);
                    }
                }
                if (channel == 7)
                {
                    try
                    {
                        NBTTagCompound tag = buffer.readNBTTagCompoundFromBuffer();
                        TeamManager.getInstance().loadFromNBT(tag);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                if (channel == 9)
                {
                    Vector3 v = Vector3.readFromBuff(buffer);
                    Random rand = new Random();
                    for (int i = 0; i < 32; ++i)
                    {
                        player.worldObj.spawnParticle(EnumParticleTypes.PORTAL, v.x + 0.5,
                                v.y + rand.nextDouble() * 2.0D, v.z + 0.5, rand.nextGaussian(), 0.0D,
                                rand.nextGaussian());
                    }
                    player.worldObj.playSoundEffect(v.x, v.y, v.z, "mob.endermen.portal", 1.0F, 1.0F);
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
            buffer.writeNBTTagCompoundToBuffer(nbt);
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
                if (channel == 1)
                {
                    handleTrainerEditPacket(buffer, player);
                }
                if (channel == 6)
                {
                    int m = buffer.readByte();
                    ((ContainerBag) (player.openContainer)).gotoInventoryPage(m);
                }
                if (channel == 7)
                {
                    player.openGui(PokecubeAdv.instance, PokecubeAdv.GUIBAG_ID, player.worldObj,
                            InventoryBag.getBag(player).getPage() + 1, 0, 0);
                }
                if (channel == 9)
                {
                    try
                    {
                        NBTTagCompound tag = buffer.readNBTTagCompoundFromBuffer();
                        String biome = tag.getString("biome");
                        if (player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemTarget
                                && player.getHeldItem().getItemDamage() == 3)
                        {
                            player.getHeldItem().setTagCompound(tag);
                            BiomeType type = BiomeType.getBiome(biome);
                            player.getHeldItem().setStackDisplayName(type.readableName + " Setter");
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                if (channel == MESSAGEGUIAFA && player.openContainer instanceof ContainerAFA)
                {
                    ContainerAFA cont = (ContainerAFA) player.openContainer;
                    TileEntityAFA tile = cont.tile;
                    if (buffer.readableBytes() > 7)
                    {
                        int id = buffer.readInt();
                        int val = buffer.readInt();
                        System.out.println(id + " " + val);
                        tile.setField(id, val);
                    }
                    PacketBuffer retBuf = new PacketBuffer(Unpooled.buffer(5));
                    retBuf.writeByte(MessageClient.MESSAGEGUIAFA);
                    retBuf.writeInt(tile.getField(0));
                    MessageClient ret = new MessageClient(retBuf);
                    return ret;

                }
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

    public static void handleTrainerEditPacket(PacketBuffer buffer, EntityPlayer player)
    {
        int[] numbers = new int[6];
        int[] levels = new int[6];
        String name = "";
        String type = "";
        try
        {
            PacketBuffer ret = new PacketBuffer(Unpooled.buffer());
            ret.writeByte(3);
            for (int i = 0; i < 6; i++)
            {
                numbers[i] = buffer.readInt();
                levels[i] = buffer.readInt();

                ret.writeInt(numbers[i]);
                ret.writeInt(levels[i]);
            }
            int num = buffer.readInt();
            ret.writeInt(num);
            byte[] string = new byte[num];
            for (int n = 0; n < num; n++)
            {
                string[n] = buffer.readByte();
                ret.writeByte(string[n]);
            }
            name = new String(string);
            num = buffer.readInt();
            ret.writeInt(num);
            string = new byte[num];
            for (int n = 0; n < num; n++)
            {
                string[n] = buffer.readByte();
                ret.writeByte(string[n]);
            }
            type = new String(string);
            int id = buffer.readInt();
            ret.writeInt(id);
            EntityTrainer trainer = (EntityTrainer) player.worldObj.getEntityByID(id);
            boolean stationary = buffer.readBoolean();
            ret.writeBoolean(stationary);
            trainer.name = name;
            trainer.type = TypeTrainer.getTrainer(type);
            trainer.setTypes();
            for (int index = 0; index < 6; index++)
            {
                trainer.setPokemob(numbers[index], levels[index], index);
            }
            if (!player.worldObj.isRemote)
            {
                if (stationary)
                {
                    trainer.setStationary(true);
                }
                MessageClient mes = new MessageClient(ret.array());
                PokecubePacketHandler.sendToClient(mes, player);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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

    public static void sendBagOpenPacket()
    {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        buf.writeByte(7);
        MessageServer packet = new MessageServer(buf);
        PokecubePacketHandler.sendToServer(packet);
    }

}
