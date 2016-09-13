package pokecube.core.database.stats;

import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.PlayerDataHandler;
import pokecube.core.handlers.PlayerDataHandler.PokecubePlayerStats;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;

/** @author Thutmose */
public class StatsCollector
{
    public static Map<PokedexEntry, Integer> getCaptures(String uuid)
    {
        return PlayerDataHandler.getInstance().getPlayerData(uuid).getData("pokecube-stats", PokecubePlayerStats.class)
                .getCaptures(PokecubeCore.proxy.getPlayer(uuid));
    }

    public static Map<PokedexEntry, Integer> getKills(String uuid)
    {
        return PlayerDataHandler.getInstance().getPlayerData(uuid).getData("pokecube-stats", PokecubePlayerStats.class)
                .getKills(PokecubeCore.proxy.getPlayer(uuid));
    }

    public static Map<PokedexEntry, Integer> getHatches(String uuid)
    {
        return PlayerDataHandler.getInstance().getPlayerData(uuid).getData("pokecube-stats", PokecubePlayerStats.class)
                .getHatches(PokecubeCore.proxy.getPlayer(uuid));
    }

    public static void addCapture(IPokemob captured)
    {
        String owner;
        if (captured.getPokemonOwner() instanceof EntityPlayer)
        {
            owner = captured.getPokemonOwner().getCachedUniqueIdString();
            PokedexEntry dbe = Database.getEntry(captured);
            PlayerDataHandler.getInstance().getPlayerData(owner).getData("pokecube-stats", PokecubePlayerStats.class)
                    .addCapture((EntityPlayer) captured.getPokemonOwner(), dbe);
        }
    }

    public static void addHatched(EntityPokemobEgg hatched)
    {
        String owner;
        IPokemob mob = null;
        if (hatched.getEggOwner() instanceof EntityPlayer)
        {
            owner = hatched.getEggOwner().getCachedUniqueIdString();
            mob = hatched.getPokemob(true);
            if (mob == null)
            {
                new Exception().printStackTrace();
                return;
            }
            PokedexEntry dbe = Database.getEntry(mob);
            PlayerDataHandler.getInstance().getPlayerData(owner).getData("pokecube-stats", PokecubePlayerStats.class)
                    .addHatch((EntityPlayer) hatched.getEggOwner(), dbe);
        }
    }

    public static void addKill(IPokemob killed, IPokemob killer)
    {
        if (killer == null || killed == null) return;
        String owner;
        if (killer.getPokemonOwner() instanceof EntityPlayer)
        {
            owner = killer.getPokemonOwner().getCachedUniqueIdString();
            PokedexEntry dbe = Database.getEntry(killed);
            PlayerDataHandler.getInstance().getPlayerData(owner).getData("pokecube-stats", PokecubePlayerStats.class)
                    .addKill((EntityPlayer) killer.getPokemonOwner(), dbe);
        }
    }

    public static void readFromNBT(NBTTagCompound nbt)
    {
        NBTBase temp = nbt.getTag("kills");
        if (temp instanceof NBTTagList)
        {
            NBTTagList list = (NBTTagList) temp;
            for (int i = 0; i < list.tagCount(); i++)
            {
                NBTTagCompound kills = list.getCompoundTagAt(i);
                String s = kills.getString("username");
                for (PokedexEntry dbe : Database.data.values())
                {
                    int count = kills.getInteger(dbe.getName());
                    if (count != 0)
                    {
                        setKills(dbe, s, count);
                    }
                }
            }
        }

        temp = nbt.getTag("captures");
        if (temp instanceof NBTTagList)
        {
            NBTTagList list = (NBTTagList) temp;
            for (int i = 0; i < list.tagCount(); i++)
            {
                NBTTagCompound captures = list.getCompoundTagAt(i);
                String s = captures.getString("username");
                for (PokedexEntry dbe : Database.data.values())
                {
                    int count = captures.getInteger(dbe.getName());
                    if (count != 0)
                    {
                        setCaptures(dbe, s, count);
                    }
                }
            }
        }

        temp = nbt.getTag("hatches");
        if (temp instanceof NBTTagList)
        {
            NBTTagList list = (NBTTagList) temp;
            for (int i = 0; i < list.tagCount(); i++)
            {
                NBTTagCompound captures = list.getCompoundTagAt(i);
                String s = captures.getString("username");
                for (PokedexEntry dbe : Database.data.values())
                {
                    int count = captures.getInteger(dbe.getName());
                    if (count != 0)
                    {
                        setHatches(dbe, s, count);
                    }
                }
            }
        }
    }

    private static void setCaptures(PokedexEntry dbe, String owner, int count)
    {
        if (owner.equals("")) owner = new UUID(1234, 4321).toString();
        Map<PokedexEntry, Integer> map = getCaptures(owner);
        map.put(dbe, count);
    }

    private static void setHatches(PokedexEntry dbe, String owner, int count)
    {
        if (owner.equals("")) owner = new UUID(1234, 4321).toString();
        Map<PokedexEntry, Integer> map = getHatches(owner);
        map.put(dbe, count);
    }

    private static void setKills(PokedexEntry dbe, String owner, int count)
    {
        if (owner.equals("")) owner = new UUID(1234, 4321).toString();
        Map<PokedexEntry, Integer> map = getKills(owner);
        map.put(dbe, count);
    }

    public StatsCollector()
    {
    }

}
