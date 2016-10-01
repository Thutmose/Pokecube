package pokecube.adventures.network.packets;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.blocks.afa.ContainerAFA;
import pokecube.adventures.blocks.afa.TileEntityAFA;
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
            player = ctx.getServerHandler().playerEntity;
        }
        if (!(player.openContainer instanceof ContainerAFA)) return;
        ContainerAFA cont = (ContainerAFA) player.openContainer;
        TileEntityAFA tile = cont.tile;
        if (message.data.hasKey("I"))
        {
            int id = message.data.getInteger("I");
            int val = message.data.getInteger("V");
            tile.setField(id, val);
        }
        PacketHandler.sendTileUpdate(tile);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        try
        {
            data = new PacketBuffer(buf).readNBTTagCompoundFromBuffer();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        new PacketBuffer(buf).writeNBTTagCompoundToBuffer(data);
    }

}
