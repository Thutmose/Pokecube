package pokecube.core.network;

import java.util.ArrayList;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.core.Mod_Pokecube_Helper;
import pokecube.core.mod_Pokecube;
import pokecube.core.blocks.pc.ContainerPC;
import pokecube.core.blocks.pc.InventoryPC;
import pokecube.core.blocks.pc.SlotPC;
import pokecube.core.blocks.tradingTable.ContainerTMCreator;
import pokecube.core.blocks.tradingTable.ContainerTradingTable;
import pokecube.core.blocks.tradingTable.TileEntityTradingTable;
import pokecube.core.utils.PCSaveHandler;
import thut.api.maths.Vector3;

public class PCPacketHandler
{
    public static byte TYPESETPUBLIC  = 0;
    public static byte TYPEADDLAND    = 1;
    public static byte TYPEREMOVELAND = 2;

    public static void sendBagOpenPacket(boolean fromPC, Vector3 loc)
    {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        buf.writeByte(7);
        buf.writeBoolean(true);
        loc.writeToBuff(buf);
        MessageServer packet = new MessageServer(buf);
        PokecubePacketHandler.sendToServer(packet);
    }

    public static class MessageClient implements IMessage
    {
        PacketBuffer buffer;

        public MessageClient()
        {
        };

        public MessageClient(byte[] data)
        {
            this.buffer = new PacketBuffer(Unpooled.buffer());
            buffer.writeBytes(data);
        }

        public MessageClient(PacketBuffer buffer)
        {
            this.buffer = buffer;
        }

        public MessageClient(byte channel, NBTTagCompound nbt)
        {
            this.buffer = new PacketBuffer(Unpooled.buffer());
            buffer.writeByte(channel);
            buffer.writeNBTTagCompoundToBuffer(nbt);
            // System.out.println(buffer.array().length);
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
                if (channel == 1)
                {
                    try
                    {
                        NBTTagCompound tag = buffer.readNBTTagCompoundFromBuffer();
                        PCSaveHandler.getInstance().readPcFromNBT(tag);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
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
                if (channel == 5)
                {
                    handleTradePacket(message, player);
                }
            }

            @Override
            public MessageServer onMessage(MessageClient message, MessageContext ctx)
            {
                handleClientSide(mod_Pokecube.getPlayer(null), message.buffer);
                return null;
            }

        }

    }

    public static class MessageServer implements IMessage
    {
        PacketBuffer buffer;

        public MessageServer()
        {
        };

        public MessageServer(byte[] data)
        {
            this.buffer = new PacketBuffer(Unpooled.buffer());
            buffer.writeBytes(data);
        }

        public MessageServer(PacketBuffer buffer)
        {
            this.buffer = buffer;
        }

