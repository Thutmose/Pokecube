package pokecube.core.handlers.playerdata;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import pokecube.core.utils.PokecubeSerializer.TeleDest;
import thut.core.common.handlers.PlayerDataHandler.PlayerData;

/** Data which needs to be synced to clients about the player, this is teleport
 * information and starter status. */
public class PokecubePlayerData extends PlayerData
{
    // TODO a way to share teleports.
    // TODO a way to sort teleports into groups.
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
                telelocs.add(d.setIndex(j));
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
