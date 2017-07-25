package pokecube.core.commands;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;

public class KillCommand extends CommandBase
{
    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandName()
    {
        return "pokekill";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
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

        World world = cSender.getEntityWorld();
        List<Entity> entities = new ArrayList<Entity>(world.loadedEntityList);
        int count = 0;
        for (Entity o : entities)
        {
            IPokemob e = CapabilityPokemob.getPokemobFor(o);
            if (e != null)
            {
                if (id == -1 && !e.getPokemonAIState(IMoveConstants.TAMED) || all)
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
            if (o instanceof EntityPokemobEgg) o.setDead();
        }
        cSender.addChatMessage(new TextComponentString("Killed " + count));
    }
}
