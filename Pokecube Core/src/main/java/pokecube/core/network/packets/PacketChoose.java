package pokecube.core.network.packets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.events.StarterEvent;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.StarterInfo;
import pokecube.core.network.PokecubePacketHandler.StarterInfoContainer;
import pokecube.core.utils.PokecubeSerializer;

public class PacketChoose implements IMessage, IMessageHandler<PacketChoose, IMessage>
{
    private static class GuiOpener
    {
        final EntityPlayer player;
        final Integer[]    starters;
        final boolean      starter;
        final boolean      fixed;

        public GuiOpener(EntityPlayer player, Integer[] starters, boolean starter, boolean fixed)
        {
            this.player = player;
            this.starter = starter;
            this.fixed = fixed;
            this.starters = starters;
            if (player.worldObj.isRemote) MinecraftForge.EVENT_BUS.register(this);
        }

        @SideOnly(Side.CLIENT)
        @SubscribeEvent
        public void tick(ClientTickEvent event)
        {
            pokecube.core.client.gui.GuiChooseFirstPokemob.options = starter;
            pokecube.core.client.gui.GuiChooseFirstPokemob.fixed = fixed;
            pokecube.core.client.gui.GuiChooseFirstPokemob.starters = starters;
            player.openGui(PokecubeCore.instance, Config.GUICHOOSEFIRSTPOKEMOB_ID, player.getEntityWorld(), 0, 0, 0);
            MinecraftForge.EVENT_BUS.unregister(this);
        }
    }

    public static final byte OPENGUI = 0;
    public static final byte CHOOSE  = 1;

    private static void handleChooseFirstClient(PacketChoose packet, EntityPlayer player)
    {
        if (player == null)
        {
            new NullPointerException("Null Player while recieving starter packet");
            return;
        }
        boolean openGui = packet.data.getBoolean("C");
        if (openGui)
        {
            boolean special = packet.data.getBoolean("S");
            boolean fixed = packet.data.getBoolean("F");
            int[] toAdd = packet.data.getIntArray("A");
            ArrayList<Integer> starters = new ArrayList<Integer>();
            for (int i = 0; i < toAdd.length; i++)
            {
                starters.add(toAdd[i]);
            }
            new GuiOpener(player, starters.toArray(new Integer[0]), !special, fixed);
        }
        else
        {
            PokecubeSerializer.getInstance().setHasStarter(player, packet.data.getBoolean("H"));
        }
    }

    private static void handleChooseFirstServer(PacketChoose packet, EntityPlayer player)
    {
        int pokedexNb = packet.data.getInteger("N");
        // This is if the player chose to get normal starter, instead of special
        // one.
        boolean fixed = packet.data.getBoolean("F");
        String username = player.getName().toLowerCase();
        if (PokecubeSerializer.getInstance().hasStarter(player)) { return; }

        // Fire pre event to deny starters at all
        StarterEvent.Pre pre = new StarterEvent.Pre(player);
        MinecraftForge.EVENT_BUS.post(pre);
        if (pre.isCanceled()) return;

        List<ItemStack> items = Lists.newArrayList();

        // 10 Pokecubes
        ItemStack pokecubesItemStack = new ItemStack(PokecubeItems.getEmptyCube(0), 10);
        items.add(pokecubesItemStack);
        if (PokecubePacketHandler.giveHealer)
        {
            ItemStack pokecenterItemStack = new ItemStack(PokecubeItems.pokecenter);
            items.add(pokecenterItemStack);
        }
        // Pokedex
        ItemStack pokedexItemStack = new ItemStack(PokecubeItems.pokedex);
        items.add(pokedexItemStack);

        // If they don't actually get the picked starter, then no achievement.
        boolean starterGiven = false;
        if (!(PokecubePacketHandler.specialStarters.containsKey(player.getUniqueID().toString())
                || PokecubePacketHandler.specialStarters.containsKey(username)) || fixed)
        {
            // No Custom Starter. just gets this
            ItemStack pokemobItemstack = PokecubeSerializer.getInstance().starter(pokedexNb, player);
            items.add(pokemobItemstack);
            starterGiven = true;
        }
        else
        {
            StarterInfoContainer info = PokecubePacketHandler.specialStarters.get(player.getUniqueID().toString());
            if (info == null) info = PokecubePacketHandler.specialStarters.get(username);
            StarterInfo[] starter = PokecubePacketHandler.specialStarters.get(username).info;

            for (StarterInfo i : starter)
            {
                if (i == null)
                {
                    if (!starterGiven)
                    {
                        starterGiven = true;
                        ItemStack pokemobItemstack = PokecubeSerializer.getInstance().starter(pokedexNb, player);
                        items.add(pokemobItemstack);
                    }
                }
                else
                {
                    ItemStack start = i.makeStack(player);
                    if (start == null && !starterGiven)
                    {
                        start = i.makeStack(player, pokedexNb);
                        starterGiven = true;
                    }
                    if (start != null) items.add(start);
                }

            }
        }
        // Fire pick event to add new starters or items
        StarterEvent.Pick pick = new StarterEvent.Pick(player, items, pokedexNb);
        MinecraftForge.EVENT_BUS.post(pick);
        if (pick.isCanceled()) return;
        items.clear();
        items.addAll(pick.starterPack);
        player.addStat(PokecubeMod.get1stPokemob, 1);
        if (starterGiven) player.addStat(PokecubeMod.pokemobAchievements.get(pokedexNb), 1);
        for (ItemStack e : items)
        {
            if (e == null || e.getItem() == null) continue;
            player.inventory.addItemStackToInventory(e);
            pokedexNb = PokecubeManager.getPokedexNb(e);
            if (pokedexNb > 0)
            {
                StatsCollector.addCapture(PokecubeManager.itemToPokemob(e, player.getEntityWorld()));
            }
        }
        PokecubeSerializer.getInstance().setHasStarter(player);
        PokecubeSerializer.getInstance().save();

        // Send Packt to client to notifiy about having a starter now.
        packet = new PacketChoose(OPENGUI);
        packet.data.setBoolean("C", false);
        packet.data.setBoolean("H", true);
        PokecubePacketHandler.sendToClient(packet, player);
    }

    public static PacketChoose createOpenPacket(boolean fixed, boolean special, Integer... starters)
    {
        PacketChoose packet = new PacketChoose(OPENGUI);
        packet.data.setBoolean("C", true);
        packet.data.setBoolean("S", special);
        packet.data.setBoolean("F", fixed);
        int[] starts = new int[starters.length];
        for (int i = 0; i < starts.length; i++)
        {
            starts[i] = starters[i];
        }
        packet.data.setIntArray("A", starts);
        return packet;
    }

    byte                  message;
    public NBTTagCompound data = new NBTTagCompound();

    public PacketChoose()
    {
    }

    public PacketChoose(byte message)
    {
        this.message = message;
    }

    @Override
    public IMessage onMessage(final PacketChoose message, final MessageContext ctx)
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

    void processMessage(MessageContext ctx, PacketChoose message)
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
        if (message.message == CHOOSE)
        {
            handleChooseFirstServer(message, player);
        }
        else if (message.message == OPENGUI)
        {
            handleChooseFirstClient(message, player);
        }
    }
}
