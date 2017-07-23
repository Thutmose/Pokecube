package pokecube.core.network.pokemobs;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.network.PokecubePacketHandler;

public class PacketPokemobMessage implements IMessage, IMessageHandler<PacketPokemobMessage, IMessage>
{
    public static void sendMessage(EntityPlayer sendTo, int senderId, ITextComponent message)
    {
        PacketPokemobMessage toSend = new PacketPokemobMessage(message, senderId);
        PokecubePacketHandler.sendToClient(toSend, sendTo);
    }

    ITextComponent message;
    int            senderId;

    public PacketPokemobMessage()
    {
    }

    public PacketPokemobMessage(ITextComponent message, int senderId)
    {
        this.message = message;
        this.senderId = senderId;
    }

    @Override
    public IMessage onMessage(final PacketPokemobMessage message, final MessageContext ctx)
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
        PacketBuffer buffer = new PacketBuffer(buf);
        senderId = buffer.readInt();
        try
        {
            message = buffer.readTextComponent();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeInt(senderId);
        buffer.writeTextComponent(message);
    }

    void processMessage(MessageContext ctx, PacketPokemobMessage message)
    {
        EntityPlayer player = PokecubeCore.getPlayer(null);
        int id = message.senderId;
        ITextComponent component = message.message;
        Entity e = PokecubeMod.core.getEntityProvider().getEntity(player.getEntityWorld(), id, false);
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(e);
        if (pokemob != null)
        {
            pokemob.displayMessageToOwner(component);
        }
        else if (e == player)
        {
            pokecube.core.client.gui.GuiInfoMessages.addMessage(component);
        }
    }

}
