package pokecube.core.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.core.database.Database;
import pokecube.core.database.moves.json.JsonMoves;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.animations.AnimationMultiAnimations;
import pokecube.core.network.packets.PacketDataSync;
import pokecube.core.utils.PokecubeSerializer;
import thut.core.common.commands.CommandTools;

public class Commands extends CommandBase
{
    private static final String RESETREWARD = "pokecube.command.resetreward";
    private static final String DENYSTARTER = "pokecube.command.denystarter";

    static
    {
        PermissionAPI.registerNode(RESETREWARD, DefaultPermissionLevel.OP,
                "Permission to reset individual pokewatch rewards.");
        PermissionAPI.registerNode(DENYSTARTER, DefaultPermissionLevel.OP,
                "Permission to set a player has having a starter.");
    }

    private List<String> aliases;

    public Commands()
    {
        this.aliases = new ArrayList<String>();
        this.aliases.add("pokecube");
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public int compareTo(ICommand arg0)
    {
        return super.compareTo(arg0);
    }

    private boolean doReset(ICommandSender cSender, String[] args, EntityPlayerMP[] targets) throws CommandException
    {
        if (args[0].equalsIgnoreCase("resetreward"))
        {
            if (!CommandTools.isOp(cSender, RESETREWARD))
            {
                CommandTools.sendNoPermissions(cSender);
                return false;
            }
            if (args.length >= 3)
            {
                EntityPlayer player = null;
                String name = null;
                if (targets == null)
                {
                    name = args[1];
                    player = getPlayer(cSender.getServer(), cSender, name);
                }
                else
                {
                    player = targets[0];
                }
                String reward = args[2];
                boolean check = args.length == 3;

                if (player != null)
                {
                    NBTTagCompound tag = PokecubePlayerDataHandler.getCustomDataTag(player);
                    if (check)
                    {
                        boolean has = tag.getBoolean(reward);
                        cSender.sendMessage(CommandTools.makeTranslatedMessage("pokecube.command.checkreward", "",
                                player.getName(), reward, has));
                    }
                    else
                    {
                        tag.setBoolean(reward, false);
                        cSender.sendMessage(CommandTools.makeTranslatedMessage("pokecube.command.resetreward", "",
                                player.getName(), reward));
                        PokecubePlayerDataHandler.saveCustomData(player);
                    }
                }
                else
                {
                    throw new PlayerNotFoundException(args[1]);
                }
                return true;
            }
        }
        return false;
    }

    private boolean doSetHasStarter(ICommandSender cSender, String[] args, EntityPlayerMP[] targets)
    {
        if (args[0].equalsIgnoreCase("denystarter") && !CommandTools.isOp(cSender, DENYSTARTER))
        {
            CommandTools.sendNoPermissions(cSender);
            return false;
        }
        if (args[0].equalsIgnoreCase("denystarter") && args.length == 2)
        {
            WorldServer world = (WorldServer) cSender.getEntityWorld();
            EntityPlayer player = null;

            int num = 1;
            int index = 0;
            String name = null;

            if (targets != null)
            {
                num = targets.length;
            }
            else
            {
                name = args[1];
                player = world.getPlayerEntityByName(name);
            }

            for (int i = 0; i < num; i++)
            {
                if (targets != null)
                {
                    player = targets[index];
                }
                if (player != null)
                {
                    PokecubeSerializer.getInstance().setHasStarter(player, true);
                    PacketDataSync.sendInitPacket(player, "pokecube-data");
                    cSender.sendMessage(new TextComponentTranslation("pokecube.command.denystarter", player.getName()));
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length == 0)
        {
            CommandTools.sendBadArgumentsTryTab(sender);
            return;
        }
        if (args[0].equals("reloadAnims"))
        {
            try
            {
                File moves = new File(Database.CONFIGLOC + args[1] + ".json");
                File anims = new File(Database.CONFIGLOC + args[2] + ".json");
                JsonMoves.merge(anims, moves);
            }
            catch (Exception e)
            {
                throw new CommandException("Error loading animations");
            }
            for (Move_Base move : MovesUtils.moves.values())
            {
                if (move.move.baseEntry != null && move.move.baseEntry.animations != null
                        && !move.move.baseEntry.animations.isEmpty())
                {
                    move.setAnimation(new AnimationMultiAnimations(move.move));
                    continue;
                }
            }
            CommandTools.sendMessage(sender, "Reloaded move animations.");
            return;
        }

        EntityPlayerMP[] targets = null;
        for (int i = 1; i < args.length; i++)
        {
            String s = args[i];
            if (s.contains("@"))
            {
                ArrayList<EntityPlayer> targs = new ArrayList<EntityPlayer>(
                        EntitySelector.matchEntities(sender, s, EntityPlayer.class));
                targets = targs.toArray(new EntityPlayerMP[0]);
            }
        }
        boolean message = false;

        message |= doReset(sender, args, targets);
        message |= doSetHasStarter(sender, args, targets);
        if (!message)
        {
            CommandTools.sendBadArgumentsTryTab(sender);
        }
    }

    @Override
    public List<String> getAliases()
    {
        return this.aliases;
    }

    @Override
    public String getName()
    {
        return "pokecube";
    }

    @Override
    public String getUsage(ICommandSender icommandsender)
    {
        return "pokecube <denystarter|resetreward|reloadAnims> <arguments>";
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        List<String> ret = new ArrayList<String>();
        if (args[0].isEmpty()) { return ret; }
        if (ret.isEmpty() && args.length == 1)
        {
            ret.add("reloadAnims moves animations");
        }
        return ret;
    }

    @Override
    public boolean isUsernameIndex(String[] astring, int i)
    {
        return false;
    }
}
