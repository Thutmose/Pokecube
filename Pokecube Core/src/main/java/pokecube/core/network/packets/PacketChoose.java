package pokecube.core.network.packets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
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
import pokecube.core.contributors.Contributor;
import pokecube.core.contributors.ContributorManager;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.events.StarterEvent;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.StarterInfo;
import pokecube.core.network.PokecubePacketHandler.StarterInfoContainer;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.Tools;

public class PacketChoose implements IMessage, IMessageHandler<PacketChoose, IMessage>
{
    private static class GuiOpener
    {
        final EntityPlayer   player;
        final PokedexEntry[] starters;
        final boolean        special;
        final boolean        pick;

        public GuiOpener(EntityPlayer player, PokedexEntry[] starters, boolean special, boolean pick)
        {
            this.player = player;
            this.special = special;
            this.starters = starters;
            this.pick = pick;
            if (player.getEntityWorld().isRemote) MinecraftForge.EVENT_BUS.register(this);
        }

        @SideOnly(Side.CLIENT)
        @SubscribeEvent
        public void tick(ClientTickEvent event)
        {
            pokecube.core.client.gui.GuiChooseFirstPokemob.special = special;
            pokecube.core.client.gui.GuiChooseFirstPokemob.pick = pick;
            pokecube.core.client.gui.GuiChooseFirstPokemob.starters = starters;
            player.openGui(PokecubeCore.instance, Config.GUICHOOSEFIRSTPOKEMOB_ID, player.getEntityWorld(), 0, 0, 0);
            MinecraftForge.EVENT_BUS.unregister(this);
        }
    }

    public static final byte OPENGUI = 0;
    public static final byte CHOOSE  = 1;

    private static void handleChooseFirstClient(PacketChoose packet, EntityPlayer player)
    {
        if (player == null) { throw new NullPointerException("Null Player while recieving starter packet"); }
        boolean openGui = packet.data.getBoolean("C");
        if (openGui)
        {
            boolean special = packet.data.getBoolean("S");
            boolean pick = packet.data.getBoolean("P");
            ArrayList<PokedexEntry> starters = new ArrayList<PokedexEntry>();
            NBTTagList starterList = packet.data.getTagList("L", 8);
            for (int i = 0; i < starterList.tagCount(); i++)
            {
                PokedexEntry entry = Database.getEntry(starterList.getStringTagAt(i));
                if (entry != null) starters.add(entry);
            }
            new GuiOpener(player, starters.toArray(new PokedexEntry[0]), special, pick);
        }
        else
        {
            PokecubeSerializer.getInstance().setHasStarter(player, packet.data.getBoolean("H"));
        }
    }

    private static void handleChooseFirstServer(PacketChoose packet, EntityPlayer player)
    {
        /** Ignore this packet if the player already has a starter. */
        if (PokecubeSerializer.getInstance().hasStarter(player)) { return; }
        // Fire pre event to deny starters from being processed.
        StarterEvent.Pre pre = new StarterEvent.Pre(player);
        MinecraftForge.EVENT_BUS.post(pre);
        if (pre.isCanceled()) return;
        GameProfile profile = player.getGameProfile();
        String entryName = packet.data.getString("N");
        PokedexEntry entry = Database.getEntry(entryName);
        // Did they also get contributor stuff.
        boolean gotSpecial = packet.data.getBoolean("S");
        Contributor contrib = ContributorManager.instance().getContributor(profile);
        List<ItemStack> items = Lists.newArrayList();
        // Copy main list from database.
        for (ItemStack stack : Database.starterPack)
        {
            items.add(stack.copy());
        }
        StarterInfoContainer info = PokecubePacketHandler.specialStarters.get(contrib);
        if (!gotSpecial || info == null)
        {
            // No Custom Starter. just gets this
            ItemStack pokemobItemstack = PokecubeSerializer.getInstance().starter(entry, player);
            items.add(pokemobItemstack);
        }
        else
        {
            StarterInfo[] starter = info.info;
            /** Check custom picks. */
            for (StarterInfo i : starter)
            {
                ItemStack stack;
                if (!(stack = i.makeStack(player)).isEmpty())
                {
                    items.add(stack);
                }
            }
            /** If also picked, add that in too. */
            if (entry != null)
            {
                ItemStack pokemobItemstack = PokecubeSerializer.getInstance().starter(entry, player);
                items.add(pokemobItemstack);
            }
        }
        // Fire pick event to add new starters or items
        StarterEvent.Pick pick = new StarterEvent.Pick(player, items, entry);
        MinecraftForge.EVENT_BUS.post(pick);
        /** If canceled, assume items were not needed, or canceller handled
         * giving them. */
        if (pick.isCanceled()) return;
        /** Update itemlist from the pick event. */
        items.clear();
        items.addAll(pick.starterPack);
        for (ItemStack e : items)
        {
            if (e.isEmpty()) continue;
            /** Run this before tools.give, as that invalidates the
             * itemstack. */
            if (PokecubeManager.isFilled(e))
            {
                IPokemob pokemob = PokecubeManager.itemToPokemob(e, player.getEntityWorld());
                /** First pokemob advancement on getting starter. */
                if (pokemob != null && pokemob.getPokedexEntry() == entry)
                {
                    StatsCollector.addCapture(pokemob);
                }
            }
            Tools.giveItem(player, e);
        }
        /** Set starter status to prevent player getting more starters. */
        PokecubeSerializer.getInstance().setHasStarter(player);
        PokecubeSerializer.getInstance().save();

        // Send Packt to client to notifiy about having a starter now.
        packet = new PacketChoose(OPENGUI);
        packet.data.setBoolean("C", false);
        packet.data.setBoolean("H", true);
        PokecubePacketHandler.sendToClient(packet, player);
    }

    public static boolean canPick(GameProfile profile)
    {
        Contributor contrib = ContributorManager.instance().getContributor(profile);
        StarterInfoContainer info = PokecubePacketHandler.specialStarters.get(contrib);
        if (info != null)
        {
            for (StarterInfo i : info.info)
            {
                if (i == null || i.name == null) return true;
            }
        }
        return false;
    }

    public static PacketChoose createOpenPacket(boolean special, boolean pick, PokedexEntry... starts)
    {
        PacketChoose packet = new PacketChoose(OPENGUI);
        packet.data.setBoolean("C", true);
        packet.data.setBoolean("S", special);
        packet.data.setBoolean("P", pick);
        NBTTagList starters = new NBTTagList();
        for (PokedexEntry e : starts)
        {
            starters.appendTag(new NBTTagString(e.getTrimmedName()));
        }
        packet.data.setTag("L", starters);
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

    void processMessage(MessageContext ctx, PacketChoose message)
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
