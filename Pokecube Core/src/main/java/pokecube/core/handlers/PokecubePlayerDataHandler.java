package pokecube.core.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.StatisticsManager;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokecubeSerializer.TeleDest;
import thut.core.common.handlers.PlayerDataHandler;

public class PokecubePlayerDataHandler extends PlayerDataHandler
{

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
        protected UUID                     uuid;

        public PokecubePlayerStats()
        {
            super();
        }

        public void initAchievements(StatisticsManager manager)
        {
            captures = Maps.newHashMap();
            hatches = Maps.newHashMap();
            kills = Maps.newHashMap();
            for (PokedexEntry e : PokecubeMod.catchAchievements.keySet())
            {
                int num = manager.readStat(PokecubeMod.catchAchievements.get(e));
                if (num > 0) captures.put(e, num);
            }
            for (PokedexEntry e : PokecubeMod.hatchAchievements.keySet())
            {
                int num = manager.readStat(PokecubeMod.hatchAchievements.get(e));
                if (num > 0) hatches.put(e, num);
            }
            for (PokedexEntry e : PokecubeMod.killAchievements.keySet())
            {
                int num = manager.readStat(PokecubeMod.killAchievements.get(e));
                if (num > 0) kills.put(e, num);
            }
        }

        public void addCapture(UUID player, PokedexEntry entry)
        {
            Achievement ach = PokecubeMod.catchAchievements.get(entry);
            if (ach == null)
            {
                System.err.println("missing for " + entry);
                return;
            }
            int num = getManager(player).readStat(ach);
            getCaptures(player).put(entry, num + 1);
            getPlayer(player).addStat(ach);
        }

        public void addKill(UUID player, PokedexEntry entry)
        {
            Achievement ach = PokecubeMod.killAchievements.get(entry);
            if (ach == null)
            {
                System.err.println("missing for " + entry);
                return;
            }
            int num = getManager(player).readStat(ach);
            getKills(player).put(entry, num + 1);
            getPlayer(player).addStat(ach);
        }

        public void addHatch(UUID player, PokedexEntry entry)
        {
            Achievement ach = PokecubeMod.hatchAchievements.get(entry);
            if (ach == null) { return; }
            int num = getManager(player).readStat(ach);
            getHatches(player).put(entry, num + 1);
            getPlayer(player).addStat(ach);
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
                initAchievements(getManager(uuid));
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
                        addKill(uuid, entry);
                }
            }
            temp = tag.getCompoundTag("captures");
            for (String s : temp.getKeySet())
            {
                int num = temp.getInteger(s);
                if (num > 0 && (entry = Database.getEntry(s)) != null)
                {
                    for (int i = 0; i < num; i++)
                        addCapture(uuid, entry);
                }
            }
            temp = tag.getCompoundTag("hatches");
            for (String s : temp.getKeySet())
            {
                int num = temp.getInteger(s);
                if (num > 0 && (entry = Database.getEntry(s)) != null)
                {
                    for (int i = 0; i < num; i++)
                        addHatch(uuid, entry);
                }
            }
        }

        @Override
        public String dataFileName()
        {
            return "pokecubeStats";
        }

        public Map<PokedexEntry, Integer> getCaptures(UUID player)
        {
            if (captures == null) initAchievements(getManager(player));
            return captures;
        }

        public Map<PokedexEntry, Integer> getKills(UUID player)
        {
            if (kills == null) initAchievements(getManager(player));
            return kills;
        }

        public Map<PokedexEntry, Integer> getHatches(UUID player)
        {
            if (hatches == null) initAchievements(getManager(player));
            return hatches;
        }

        private StatisticsManager getManager(UUID player)
        {
            return PokecubeCore.proxy.getManager(player);
        }

        private EntityPlayer getPlayer(UUID player)
        {
            return PokecubeCore.proxy.getPlayer(player);
        }
    }

    public static NBTTagCompound getCustomDataTag(EntityPlayer player)
    {
        PlayerDataManager manager = PokecubePlayerDataHandler.getInstance().getPlayerData(player);
        PokecubePlayerCustomData data = manager.getData(PokecubePlayerCustomData.class);
        return data.tag;
    }

    public static NBTTagCompound getCustomDataTag(String player)
    {
        PlayerDataManager manager = PokecubePlayerDataHandler.getInstance().getPlayerData(player);
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
}
