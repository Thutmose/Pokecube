package pokecube.core.handlers.playerdata;

import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.StatisticsManager;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.PokecubeMod;
import thut.core.common.handlers.PlayerDataHandler.PlayerData;

/** Player capture/hatch/kill stats */
public class PokecubePlayerStats extends PlayerData
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
