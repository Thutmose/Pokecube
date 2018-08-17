package pokecube.core.handlers.playerdata;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.playerdata.advancements.triggers.Triggers;
import pokecube.core.interfaces.IPokemob;
import thut.core.common.handlers.PlayerDataHandler.PlayerData;

/** Player capture/hatch/kill stats */
public class PokecubePlayerStats extends PlayerData
{
    private Map<PokedexEntry, Integer> hatches;
    private Map<PokedexEntry, Integer> captures;
    private Map<PokedexEntry, Integer> kills;
    private Set<PokedexEntry>          inspected;
    protected boolean                  hasFirst = false;

    public PokecubePlayerStats()
    {
        super();
    }

    public void initMaps()
    {
        captures = Maps.newHashMap();
        hatches = Maps.newHashMap();
        kills = Maps.newHashMap();
        inspected = Sets.newHashSet();
    }

    public boolean hasInspected(PokedexEntry entry)
    {
        if (inspected == null) initMaps();
        return inspected.contains(entry);
    }

    public boolean inspect(EntityPlayer player, IPokemob pokemob)
    {
        if (inspected == null) initMaps();
        if (player instanceof EntityPlayerMP) Triggers.INSPECTPOKEMOB.trigger((EntityPlayerMP) player, pokemob);
        return inspected.add(pokemob.getPokedexEntry());
    }

    public void addCapture(PokedexEntry entry)
    {
        int num = getCaptures().get(entry) == null ? 0 : getCaptures().get(entry);
        getCaptures().put(entry, num + 1);
    }

    public void addKill(PokedexEntry entry)
    {
        int num = getKills().get(entry) == null ? 0 : getKills().get(entry);
        getKills().put(entry, num + 1);
    }

    public void addHatch(PokedexEntry entry)
    {
        int num = getHatches().get(entry) == null ? 0 : getHatches().get(entry);
        getHatches().put(entry, num + 1);
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
        for (PokedexEntry e : getKills().keySet())
        {
            tag.setInteger(e.getName(), getKills().get(e));
        }
        tag_.setTag("kills", tag);
        tag = new NBTTagCompound();
        for (PokedexEntry e : getCaptures().keySet())
        {
            tag.setInteger(e.getName(), getCaptures().get(e));
        }
        tag_.setTag("captures", tag);
        tag = new NBTTagCompound();
        for (PokedexEntry e : getHatches().keySet())
        {
            tag.setInteger(e.getName(), getHatches().get(e));
        }
        tag_.setTag("hatches", tag);
        NBTTagList list = new NBTTagList();
        for (PokedexEntry e : inspected)
        {
            list.appendTag(new NBTTagString(e.getName()));
        }
        tag_.setTag("inspected", list);
        tag_.setBoolean("F", hasFirst);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        NBTTagCompound temp = tag.getCompoundTag("kills");
        PokedexEntry entry;
        initMaps();
        hasFirst = tag.getBoolean("F");
        for (String s : temp.getKeySet())
        {
            int num = temp.getInteger(s);
            if (num > 0 && (entry = Database.getEntry(s)) != null)
            {
                for (int i = 0; i < num; i++)
                    addKill(entry);
            }
        }
        temp = tag.getCompoundTag("captures");
        for (String s : temp.getKeySet())
        {
            int num = temp.getInteger(s);
            if (num > 0 && (entry = Database.getEntry(s)) != null)
            {
                for (int i = 0; i < num; i++)
                    addCapture(entry);
            }
        }
        temp = tag.getCompoundTag("hatches");
        for (String s : temp.getKeySet())
        {
            int num = temp.getInteger(s);
            if (num > 0 && (entry = Database.getEntry(s)) != null)
            {
                for (int i = 0; i < num; i++)
                    addHatch(entry);
            }
        }
        if (tag.hasKey("inspected"))
        {
            NBTTagList list = (NBTTagList) tag.getTag("inspected");
            if (inspected == null) initMaps();
            for (int i = 0; i < list.tagCount(); i++)
            {
                String s = list.getStringTagAt(i);
                entry = Database.getEntry(s);
                if (entry != null) inspected.add(entry);
            }
        }
    }

    @Override
    public String dataFileName()
    {
        return "pokecubeStats";
    }

    public Map<PokedexEntry, Integer> getCaptures()
    {
        if (captures == null) initMaps();
        return captures;
    }

    public Map<PokedexEntry, Integer> getKills()
    {
        if (kills == null) initMaps();
        return kills;
    }

    public Map<PokedexEntry, Integer> getHatches()
    {
        if (hatches == null) initMaps();
        return hatches;
    }
}
