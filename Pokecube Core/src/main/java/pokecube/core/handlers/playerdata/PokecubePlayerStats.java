package pokecube.core.handlers.playerdata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.util.ResourceLocation;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.playerdata.advancements.AdvancementGenerator;
import pokecube.core.handlers.playerdata.advancements.BadgeGen;
import pokecube.core.handlers.playerdata.advancements.triggers.Triggers;
import pokecube.core.utils.PokeType;
import thut.core.common.handlers.PlayerDataHandler.PlayerData;

/** Player capture/hatch/kill stats */
public class PokecubePlayerStats extends PlayerData
{
    private Map<PokedexEntry, Integer> hatches;
    private Map<PokedexEntry, Integer> captures;
    private Map<PokedexEntry, Integer> kills;
    protected boolean                  hasFirst = false;

    public PokecubePlayerStats()
    {
        super();
    }

    public void initAchievements(StatisticsManager manager)
    {
        captures = Maps.newHashMap();
        hatches = Maps.newHashMap();
        kills = Maps.newHashMap();
    }

    public void addCapture(UUID player, PokedexEntry entry)
    {
        int num = getCaptures(player).get(entry) == null ? 0 : getCaptures(player).get(entry);
        getCaptures(player).put(entry, num + 1);
    }

    public void addKill(UUID player, PokedexEntry entry)
    {
        int num = getKills(player).get(entry) == null ? 0 : getKills(player).get(entry);
        getKills(player).put(entry, num + 1);
    }

    public void addHatch(UUID player, PokedexEntry entry)
    {
        int num = getHatches(player).get(entry) == null ? 0 : getHatches(player).get(entry);
        getHatches(player).put(entry, num + 1);
    }

    public void setHasFirst(EntityPlayer player)
    {
        hasFirst = true;
        Triggers.FIRSTPOKEMOB.trigger((EntityPlayerMP) player);
    }

    public boolean hasFirst()
    {
        return hasFirst;
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
    public void writeToNBT(NBTTagCompound tag_)
    {
        NBTTagCompound tag = new NBTTagCompound();
        for (PokedexEntry e : getKills(null).keySet())
        {
            tag.setInteger(e.getName(), getKills(null).get(e));
        }
        tag_.setTag("kills", tag);
        tag = new NBTTagCompound();
        for (PokedexEntry e : getCaptures(null).keySet())
        {
            tag.setInteger(e.getName(), getCaptures(null).get(e));
        }
        tag_.setTag("captures", tag);
        tag = new NBTTagCompound();
        for (PokedexEntry e : getHatches(null).keySet())
        {
            tag.setInteger(e.getName(), getHatches(null).get(e));
        }
        tag_.setTag("hatches", tag);
        tag_.setBoolean("F", hasFirst);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        NBTTagCompound temp = tag.getCompoundTag("kills");
        PokedexEntry entry;
        hasFirst = tag.getBoolean("F");
        for (String s : temp.getKeySet())
        {
            int num = temp.getInteger(s);
            if (num > 0 && (entry = Database.getEntry(s)) != null)
            {
                for (int i = 0; i < num; i++)
                    addKill(null, entry);
            }
        }
        temp = tag.getCompoundTag("captures");
        for (String s : temp.getKeySet())
        {
            int num = temp.getInteger(s);
            if (num > 0 && (entry = Database.getEntry(s)) != null)
            {
                for (int i = 0; i < num; i++)
                    addCapture(null, entry);
            }
        }
        temp = tag.getCompoundTag("hatches");
        for (String s : temp.getKeySet())
        {
            int num = temp.getInteger(s);
            if (num > 0 && (entry = Database.getEntry(s)) != null)
            {
                for (int i = 0; i < num; i++)
                    addHatch(null, entry);
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

    public static void initAchievements()
    {

        // Comment in this stuff if you want to generate get item achivements
        // for different types.
        for (PokeType type : PokeType.values())
        {
            if (type != PokeType.unknown)
            {
                String json = BadgeGen.makeJson(type.name, "pokecube_adventures:trainers/root");
                File dir = new File("./mods/pokecube/assets/pokecube_adventures/advancements/trainers/");
                if (!dir.exists()) dir.mkdirs();
                File file = new File(dir, "get_" + type.name + "_badge.json");
                try
                {
                    FileWriter write = new FileWriter(file);
                    write.write(json);
                    write.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /** Comment these out to re-generate advancements. */
    public static void registerAchievements(PokedexEntry entry)
    {
        // make(entry, "catch", "pokecube_mobs:capture/get_first_pokemob",
        // "capture");
        // make(entry, "kill", "pokecube_mobs:kill/root", "kill");
        // make(entry, "hatch", "pokecube_mobs:hatch/root", "hatch");
    }

    protected static void make(PokedexEntry entry, String id, String parent, String path)
    {
        ResourceLocation key = new ResourceLocation(entry.getModId(), id + "_" + entry.getName());
        String json = AdvancementGenerator.makeJson(entry, id, parent);
        File dir = new File("./mods/pokecube/assets/pokecube_mobs/advancements/" + path + "/");
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, key.getResourcePath() + ".json");
        try
        {
            FileWriter write = new FileWriter(file);
            write.write(json);
            write.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void initMap()
    {
    }

    public static void reset()
    {
    }
}
