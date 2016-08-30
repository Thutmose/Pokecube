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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
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
    }

    public static abstract class PlayerData implements IPlayerData
    {

    }

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

    public static class PokecubePlayerStats extends PlayerData
    {
        public Map<PokedexEntry, Integer> hatches  = Maps.newHashMap();
        public Map<PokedexEntry, Integer> captures = Maps.newHashMap();
        public Map<PokedexEntry, Integer> kills    = Maps.newHashMap();

        public PokecubePlayerStats()
        {
            super();
        }

        @Override
        public String getIdentifier()
        {
            return "pokecube-stats";
        }

        @Override
        public boolean shouldSync()
        {
            return true;
        }

        @Override
        public void writeToNBT(NBTTagCompound tag)
        {
            NBTTagCompound tag1 = new NBTTagCompound();
            for (PokedexEntry dbe : kills.keySet())
            {
                tag.setInteger(dbe.getName(), kills.get(dbe));
            }
            tag.setTag("kills", tag1);

            tag1 = new NBTTagCompound();
            for (PokedexEntry dbe : captures.keySet())
            {
                tag1.setInteger(dbe.getName(), captures.get(dbe));
            }
            tag.setTag("captures", tag1);

            tag1 = new NBTTagCompound();
            for (PokedexEntry dbe : hatches.keySet())
            {
                tag1.setInteger(dbe.getName(), hatches.get(dbe));
            }
            tag.setTag("hatches", tag1);
        }

        @Override
        public void readFromNBT(NBTTagCompound tag)
        {
            kills.clear();
            captures.clear();
            hatches.clear();
            NBTTagCompound temp = tag.getCompoundTag("kills");
            for (String s : temp.getKeySet())
            {
                int num = temp.getInteger(s);
                if (num > 0 && Database.getEntry(s) != null) kills.put(Database.getEntry(s), num);
            }
            temp = tag.getCompoundTag("captures");
            for (String s : temp.getKeySet())
            {
                int num = temp.getInteger(s);
                if (num > 0 && Database.getEntry(s) != null) captures.put(Database.getEntry(s), num);
            }
            temp = tag.getCompoundTag("hatches");
            for (String s : temp.getKeySet())
            {
                int num = temp.getInteger(s);
                if (num > 0 && Database.getEntry(s) != null) hatches.put(Database.getEntry(s), num);
            }
        }

        @Override
        public String dataFileName()
        {
            return "pokecubeStats";
        }
    }

    public static class PlayerDataManager
    {
        Map<String, PlayerData> data = Maps.newHashMap();
        final String            uuid;

        public PlayerDataManager(String uuid)
        {
            this.uuid = uuid;
            for (Class<? extends PlayerData> type : PlayerDataHandler.dataMap)
            {
                try
                {
                    PlayerData toAdd = type.newInstance();
                    data.put(toAdd.getIdentifier(), toAdd);
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
        public <T> T getData(String identifier, Class<T> type)
        {
            return (T) data.get(identifier);
        }
    }

    public static Set<Class<? extends PlayerData>> dataMap = Sets.newHashSet();

    static
    {
        dataMap.add(PokecubePlayerData.class);
        dataMap.add(PokecubePlayerStats.class);
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
        INSTANCECLIENT = INSTANCESERVER = null;
    }

    private Map<String, PlayerDataManager> data = Maps.newHashMap();

    public PlayerDataHandler()
    {
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

    public PlayerDataManager load(String uuid)
    {
        PlayerDataManager manager = new PlayerDataManager(uuid);
        for (PlayerData data : manager.data.values())
        {
            String fileName = data.dataFileName();
            File file = PokecubeSerializer.getFileForUUID(uuid, fileName);
            if (file != null && file.exists())
            {
                try
                {
                    FileInputStream fileinputstream = new FileInputStream(file);
                    NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(fileinputstream);
                    fileinputstream.close();
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
