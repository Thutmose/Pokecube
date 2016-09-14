package pokecube.core.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.StatisticsManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.PokecubeSerializer.TeleDest;

public class PlayerDataHandler
{
    private static interface IPlayerData
    {
        String getIdentifier();

        String dataFileName();

        boolean shouldSync();

        void writeToNBT(NBTTagCompound tag);

        void readFromNBT(NBTTagCompound tag);

        void readSync(ByteBuf data);

        void writeSync(ByteBuf data);
    }

    public static abstract class PlayerData implements IPlayerData
    {
        public void readSync(ByteBuf data)
        {
        }

        public void writeSync(ByteBuf data)
        {
        }
    }

    /** Generic data to store for each player, this gives another place besides
     * in the player's entity data to store information. */
    public static class PokecubePlayerCustomData extends PlayerData
    {
        public NBTTagCompound tag = new NBTTagCompound();

        public PokecubePlayerCustomData()
        {
        }

        @Override
        public String getIdentifier()
        {
            return "pokecube-custom";
        }

        @Override
        public String dataFileName()
        {
            return "customData";
        }

        @Override
        public boolean shouldSync()
        {
            return false;
        }

        @Override
        public void writeToNBT(NBTTagCompound tag)
        {
            tag.setTag("data", this.tag);
        }

        @Override
        public void readFromNBT(NBTTagCompound tag)
        {
            this.tag = tag.getCompoundTag("data");
        }
    }

    /** Data which needs to be synced to clients about the player, this is
     * teleport information and starter status. */
    public static class PokecubePlayerData extends PlayerData
    {
        ArrayList<TeleDest> telelocs = Lists.newArrayList();
        int                 teleIndex;
        boolean             hasStarter;

        public PokecubePlayerData()
        {
            super();
        }

        @Override
        public void writeToNBT(NBTTagCompound tag)
        {
            tag.setBoolean("hasStarter", hasStarter);
            tag.setInteger("teleIndex", teleIndex);
            NBTTagList list = new NBTTagList();
            for (TeleDest d : telelocs)
            {
                if (d != null)
                {
                    NBTTagCompound loc = new NBTTagCompound();
                    d.writeToNBT(loc);
                    list.appendTag(loc);
                }
            }
            tag.setTag("telelocs", list);
        }

        @Override
        public void readFromNBT(NBTTagCompound tag)
        {
            hasStarter = tag.getBoolean("hasStarter");
            teleIndex = tag.getInteger("teleIndex");
            NBTBase temp2 = tag.getTag("telelocs");
            telelocs.clear();
            if (temp2 instanceof NBTTagList)
            {
                NBTTagList tagListOptions = (NBTTagList) temp2;
                NBTTagCompound pokemobData2 = null;
                for (int j = 0; j < tagListOptions.tagCount(); j++)
                {
                    pokemobData2 = tagListOptions.getCompoundTagAt(j);
                    TeleDest d = TeleDest.readFromNBT(pokemobData2);
                    telelocs.add(d);
                }
            }
        }

        public int getTeleIndex()
        {
            return teleIndex;
        }

        public void setTeleIndex(int index)
        {
            teleIndex = index;
        }

        public boolean hasStarter()
        {
            return hasStarter;
        }

        public void setHasStarter(boolean has)
        {
            hasStarter = has;
        }

        public List<TeleDest> getTeleDests()
        {
            return telelocs;
        }

        @Override
        public String getIdentifier()
        {
            return "pokecube-data";
        }

        @Override
        public boolean shouldSync()
        {
            return true;
        }

        @Override
        public String dataFileName()
        {
            return "pokecubeData";
        }
    }

    /** Player capture/hatch/kill stats */
    public static class PokecubePlayerStats extends PlayerData
    {
        private Map<PokedexEntry, Integer> hatches;
        private Map<PokedexEntry, Integer> captures;
        private Map<PokedexEntry, Integer> kills;
        private NBTTagCompound             backup;
        protected String                   uuid;

        public PokecubePlayerStats()
        {
            super();
        }

        public void initAchievements(StatisticsManager manager)
        {
            captures = Maps.newHashMap();
            hatches = Maps.newHashMap();
            kills = Maps.newHashMap();
            for (PokedexEntry e : PokecubeCore.catchAchievements.keySet())
            {
                int num = manager.readStat(PokecubeCore.catchAchievements.get(e));
                if (num > 0) captures.put(e, num);
            }
            for (PokedexEntry e : PokecubeCore.hatchAchievements.keySet())
            {
                int num = manager.readStat(PokecubeCore.hatchAchievements.get(e));
                if (num > 0) hatches.put(e, num);
            }
            for (PokedexEntry e : PokecubeCore.killAchievements.keySet())
            {
                int num = manager.readStat(PokecubeCore.killAchievements.get(e));
                if (num > 0) kills.put(e, num);
            }
        }

