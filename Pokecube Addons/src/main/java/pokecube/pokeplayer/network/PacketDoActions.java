package pokecube.pokeplayer.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.packets.PacketDataSync;
import pokecube.pokeplayer.PokePlayer;
import pokecube.pokeplayer.client.gui.GuiAsPokemob;
import thut.api.maths.Vector3;

public class PacketDoActions implements IMessage, IMessageHandler<PacketDoActions, IMessage>
{
    public static final byte MOVEINDEX = 0;
    public static final byte MOVEUSE   = 1;

    PacketBuffer             buffer    = new PacketBuffer(Unpooled.buffer(4));

    public PacketDoActions()
    {
    }

    public PacketDoActions(PacketBuffer buf)
    {
        buffer = buf;
    }

    @Override
    public IMessage onMessage(final PacketDoActions message, final MessageContext ctx)
    {
        PokecubeCore.proxy.getMainThreadListener().addScheduledTask(new Runnable()
        {
            @Override
            public void run()
            {
                apply(message, ctx);
            }
        });
        return null;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        buffer = new PacketBuffer(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeBytes(buffer);
    }

    private void handleMoveUse(IPokemob pokemob, EntityPlayer player)
    {
        PacketBuffer dat = buffer;
        int id = dat.readInt();
        Vector3 v = Vector3.getNewVector();
        if (pokemob != null)
        {
            int currentMove = pokemob.getMoveIndex();
            if (currentMove == 5) { return; }
            boolean teleport = dat.readBoolean();
            if (teleport)
            {
                PacketDataSync.sendInitPacket(player, "pokecube-data");
            }
            if (pokemob.getAttackCooldown() > 0) return;
            Entity closest = PokecubeMod.core.getEntityProvider().getEntity(player.getEntityWorld(), id, false);
            if (closest != null)
            {
                pokemob.executeMove(closest, v.set(closest), closest.getDistanceToEntity(pokemob.getEntity()));
            }
            else if (buffer.isReadable(24))
            {
                v = Vector3.readFromBuff(buffer);
                pokemob.executeMove(closest, v, (float) v.distToEntity(pokemob.getEntity()));
            }
        }
    }

    static void apply(PacketDoActions message, MessageContext ctx)
    {
        byte channel = message.buffer.readByte();
        final EntityPlayer player;
        if (ctx.side == Side.CLIENT)
        {
            player = PokecubeCore.getPlayer(null);
            int index = message.buffer.readByte();
            GuiAsPokemob.moveIndex = index;
            return;
        }
        player = ctx.getServerHandler().playerEntity;
        if (channel == MOVEUSE)
        {
            message.handleMoveUse(PokePlayer.PROXY.getPokemob(player), player);
        }
        else if (channel == MOVEINDEX)
        {
            IPokemob pokemob = PokePlayer.PROXY.getPokemob(player);
            if (pokemob != null)
            {
                byte index = message.buffer.readByte();
                pokemob.setMoveIndex(index);
                PacketBuffer bufferRet = new PacketBuffer(Unpooled.buffer(2));
                bufferRet.writeByte(MOVEINDEX);
                bufferRet.writeByte(index);
                PacketDoActions ret = new PacketDoActions(bufferRet);
                PokecubePacketHandler.sendToClient(ret, player);
            }

        }
    }
}