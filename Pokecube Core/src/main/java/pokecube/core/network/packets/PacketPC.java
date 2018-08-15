package pokecube.core.network.packets;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.pc.ContainerPC;
import pokecube.core.blocks.pc.InventoryPC;
import pokecube.core.interfaces.PokecubeMod;

public class PacketPC implements IMessage, IMessageHandler<PacketPC, IMessage>
{
    public static final byte SETPAGE    = 0;
    public static final byte RENAME     = 1;
    public static final byte ONOPEN     = 2;
    public static final byte RELEASE    = 3;
    public static final byte TOGGLEAUTO = 4;
    public static final byte BIND       = 5;

    public static void sendInitialSyncMessage(EntityPlayer sendTo)
    {
        InventoryPC inv = InventoryPC.getPC(sendTo);
        PacketPC packet = new PacketPC(PacketPC.ONOPEN);
        packet.data.setInteger("N", inv.boxes.length);
        packet.data.setBoolean("A", inv.autoToPC);
        packet.data.setBoolean("O", inv.seenOwner);
        packet.data.setInteger("C", inv.getPage());
        for (int i = 0; i < inv.boxes.length; i++)
        {
            packet.data.setString("N" + i, inv.boxes[i]);
        }
        PokecubeMod.packetPipeline.sendTo(packet, (EntityPlayerMP) sendTo);
    }

    byte                  message;
    public NBTTagCompound data = new NBTTagCompound();

    public PacketPC()
    {
    }

    public PacketPC(byte message)
    {
        this.message = message;
    }

    @Override
    public IMessage onMessage(final PacketPC message, final MessageContext ctx)
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
        message = buf.readByte();
        PacketBuffer buffer = new PacketBuffer(buf);
        try
        {
            data = buffer.readCompoundTag();
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
        buffer.writeCompoundTag(data);
    }

    void processMessage(MessageContext ctx, PacketPC message)
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
        ContainerPC container = null;
        if (player.openContainer instanceof ContainerPC) container = (ContainerPC) player.openContainer;
        if (message.message == SETPAGE)
        {
            if (container != null)
            {
                container.gotoInventoryPage(message.data.getInteger("P"));
            }
        }
        if (message.message == RENAME)
        {
            if (container != null)
            {
                String name = message.data.getString("N");
                container.changeName(name);
            }
        }
        if (message.message == ONOPEN)
        {
            InventoryPC.blank = new InventoryPC(InventoryPC.defaultId);
            InventoryPC pc = InventoryPC.getPC(player);
            pc.seenOwner = message.data.getBoolean("O");
            pc.autoToPC = message.data.getBoolean("A");
            if (message.data.hasKey("C")) pc.setPage(message.data.getInteger("C"));
            if (message.data.hasKey("N"))
            {
                int num = message.data.getInteger("N");
                pc.boxes = new String[num];
                for (int i = 0; i < pc.boxes.length; i++)
                {
                    pc.boxes[i] = message.data.getString("N" + i);
                }
            }
        }
        if (message.message == RELEASE)
        {
            boolean toggle = message.data.getBoolean("T");
            if (toggle)
            {
                container.setRelease(message.data.getBoolean("R"));
            }
            else
            {
                int page = message.data.getInteger("page");
                InventoryPC pc = InventoryPC.getPC(player);
                for (int i = 0; i < 54; i++)
                {
                    if (message.data.getBoolean("val" + i))
                    {
                        int j = i + page * 54;
                        pc.setInventorySlotContents(j, ItemStack.EMPTY);
                    }
                }
            }
        }
        if (message.message == TOGGLEAUTO)
        {
            InventoryPC pc = InventoryPC.getPC(player);
            pc.autoToPC = message.data.getBoolean("A");
        }
        if (message.message == BIND)
        {
            if (container != null && container.pcTile != null)
            {
                boolean owned = message.data.getBoolean("O");
                if (PokecubeMod.debug) PokecubeMod.log("Bind PC Packet: " + owned + " " + player);
                if (owned)
                {
                    container.pcTile.toggleBound();
                }
                else
                {
                    container.pcTile.setBoundOwner(player);
                }
            }
        }
    }

}
