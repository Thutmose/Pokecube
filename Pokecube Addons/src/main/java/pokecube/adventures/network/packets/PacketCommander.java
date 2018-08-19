package pokecube.adventures.network.packets;

import java.io.IOException;
import java.util.logging.Level;

import io.netty.buffer.ByteBuf;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.adventures.blocks.afa.TileEntityCommander;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;
import thut.api.network.PacketHandler;

public class PacketCommander implements IMessage, IMessageHandler<PacketCommander, IMessage>
{
    public NBTTagCompound data = new NBTTagCompound();

    public PacketCommander()
    {
    }

    @Override
    public IMessage onMessage(final PacketCommander message, final MessageContext ctx)
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

    private void processMessage(MessageContext ctx, PacketCommander message)
    {
        World world = ctx.getServerHandler().player.getEntityWorld();
        BlockPos pos = new BlockPos(message.data.getInteger("x"), message.data.getInteger("y"),
                message.data.getInteger("z"));
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntityCommander)) return;
        TileEntityCommander tile = (TileEntityCommander) te;
        String command = message.data.getString("C");
        String args = message.data.getString("A");
        Command command_ = null;
        try
        {
            if (!command.isEmpty()) command_ = Command.valueOf(command);
            tile.setCommand(command_, args);
        }
        catch (Exception e)
        {
            if (PokecubeMod.debug) PokecubeMod.log(Level.WARNING, "Invalid Commander Block use at " + tile.getPos(), e);
            tile.getWorld().playSound(null, tile.getPos(), SoundEvents.BLOCK_NOTE_BASEDRUM, SoundCategory.BLOCKS, 1, 1);
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
