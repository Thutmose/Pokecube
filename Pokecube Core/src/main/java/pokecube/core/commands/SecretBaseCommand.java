package pokecube.core.commands;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.nests.BlockNest;
import pokecube.core.blocks.nests.TileEntityBasePortal;
import pokecube.core.moves.implementations.actions.ActionSecretPower;
import pokecube.core.world.dimensions.PokecubeDimensionManager;
import pokecube.core.world.dimensions.secretpower.WorldProviderSecretBase;
import thut.api.entity.Transporter;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.common.commands.CommandTools;

public class SecretBaseCommand extends CommandBase
{
    private static final String ENTERANY = "pokecube.command.pokebase.enterany";

    static
    {
        PermissionAPI.registerNode(ENTERANY, DefaultPermissionLevel.OP, "Permission to TP to anyone's secret base.");
    }

    public SecretBaseCommand()
    {
    }

    @Override
    /** Check if the given ICommandSender has permission to execute this
     * command */
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return true;
    }

    @Override
    public String getName()
    {
        return "pokebase";
    }

    @Override
    public String getUsage(ICommandSender sender)
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
                PokecubeDimensionManager.createNewSecretBaseDimension(dim, true);
            }
            else if (args[0].equals("exit"))
            {
                if (player.getEntityWorld().provider instanceof WorldProviderSecretBase)
                {
                    String owner = PokecubeDimensionManager.getOwner(player.dimension);
                    BlockPos exit = PokecubeDimensionManager.getBaseEntrance(owner, 0);
                    if (exit == null) exit = server.getEntityWorld().getSpawnPoint();
                    Transporter.teleportEntity(player, Vector3.getNewVector().set(exit).add(0.5, 0, 0.5), 0, false);
                }
                else throw new CommandException("You must be in a secret base to do that.");
            }
            else if (args[0].equals("tp") && CommandTools.isOp(sender, ENTERANY))
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
                    UUID id = null;
                    try
                    {
                        id = UUID.fromString(playerName);
                    }
                    catch (Exception e2)
                    {
                    }
                    GameProfile profile = new GameProfile(id, playerName);
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
            else if (args[0].equals("confirm"))
            {
                if (ActionSecretPower.pendingBaseLocations.containsKey(player.getUniqueID()))
                {
                    Vector4 loc = ActionSecretPower.pendingBaseLocations.remove(player.getUniqueID());
                    Vector3 pos = Vector3.getNewVector().set(loc.x, loc.y, loc.z);
                    if (loc.w == player.dimension && pos.distToEntity(player) < 16)
                    {
                        BlockNest nest = (BlockNest) PokecubeItems.getBlock("pokemobNest");
                        pos.setBlock(player.getEntityWorld(), nest.getDefaultState().withProperty(nest.TYPE, 1));
                        TileEntityBasePortal tile = (TileEntityBasePortal) player.getEntityWorld()
                                .getTileEntity(pos.getPos());
                        tile.setPlacer(player);
                        Vector3 baseExit = Vector3.getNewVector();
                        baseExit.set(Double.parseDouble(args[1]), Double.parseDouble(args[2]),
                                Double.parseDouble(args[3]));
                        PokecubeDimensionManager.setBaseEntrance(player, player.dimension, baseExit.getPos());
                        TextComponentTranslation message = new TextComponentTranslation("pokemob.createbase.confirmed",
                                pos);
                        sender.sendMessage(message);
                    }
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            throw new CommandException(
                    "valid options: /pokebase reset, /pokebase exit, /pokebase tp <playername> (OP only)");
        }
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

}