        public void addCapture(EntityPlayer player, PokedexEntry entry)
        {
            Achievement ach = PokecubeCore.catchAchievements.get(entry);
            if (ach == null)
            {
                System.err.println("missing for " + entry);
                return;
            }
            int num = ((EntityPlayerMP) player).getStatFile().readStat(ach);
            getCaptures(player).put(entry, num + 1);
            player.addStat(ach);
        }

        public void addKill(EntityPlayer player, PokedexEntry entry)
        {
            Achievement ach = PokecubeCore.killAchievements.get(entry);
            if (ach == null)
            {
                System.err.println("missing for " + entry);
                return;
            }
            int num = ((EntityPlayerMP) player).getStatFile().readStat(ach);
            getKills(player).put(entry, num + 1);
            player.addStat(ach);
        }

        public void addHatch(EntityPlayer player, PokedexEntry entry)
        {
            Achievement ach = PokecubeCore.hatchAchievements.get(entry);
            if (ach == null)
            {
                System.err.println("missing for " + entry);
                return;
            }
            int num = ((EntityPlayerMP) player).getStatFile().readStat(ach);
            getHatches(player).put(entry, num + 1);
            player.addStat(ach);
        }

        @Override
        public String getIdentifier()
        {
            return "pokecube-stats";
        }

        @Override
        public boolean shouldSync()
        {
            return false;
        }

        @Override
        public void writeToNBT(NBTTagCompound tag)
        {
            tag = backup;
        }

        @Override
        public void readFromNBT(NBTTagCompound tag)
        {
            EntityPlayer player = PokecubeCore.proxy.getPlayer(uuid);
            if (player == null || tag == null) return;

            if (player.worldObj.isRemote)
            {
                net.minecraft.client.entity.EntityPlayerSP player1 = (net.minecraft.client.entity.EntityPlayerSP) player;
                initAchievements(player1.getStatFileWriter());
                return;
            }
            backup = tag;
            NBTTagCompound temp = tag.getCompoundTag("kills");
            PokedexEntry entry;
            for (String s : temp.getKeySet())
            {
                int num = temp.getInteger(s);
                if (num > 0 && (entry = Database.getEntry(s)) != null)
                {
                    for (int i = 0; i < num; i++)
                        addKill(player, entry);
                }
            }
            temp = tag.getCompoundTag("captures");
            for (String s : temp.getKeySet())
            {
                int num = temp.getInteger(s);
                if (num > 0 && (entry = Database.getEntry(s)) != null)
                {
                    System.out.println(num + " " + entry);
                    for (int i = 0; i < num; i++)
                        addCapture(player, entry);
                }
            }
            temp = tag.getCompoundTag("hatches");
            for (String s : temp.getKeySet())
            {
                int num = temp.getInteger(s);
                if (num > 0 && (entry = Database.getEntry(s)) != null)
                {
                    for (int i = 0; i < num; i++)
                        addHatch(player, entry);
                }
            }
        }

        @Override
        public String dataFileName()
        {
            return "pokecubeStats";
        }

        public Map<PokedexEntry, Integer> getCaptures(EntityPlayer player)
        {
            if (captures == null) initAchievements(((EntityPlayerMP) player).getStatFile());
            return captures;
        }

        public Map<PokedexEntry, Integer> getKills(EntityPlayer player)
        {
            if (kills == null) initAchievements(((EntityPlayerMP) player).getStatFile());
            return kills;
        }

        public Map<PokedexEntry, Integer> getHatches(EntityPlayer player)
        {
            if (hatches == null) initAchievements(((EntityPlayerMP) player).getStatFile());
            return hatches;
        }
    }

    public static class PlayerDataManager
    {
        Map<Class<? extends PlayerData>, PlayerData> data  = Maps.newHashMap();
        Map<String, PlayerData>                      idMap = Maps.newHashMap();
        final String                                 uuid;

        public PlayerDataManager(String uuid)
        {
            this.uuid = uuid;
            for (Class<? extends PlayerData> type : PlayerDataHandler.dataMap)
            {
                try
                {
                    PlayerData toAdd = type.newInstance();
                    data.put(type, toAdd);
                    idMap.put(toAdd.getIdentifier(), toAdd);
                }
                catch (InstantiationException e)
                {
                    e.printStackTrace();
                }
                catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                }
            }
        }

        @SuppressWarnings("unchecked")
        public <T extends PlayerData> T getData(Class<T> type)
        {
            return (T) data.get(type);
        }

