package pokecube.core.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.core.common.commands.CommandTools;

public class CountCommand extends CommandBase
{
    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getName()
    {
        return "pokecount";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/pokecount";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender cSender, String[] args) throws CommandException
    {
        boolean specific = args.length > 0;
        World world = cSender.getEntityWorld();
        List<Entity> entities = new ArrayList<Entity>(world.loadedEntityList);
        int count1 = 0;
        int count2 = 0;
        String name = "";
        Map<PokedexEntry, Integer> counts = Maps.newHashMap();
        PokedexEntry entry = null;
        if (specific)
        {
            name = args[1];
            entry = Database.getEntry(name);
            if (entry == null) throw new CommandException(name + " not found");
        }
        for (Entity o : entities)
        {
            IPokemob e = CapabilityPokemob.getPokemobFor(o);
            if (e != null)
            {
                if (!specific || e.getPokedexEntry() == entry)
                {
                    if (o.getDistance(cSender.getPositionVector().x, cSender.getPositionVector().y,
                            cSender.getPositionVector().z) > PokecubeMod.core.getConfig().maxSpawnRadius)
                        count2++;
                    else count1++;
                    Integer i = counts.get(e.getPokedexEntry());
                    if (i == null) i = 0;
                    counts.put(e.getPokedexEntry(), i + 1);
                }
            }
        }
        List<Map.Entry<PokedexEntry, Integer>> entries = Lists.newArrayList(counts.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<PokedexEntry, Integer>>()
        {
            @Override
            public int compare(Entry<PokedexEntry, Integer> o1, Entry<PokedexEntry, Integer> o2)
            {
                return o2.getValue() - o1.getValue();
            }
        });
        cSender.sendMessage(CommandTools.makeTranslatedMessage("pokecube.command.count", "", count1, count2));
        cSender.sendMessage(new TextComponentString(entries.toString()));
    }
}