        public MessageServer(byte channel, NBTTagCompound nbt)
        {
            this.buffer = new PacketBuffer(Unpooled.buffer());
            buffer.writeByte(channel);
            buffer.writeNBTTagCompoundToBuffer(nbt);
            // System.out.println(buffer.array().length);
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

        public static class MessageHandlerServer implements IMessageHandler<MessageServer, IMessage>
        {
            public void handleServerSide(EntityPlayer player, PacketBuffer buffer)
            {
                byte channel = buffer.readByte();
                byte[] message = new byte[buffer.array().length - 1];
                for (int i = 0; i < message.length; i++)
                {
                    message[i] = buffer.array()[i + 1];
                }
                if (channel == 5)
                {
                    handleTradePacket(message, player);
                }
                if (channel == 6)
                {
                    handlePCPacket(message, player);
                }
                if (channel == 7)
                {
//                    boolean pc = buffer.readBoolean();//TODO see when this packet was sent
//                    Vector3 v = Vector3.readFromBuff(buffer);
//                    if (pc) player.openGui(PokecubeAdv.instance, PokecubeAdv.GUIBAG_ID, player.worldObj, v.intX(),
//                            v.intY(), v.intZ());
//                    else player.openGui(PokecubeAdv.instance, PokecubeAdv.GUIPC_ID, player.worldObj, v.intX(), v.intY(),
//                            v.intZ());
                }

                if (channel == 8)
                {
                    try
                    {
                        NBTTagCompound tag = buffer.readNBTTagCompoundFromBuffer();
                        int page = tag.getInteger("page");

                        boolean[] toRelease = new boolean[54];
                        InventoryPC pc = InventoryPC.getPC(player);
                        for (int i = 0; i < 54; i++)
                        {
                            toRelease[i] = tag.getBoolean("val" + i);
                            if (toRelease[i])
                            {
                                int j = i + page * 54;
                                pc.setInventorySlotContents(j, null);
                            }
                        }

                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public IMessage onMessage(MessageServer message, MessageContext ctx)
            {
                EntityPlayer player = ctx.getServerHandler().playerEntity;
                handleServerSide(player, message.buffer);
                return null;
            }

        }

    }

    public static void handleTradePacket(byte[] packet, EntityPlayer player)
    {
        String mes = new String(packet);

        String[] args = mes.split(",");

        byte message = Byte.valueOf(args[0].trim());

        if (message == 9 && player.worldObj.isRemote)
        {

            int x = Integer.valueOf(args[1].trim());
            int y = Integer.valueOf(args[2].trim());
            int z = Integer.valueOf(args[3].trim());

            TileEntityTradingTable tile = (TileEntityTradingTable) player.worldObj.getTileEntity(new BlockPos(x, y, z));

            int id;
            try
            {
                id = Integer.valueOf(args[4].trim());

                Entity player2 = player.worldObj.getEntityByID(id);

                if (args.length == 6)
                {
                    tile.player1 = null;
                    tile.player2 = null;
                }
                else if (player instanceof EntityPlayer)
                {
                    tile.addPlayer((EntityPlayer) player2);
                }
            }
            catch (NumberFormatException e)
            {
                e.printStackTrace();
            }
            return;
        }

        if (!(player instanceof EntityPlayerMP))
        {
            EntityPlayer sender = (EntityPlayer) player;
            if (Integer.valueOf(args[0]) == 3)
            {
                ArrayList<String> moves = new ArrayList<String>();
                if (args.length > 2) for (int i = 2; i < args.length; i++)
                {
                    moves.add(args[i].trim());
                }
                // System.out.println(moves);
                if (sender.openContainer instanceof ContainerTMCreator)
                {
                    ((ContainerTMCreator) sender.openContainer).getTile().moves.put(sender.getUniqueID().toString(),
                            moves);
                }
            }

        }
        else
        {
            EntityPlayerMP sender = (EntityPlayerMP) player;
            if (message == 0 && sender.openContainer instanceof ContainerTradingTable)
            {
                TileEntityTradingTable tradeTable = ((ContainerTradingTable) sender.openContainer).getTile();
                tradeTable.addPlayer(sender);
            }
            else if (message == 5)
            {
                int x = Integer.valueOf(args[1].trim());
                int y = Integer.valueOf(args[2].trim());
                int z = Integer.valueOf(args[3].trim());
                TileEntityTradingTable tile = (TileEntityTradingTable) sender.worldObj
                        .getTileEntity(new BlockPos(x, y, z));
                tile.addPlayer(null);

                mes = 9 + "," + x + "," + y + "," + z + "," + player.getEntityId() + ",0";
                Vector3 point = Vector3.getNewVectorFromPool().set(player);
                MessageClient pac = makeClientPacket(PokecubePacketHandler.CHANNEL_ID_TradingTable,
                        mes.getBytes());
                PokecubePacketHandler.sendToAllNear(pac, point, player.dimension, 10);
                point.freeVectorFromPool();
                return;

            }
            else if (message == 1)
            {
                TileEntityTradingTable tradeTable = sender.openContainer instanceof ContainerTMCreator
                        ? ((ContainerTMCreator) sender.openContainer).getTile()
                        : sender.openContainer instanceof ContainerTradingTable
                                ? ((ContainerTradingTable) sender.openContainer).getTile() : null;
                if (tradeTable == null) return;
                tradeTable.addPlayer(sender);
                tradeTable.trade = !tradeTable.trade;

                tradeTable.getWorld().markBlockForUpdate(tradeTable.getPos());
                tradeTable.markDirty();
                tradeTable.openGUI(sender);
            }
            else if (message == 2 && args.length > 1)
            {
                TileEntityTradingTable tradeTable = sender.openContainer instanceof ContainerTMCreator
                        ? ((ContainerTMCreator) sender.openContainer).getTile()
                        : ((ContainerTradingTable) sender.openContainer).getTile();
                tradeTable.addMoveToTM(args[1]);
            }
        }
    }

    public static void handlePCPacket(byte[] packet, EntityPlayer player)
    {
        byte message = 0;
        message = packet[0];
        // System.out.println(message);
        EntityPlayerMP sender = (EntityPlayerMP) player;
        if (message == 9)
        {
            if (sender.openContainer instanceof ContainerPC)
            {
                ((ContainerPC) (sender.openContainer)).pcTile.setBoundOwner(player.getUniqueID().toString());
                player.closeScreen();
            }
            return;
        }
        if (message == 5)
        {
            if (sender.openContainer instanceof ContainerPC)
            {
                ((ContainerPC) (sender.openContainer)).pcTile.toggleBound();
                player.closeScreen();
            }
            return;
        }
        if (message == 10)
        {
            player.openGui(mod_Pokecube.instance, Mod_Pokecube_Helper.GUIPC_ID, mod_Pokecube.getWorld(), 0, 0, 0);
            return;
        }
        if (message == 11)
        {
            int num = packet[1];
            byte[] string = new byte[num];
            for (int i = 0; i < num; i++)
            {
                string[i] = packet[i + 2];
            }
            if (sender.openContainer instanceof ContainerPC)
            {
                ((ContainerPC) (sender.openContainer)).changeName(new String(string));
            }
            return;
        }
        if (message == 3)
        {
            if (sender.openContainer instanceof ContainerPC)
            {
                ((ContainerPC) (sender.openContainer)).inv.autoToPC = !((ContainerPC) (sender.openContainer)).inv.autoToPC;
            }
            return;
        }
        if (sender.openContainer instanceof ContainerPC)
        {
            if (message < 3)
            {
                byte dir = (byte) (message == 1 ? 1 : message == 2 ? -1 : 0);
                ((ContainerPC) (sender.openContainer)).updateInventoryPages(dir, sender.inventory);
            }
            if (message == 5)
            {
                ((ContainerPC) (sender.openContainer)).pcTile.toggleBound();
            }
            if (message == 12)
            {
                ContainerPC cont = ((ContainerPC) (sender.openContainer));
                for (int i = 0; i < 54; i++)
                {
                    int index = i + 36;
                    SlotPC slot = (SlotPC) cont.inventorySlots.get(index);
                    slot.release = true;
                }
            }
            if (message == 13)
            {
                ContainerPC cont = ((ContainerPC) (sender.openContainer));
                for (int i = 0; i < 54; i++)
                {
                    int index = i + 36;
                    SlotPC slot = (SlotPC) cont.inventorySlots.get(index);
                    slot.release = false;
                }
            }
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

}
