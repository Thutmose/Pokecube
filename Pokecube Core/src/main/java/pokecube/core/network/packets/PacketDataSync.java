package pokecube.core.network.packets;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.GuiInfoMessages;
import pokecube.core.handlers.Config;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.SyncConfig;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokecubeSerializer;
import thut.core.common.config.Configure;
import thut.core.common.handlers.PlayerDataHandler.PlayerData;
import thut.core.common.handlers.PlayerDataHandler.PlayerDataManager;

public class PacketDataSync implements IMessage, IMessageHandler<PacketDataSync, IMessage>
{
    public NBTTagCompound data = new NBTTagCompound();

    public static void sendInitPacket(EntityPlayer player, String dataType)
    {
        PlayerDataManager manager = PokecubePlayerDataHandler.getInstance().getPlayerData(player);
        PlayerData data = manager.getData(dataType);
        PacketDataSync packet = new PacketDataSync();
        packet.data.setString("type", dataType);
        NBTTagCompound tag1 = new NBTTagCompound();
        data.writeToNBT(tag1);
        packet.data.setTag("data", tag1);
        PokecubeMod.packetPipeline.sendTo(packet, (EntityPlayerMP) player);
        PokecubePlayerDataHandler.getInstance().save(player.getCachedUniqueIdString());
    }

    public static void sendInitHandshake(EntityPlayer player)
    {
        PacketDataSync packet = new PacketDataSync();
        packet.data.setBoolean("I", true);
        if (FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
            packet.data.setTag("C", writeConfigs());
        PokecubeMod.packetPipeline.sendTo(packet, (EntityPlayerMP) player);
    }

    private static NBTTagCompound writeConfigs()
    {
        NBTTagCompound ret = new NBTTagCompound();
        Config defaults = PokecubeCore.instance.getConfig();
        NBTTagCompound longs = new NBTTagCompound();
        NBTTagCompound ints = new NBTTagCompound();
        NBTTagCompound bools = new NBTTagCompound();
        NBTTagCompound floats = new NBTTagCompound();
        NBTTagCompound doubles = new NBTTagCompound();
        NBTTagCompound strings = new NBTTagCompound();
        NBTTagCompound intarrs = new NBTTagCompound();
        NBTTagCompound stringarrs = new NBTTagCompound();
        for (Field f : Config.class.getDeclaredFields())
        {
            SyncConfig c = f.getAnnotation(SyncConfig.class);
            Configure conf = f.getAnnotation(Configure.class);
            /** client stuff doesn't need to by synced, clients will use the
             * dummy config while on servers. */
            if (conf != null && conf.category().equals(Config.client)) continue;
            if (c != null)
            {
                try
                {
                    f.setAccessible(true);
                    if ((f.getType() == Long.TYPE) || (f.getType() == Long.class))
                    {
                        long defaultValue = f.getLong(defaults);
                        longs.setTag(f.getName(), new NBTTagLong(defaultValue));
                    }
                    else if (f.getType() == String.class)
                    {
                        String defaultValue = (String) f.get(defaults);
                        strings.setTag(f.getName(), new NBTTagString(defaultValue));
                    }
                    else if ((f.getType() == Integer.TYPE) || (f.getType() == Integer.class))
                    {
                        int defaultValue = f.getInt(defaults);
                        ints.setTag(f.getName(), new NBTTagInt(defaultValue));
                    }
                    else if ((f.getType() == Float.TYPE) || (f.getType() == Float.class))
                    {
                        float defaultValue = f.getFloat(defaults);
                        floats.setTag(f.getName(), new NBTTagFloat(defaultValue));
                    }
                    else if ((f.getType() == Double.TYPE) || (f.getType() == Double.class))
                    {
                        double defaultValue = f.getDouble(defaults);
                        doubles.setTag(f.getName(), new NBTTagDouble(defaultValue));
                    }
                    else if ((f.getType() == Boolean.TYPE) || (f.getType() == Boolean.class))
                    {
                        boolean defaultValue = f.getBoolean(defaults);
                        bools.setTag(f.getName(), new NBTTagByte((byte) (defaultValue ? 1 : 0)));
                    }
                    else
                    {
                        Object o = f.get(defaults);
                        if (o instanceof String[])
                        {
                            String[] defaultValue = (String[]) o;
                            NBTTagList arr = new NBTTagList();
                            for (String s : defaultValue)
                                arr.appendTag(new NBTTagString(s));
                            stringarrs.setTag(f.getName(), arr);
                        }
                        else if (o instanceof int[])
                        {
                            int[] defaultValue = (int[]) o;
                            intarrs.setTag(f.getName(), new NBTTagIntArray(defaultValue));
                        }
                        else System.err.println("Unknown Type " + f.getType() + " " + f.getName() + " " + o.getClass());
                    }
                }
                catch (IllegalArgumentException e)
                {
                    e.printStackTrace();
                }
                catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                }
            }
        }
        if (!longs.hasNoTags()) ret.setTag("L", longs);
        if (!ints.hasNoTags()) ret.setTag("I", ints);
        if (!bools.hasNoTags()) ret.setTag("B", bools);
        if (!floats.hasNoTags()) ret.setTag("F", floats);
        if (!doubles.hasNoTags()) ret.setTag("D", doubles);
        if (!strings.hasNoTags()) ret.setTag("S", strings);
        if (!intarrs.hasNoTags()) ret.setTag("A", intarrs);
        if (!stringarrs.hasNoTags()) ret.setTag("R", stringarrs);
        return ret;
    }