        public PlayerData getData(String dataType)
        {
            return idMap.get(dataType);
        }
    }

    public static Set<Class<? extends PlayerData>> dataMap = Sets.newHashSet();

    static
    {
        dataMap.add(PokecubePlayerData.class);
        dataMap.add(PokecubePlayerStats.class);
        dataMap.add(PokecubePlayerCustomData.class);
    }
    private static PlayerDataHandler INSTANCESERVER;
    private static PlayerDataHandler INSTANCECLIENT;

    public static PlayerDataHandler getInstance()
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            return INSTANCECLIENT != null ? INSTANCECLIENT : (INSTANCECLIENT = new PlayerDataHandler());
        }
        else
        {
            return INSTANCESERVER != null ? INSTANCESERVER : (INSTANCESERVER = new PlayerDataHandler());
        }
    }

    public static void clear()
    {
        if (INSTANCECLIENT != null) MinecraftForge.EVENT_BUS.unregister(INSTANCECLIENT);
        if (INSTANCESERVER != null) MinecraftForge.EVENT_BUS.unregister(INSTANCESERVER);
        INSTANCECLIENT = INSTANCESERVER = null;
    }

    public static NBTTagCompound getCustomDataTag(EntityPlayer player)
    {
        PlayerDataManager manager = PlayerDataHandler.getInstance().getPlayerData(player);
        PokecubePlayerCustomData data = manager.getData(PokecubePlayerCustomData.class);
        return data.tag;
    }

    public static NBTTagCompound getCustomDataTag(String player)
    {
        PlayerDataManager manager = PlayerDataHandler.getInstance().getPlayerData(player);
        PokecubePlayerCustomData data = manager.getData(PokecubePlayerCustomData.class);
        return data.tag;
    }

    public static void saveCustomData(EntityPlayer player)
    {
        saveCustomData(player.getCachedUniqueIdString());
    }

    public static void saveCustomData(String cachedUniqueIdString)
    {
        getInstance().save(cachedUniqueIdString, "pokecube-custom");
    }

    private Map<String, PlayerDataManager> data = Maps.newHashMap();

    public PlayerDataHandler()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public PlayerDataManager getPlayerData(EntityPlayer player)
    {
        return getPlayerData(player.getCachedUniqueIdString());
    }

    public PlayerDataManager getPlayerData(UUID uniqueID)
    {
        return getPlayerData(uniqueID.toString());
    }

    public PlayerDataManager getPlayerData(String uuid)
    {
        PlayerDataManager manager = data.get(uuid);
        if (manager == null)
        {
            manager = load(uuid);
        }
        return manager;
    }

    @SubscribeEvent
    public void cleanupOfflineData(WorldEvent.Save event)
    {
        // Whenever overworld saves, check player list for any that are not
        // online, and remove them. This is done here, and not on logoff, as
        // something may have requested the manager for an offline player, which
        // would have loaded it.
        if (event.getWorld().provider.getDimension() == 0)
        {
            Set<String> toUnload = Sets.newHashSet();
            for (String uuid : data.keySet())
            {
                EntityPlayerMP player = event.getWorld().getMinecraftServer().getPlayerList()
                        .getPlayerByUUID(UUID.fromString(uuid));
                if (player == null)
                {
                    toUnload.add(uuid);
                }
            }
            for (String s : toUnload)
            {
                save(s);
                data.remove(s);
            }
        }
    }

    public PlayerDataManager load(String uuid)
    {
        PlayerDataManager manager = new PlayerDataManager(uuid);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) for (PlayerData data : manager.data.values())
        {
            String fileName = data.dataFileName();
            File file = null;
            try
            {
                file = PokecubeSerializer.getFileForUUID(uuid, fileName);
            }
            catch (Exception e)
            {

            }
            if (file != null && file.exists())
            {
                try
                {
                    FileInputStream fileinputstream = new FileInputStream(file);
                    NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(fileinputstream);
                    fileinputstream.close();
                    if (data instanceof PokecubePlayerStats)
                    {
                        ((PokecubePlayerStats) data).uuid = uuid;
                    }

                    data.readFromNBT(nbttagcompound.getCompoundTag("Data"));
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        data.put(uuid, manager);
        return manager;
    }

    public void save(String uuid, String dataType)
    {
        PlayerDataManager manager = data.get(uuid);
        if (manager != null)
        {
            for (PlayerData data : manager.data.values())
            {
                if (!data.getIdentifier().equals(dataType)) continue;
                String fileName = data.dataFileName();
                File file = PokecubeSerializer.getFileForUUID(uuid, fileName);
                if (file != null)
                {
                    NBTTagCompound nbttagcompound = new NBTTagCompound();
                    data.writeToNBT(nbttagcompound);
                    NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                    nbttagcompound1.setTag("Data", nbttagcompound);
                    try
                    {
                        FileOutputStream fileoutputstream = new FileOutputStream(file);
                        CompressedStreamTools.writeCompressed(nbttagcompound1, fileoutputstream);
                        fileoutputstream.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void save(String uuid)
    {
        PlayerDataManager manager = data.get(uuid);
        if (manager != null)
        {
            for (PlayerData data : manager.data.values())
            {
                String fileName = data.dataFileName();
                File file = PokecubeSerializer.getFileForUUID(uuid, fileName);
                if (file != null)
                {
                    NBTTagCompound nbttagcompound = new NBTTagCompound();
                    data.writeToNBT(nbttagcompound);
                    NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                    nbttagcompound1.setTag("Data", nbttagcompound);
                    try
                    {
                        FileOutputStream fileoutputstream = new FileOutputStream(file);
                        CompressedStreamTools.writeCompressed(nbttagcompound1, fileoutputstream);
                        fileoutputstream.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
