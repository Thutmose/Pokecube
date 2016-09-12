package pokecube.core.commands;

import com.mojang.authlib.GameProfile;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntitySkull;
import pokecube.core.world.dimensions.PokecubeDimensionManager;
import pokecube.core.world.dimensions.secretpower.WorldProviderSecretBase;
import thut.api.entity.Transporter;
import thut.api.maths.Vector3;

public class SecretBaseCommand extends CommandBase
{

    public SecretBaseCommand()
    {
    }

    @Override
    public String getCommandName()
    {
        return "pokebase";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/pokebase";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        EntityPlayer player = getCommandSenderAsPlayer(sender);
        try
        {
            if (args[0].equals("reset"))
            {
                int dim = PokecubeDimensionManager.getDimensionForPlayer(player);
                PokecubeDimensionManager.createNewSecretBaseDimension(dim);
            }
            else if (args[0].equals("exit"))
            {
                if (player.worldObj.provider instanceof WorldProviderSecretBase)
                {
                    Transporter.teleportEntity(player,
                            Vector3.getNewVector().set(server.getEntityWorld().getSpawnPoint()), 0, false);
                }
                else throw new CommandException("You must be in a secret base to do that.");
            }
            else if (args[0].equals("tp") && CommandTools.isOp(sender))
            {
                String player2;
                int dim = 0;
                try
                {
                    player2 = getPlayer(server, sender, args[1]).getCachedUniqueIdString();
                    dim = PokecubeDimensionManager.getDimensionForPlayer(player2);
                }
                catch (Exception e)
                {
                    String playerName = args[1];
                    GameProfile profile = new GameProfile(null, playerName);
                    profile = TileEntitySkull.updateGameprofile(profile);
                    if (profile.getId() == null) { throw new CommandException(
                            "Error, cannot find profile for " + playerName); }
                    player2 = profile.getId().toString();
                    dim = PokecubeDimensionManager.getDimensionForPlayer(player2);
                }
                if (dim != 0)
                {
                    PokecubeDimensionManager.sendToBase(player2, player);
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            throw new CommandException(
                    "valid options: /pokebase reset, /pokebase exit, /pokebase tp <playername> (OP only)");
        }
    }

    public int getRequiredPermissionLevel()
    {
        return 0;
    }

}
