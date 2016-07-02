package pokecube.core.network.packets;

import java.io.IOException;
import java.util.ArrayList;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.tradingTable.ContainerTMCreator;
import pokecube.core.blocks.tradingTable.ContainerTradingTable;
import pokecube.core.blocks.tradingTable.TileEntityTradingTable;
import thut.api.network.PacketHandler;

public class PacketTrade implements IMessage, IMessageHandler<PacketTrade, IMessage>
{
    public static final byte SETTRADER = 0;
    public static final byte TRADE     = 1;
    public static final byte MAKETM    = 2;
    public static final byte SETMOVES  = 3;

    byte                     message;
    public NBTTagCompound    data      = new NBTTagCompound();

    public PacketTrade()
    {
    }

    public PacketTrade(byte message)
    {
        this.message = message;
    }

    @Override
    public IMessage onMessage(final PacketTrade message, final MessageContext ctx)
    {
        PokecubeCore.proxy.getMainThreadListener().addScheduledTask(new Runnable()
        {
            public void run()
            {
                processMessage(ctx, message);
            }
        });
        return null;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        message = buf.readByte();
        PacketBuffer buffer = new PacketBuffer(buf);
        try
        {
            data = buffer.readNBTTagCompoundFromBuffer();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeByte(message);
        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeNBTTagCompoundToBuffer(data);
    }

    void processMessage(MessageContext ctx, PacketTrade message)
    {
        EntityPlayer player;
        if (ctx.side == Side.CLIENT)
        {
            player = PokecubeCore.getPlayer(null);
        }
        else
        {
            player = ctx.getServerHandler().playerEntity;
        }
        if (message.message == MAKETM)
        {
            if (player.openContainer instanceof ContainerTMCreator)
            {
                TileEntityTradingTable tradeTable = ((ContainerTMCreator) player.openContainer).getTile();
                tradeTable.addMoveToTM(message.data.getString("M"));
            }
        }
        else if (message.message == SETMOVES)
        {
            if (player.openContainer instanceof ContainerTMCreator)
            {
                TileEntityTradingTable tradeTable = ((ContainerTMCreator) player.openContainer).getTile();
                ArrayList<String> moves = new ArrayList<String>();
                for (int i = 0; i < message.data.getInteger("N"); i++)
                    moves.add(message.data.getString("M" + i));
                tradeTable.moves.put(player.getUniqueID().toString(), moves);
            }
        }
        else if (message.message == SETTRADER)
        {
            int[] coords = message.data.getIntArray("L");
            BlockPos pos = new BlockPos(coords[0], coords[1], coords[2]);
            TileEntityTradingTable tradeTable = (TileEntityTradingTable) player.getEntityWorld().getTileEntity(pos);
            boolean remove = message.data.getBoolean("R");
            if (remove)
            {
                tradeTable.player1 = null;
                tradeTable.player2 = null;
            }
            else
            {
                player = (EntityPlayer) player.getEntityWorld().getEntityByID(message.data.getInteger("I"));
                tradeTable.addPlayer(player);
            }
            if (!player.getEntityWorld().isRemote) PacketHandler.sendTileUpdate(tradeTable);

        }
        else if (message.message == TRADE)
        {
            if (player.openContainer instanceof ContainerTradingTable)
            {
                TileEntityTradingTable tradeTable = ((ContainerTradingTable) player.openContainer).getTile();
                tradeTable.trade();
                PacketHandler.sendTileUpdate(tradeTable);
            }
        }
    }
}