    public PacketDataSync()
    {
    }

    @Override
    public IMessage onMessage(final PacketDataSync message, final MessageContext ctx)
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
        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeCompoundTag(data);
    }

    void processMessage(MessageContext ctx, PacketDataSync message)
    {
        EntityPlayer player;
        if (ctx.side == Side.CLIENT)
        {
            player = PokecubeCore.getPlayer(null);
            if (message.data.getBoolean("I"))
            {
                PokecubeSerializer.getInstance().clearInstance();
                GuiInfoMessages.clear();
                try
                {
                    if (FMLCommonHandler.instance().getMinecraftServerInstance() == null)
                        syncConfigs(message.data.getCompoundTag("C"));
                }
                catch (Exception e)
                {
                    PokecubeMod.log(Level.WARNING, "Error with config Sync: " + message.data.getCompoundTag("C"), e);
                }
            }
            else
            {
                PlayerDataManager manager = PokecubePlayerDataHandler.getInstance().getPlayerData(player);
                manager.getData(message.data.getString("type")).readFromNBT(message.data.getCompoundTag("data"));
            }
        }
    }

    private void syncConfigs(NBTTagCompound tag) throws Exception
    {
        Config defaults = PokecubeCore.instance.getConfig();
        Field f;
        if (tag.hasKey("L"))
        {
            NBTTagCompound longs = tag.getCompoundTag("L");
            for (String s : longs.getKeySet())
            {
                try
                {
                    long l = longs.getLong(s);
                    f = defaults.getClass().getDeclaredField(s);
                    f.setAccessible(true);
                    defaults.updateField(f, l + "");
                }
                catch (Exception e)
                {
                    PokecubeMod.log(Level.WARNING, s, e);
                }
            }
        }
        if (tag.hasKey("I"))
        {
            NBTTagCompound ints = tag.getCompoundTag("I");
            for (String s : ints.getKeySet())
            {
                try
                {
                    int l = ints.getInteger(s);
                    f = defaults.getClass().getDeclaredField(s);
                    f.setAccessible(true);
                    defaults.updateField(f, l + "");
                }
                catch (Exception e)
                {
                    PokecubeMod.log(Level.WARNING, s, e);
                }
            }
        }
        if (tag.hasKey("B"))
        {
            NBTTagCompound bools = tag.getCompoundTag("B");
            for (String s : bools.getKeySet())
            {
                try
                {
                    boolean l = bools.getByte(s) != 0;
                    f = defaults.getClass().getDeclaredField(s);
                    f.setAccessible(true);
                    defaults.updateField(f, l + "");
                }
                catch (Exception e)
                {
                    PokecubeMod.log(Level.WARNING, s, e);
                }
            }
        }
        if (tag.hasKey("F"))
        {
            NBTTagCompound floats = tag.getCompoundTag("F");
            for (String s : floats.getKeySet())
            {
                try
                {
                    float l = floats.getFloat(s);
                    f = defaults.getClass().getDeclaredField(s);
                    f.setAccessible(true);
                    defaults.updateField(f, l + "");
                }
                catch (Exception e)
                {
                    PokecubeMod.log(Level.WARNING, s, e);
                }
            }
        }
        if (tag.hasKey("D"))
        {
            NBTTagCompound doubles = tag.getCompoundTag("D");
            for (String s : doubles.getKeySet())
            {
                try
                {
                    double l = doubles.getDouble(s);
                    f = defaults.getClass().getDeclaredField(s);
                    f.setAccessible(true);
                    defaults.updateField(f, l + "");
                }
                catch (Exception e)
                {
                    PokecubeMod.log(Level.WARNING, s, e);
                }
            }
        }
        if (tag.hasKey("S"))
        {
            NBTTagCompound strings = tag.getCompoundTag("S");
            for (String s : strings.getKeySet())
            {
                try
                {
                    String l = strings.getString(s);
                    f = defaults.getClass().getDeclaredField(s);
                    f.setAccessible(true);
                    defaults.updateField(f, l + "");
                }
                catch (Exception e)
                {
                    PokecubeMod.log(Level.WARNING, s, e);
                }
            }
        }
        if (tag.hasKey("A"))
        {
            NBTTagCompound intarrs = tag.getCompoundTag("A");
            for (String s : intarrs.getKeySet())
            {
                try
                {
                    int[] l = intarrs.getIntArray(s);
                    f = defaults.getClass().getDeclaredField(s);
                    f.setAccessible(true);
                    defaults.updateField(f, l);
                }
                catch (Exception e)
                {
                    PokecubeMod.log(Level.WARNING, s, e);
                }
            }
        }
        if (tag.hasKey("R"))
        {
            NBTTagCompound stringarrs = tag.getCompoundTag("R");
            for (String s : stringarrs.getKeySet())
            {
                try
                {
                    NBTTagList list = (NBTTagList) stringarrs.getTag(s);
                    List<String> vars = Lists.newArrayList();
                    for (int i = 0; i < list.tagCount(); i++)
                        vars.add(list.getStringTagAt(i));
                    String[] arr = vars.toArray(new String[0]);
                    f = defaults.getClass().getDeclaredField(s);
                    f.setAccessible(true);
                    defaults.updateField(f, arr);
                }
                catch (Exception e)
                {
                    PokecubeMod.log(Level.WARNING, s, e);
                }
            }
        }
    }
}
