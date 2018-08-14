package pokecube.core.commands;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;

public class KillCommand extends CommandBase
{
    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getName()
    {
        return "pokekill";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/pokekill <optional:all|id>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender cSender, String[] args) throws CommandException
    {
        boolean all = args.length > 0 && args[0].equalsIgnoreCase("all");

        int id = -1;
        if (args.length > 0 && !all)
        {
            try
            {
                id = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException e)
            {

            }
        }
        boolean items = !all && id == -1 && args.length > 0 && args[0].equals("!items");
        World world = cSender.getEntityWorld();
        List<Entity> entities = new ArrayList<Entity>(world.loadedEntityList);
        int count = 0;
        for (Entity o : entities)
        {
            IPokemob e = CapabilityPokemob.getPokemobFor(o);
            if (e != null)
            {
                if (id == -1 && !e.getGeneralState(GeneralStates.TAMED) || all)
                {
                    e.returnToPokecube();
                    o.setDead();
                    count++;
                }
                if (id != -1 && o.getEntityId() == id)
                {
                    e.returnToPokecube();
                    o.setDead();
                    count++;
                }
            }
            if (o instanceof EntityPokemobEgg)
            {
                count++;
                o.setDead();
            }
            else if (items && o instanceof EntityItem)
            {
                count++;
                o.setDead();
            }
        }
        cSender.sendMessage(new TextComponentString("Killed " + count));
    }
}
