package pokecube.core.network.pokemobs;

import java.io.IOException;
import java.util.logging.Level;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.utils.PokemobDataManager;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;

public class PacketPokemobMetadata implements IMessage, IMessageHandler<PacketPokemobMetadata, IMessage>
{
    public PacketBuffer wrapped = new PacketBuffer(Unpooled.buffer(0));

    public PacketPokemobMetadata()
    {
    }

    @Override
    public IMessage onMessage(final PacketPokemobMetadata message, final MessageContext ctx)
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

    @Override
    public void fromBytes(ByteBuf buf)
    {
        wrapped.writeBytes(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeBytes(wrapped);
    }

    static void processMessage(MessageContext ctx, PacketPokemobMetadata message)
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
        int id = message.wrapped.readInt();
        World world = player.getEntityWorld();
        Entity entity = PokecubeMod.core.getEntityProvider().getEntity(world, id, true);
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
        if (pokemob == null) { return; }
        EntityDataManager manager = pokemob.getDataManager();
        if (manager instanceof PokemobDataManager)
        {
            try
            {
                ((PokemobDataManager) manager).readEntry(message.wrapped);
            }
            catch (IOException e)
            {
                PokecubeMod.log(Level.WARNING, "Error with Datamanager updating for " + entity, e);
            }
        }
    }
}
