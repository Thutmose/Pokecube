package pokecube.core.commands;

import java.util.ArrayList;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;

public class RecallCommand extends CommandBase
{
    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    /** Check if the given ICommandSender has permission to execute this
     * command */
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return true;
    }

    @Override
    public String getCommandName()
    {
        return "pokerecall";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/pokerecall <optional:name,cubes,stay,all>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender cSender, String[] args) throws CommandException
    {
        String sender = cSender.getName();
        boolean isOp = CommandTools.isOp(cSender);
        boolean all = args.length > 0 && args[0].equalsIgnoreCase("all");
        boolean allall = args.length > 1 && args[1].equalsIgnoreCase("all");
        boolean cubes = args.length > 0 && args[0].equalsIgnoreCase("cubes");
        boolean stay = args.length > 0 && args[0].equalsIgnoreCase("stay");

        boolean named = !all && !stay && args.length > 0;
        String specificName = named ? args[0] : "";

        WorldServer world = (WorldServer) cSender.getEntityWorld();

        EntityPlayer player = cSender.getEntityWorld().getPlayerEntityByName(sender);
        if (allall && cSender.getEntityWorld().getPlayerEntityByName(sender) != null)
        {
            allall = isOp;
            if (!allall)
            {
                allall = false;
                CommandTools.sendNoPermissions(cSender);
                throw new CommandException("You need to be OP to use this command");
            }

        }
        ArrayList<?> list = new ArrayList<Object>(world.loadedEntityList);
        int num = 0;
        for (Object o : list)
        {
            if (!cubes && o instanceof IPokemob)
            {
                IPokemob mob = (IPokemob) o;

                boolean isStaying = mob.getPokemonAIState(IMoveConstants.STAYING);
                if (mob.getPokemonAIState(IMoveConstants.TAMED) && (mob.getPokemonOwner() == player || allall)
                        && (named || all || (stay == isStaying)) && (named == specificName
                                .equalsIgnoreCase(mob.getPokemonDisplayName().getUnformattedComponentText())))
                {
                    num++;
                    mob.returnToPokecube();
                }
            }
            if ((all || cubes) && o instanceof EntityPokecube)
            {
                EntityPokecube cube = (EntityPokecube) o;
                if (!cube.addedToChunk) continue;
                Entity owner = cube.getOwner();
                boolean owned = owner != null;
                if (!owned)
                {
                    String ownerId = PokecubeManager.getOwner(cube.getEntityItem());
                    if (!ownerId.isEmpty())
                    {
                        owned = !ownerId.equals(PokecubeMod.fakeUUID.toString());
                    }
                }
                if ((owner == player) || (allall && owned))
                {
                    try
                    {
                        cube.sendOut().returnToPokecube();
                    }
                    catch (Exception e)
                    {
                        System.err.println(cube.getEntityItem().getDisplayName());
                    }
                    num++;
                }
            }
        }
        if (num == 0)
        {
            cSender.addChatMessage(new TextComponentTranslation("pokecube.recall.fail"));
        }
        else
        {
            cSender.addChatMessage(new TextComponentTranslation("pokecube.recall.success", num));
        }
    }
}
