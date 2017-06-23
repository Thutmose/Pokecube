package pokecube.adventures.network.packets;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.blocks.afa.ContainerAFA;
import pokecube.adventures.blocks.afa.ContainerDaycare;
import pokecube.core.PokecubeCore;
import thut.api.network.PacketHandler;

public class PacketAFA implements IMessage, IMessageHandler<PacketAFA, IMessage>
{
    public NBTTagCompound data = new NBTTagCompound();

    public PacketAFA()
    {
    }

    @Override
    public IMessage onMessage(final PacketAFA message, final MessageContext ctx)
    {
        PokecubeCore.proxy.getMainThreadListener().addScheduledTask(new Runnable()
        {
            @Override
            public void run()
            {
                processMessage(ctx, message);
            }
        });
        return null;
    }

    private void processMessage(MessageContext ctx, PacketAFA message)
    {
        EntityPlayer player;
        if (ctx.side == Side.CLIENT)
        {
            player = PokecubeCore.getPlayer(null);
        }
        else
        {
            player = ctx.getServerHandler().player;
        }
        IInventory tile = null;
        if ((player.openContainer instanceof ContainerAFA))
        {
            ContainerAFA cont = (ContainerAFA) player.openContainer;
            tile = cont.tile;
        }
        else if ((player.openContainer instanceof ContainerDaycare))
        {
            ContainerDaycare cont = (ContainerDaycare) player.openContainer;
            tile = cont.tile;
        }
        if (tile == null) return;
        if (message.data.hasKey("I"))
        {
            int id = message.data.getInteger("I");
            int val = message.data.getInteger("V");
            tile.setField(id, val);
        }
        PacketHandler.sendTileUpdate((TileEntity) tile);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        try
        {
            data = new PacketBuffer(buf).readCompoundTag();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        new PacketBuffer(buf).writeCompoundTag(data);
    }

}
