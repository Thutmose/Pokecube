package pokecube.core.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import pokecube.core.items.ItemTM;
import pokecube.core.moves.MovesUtils;
import thut.core.common.commands.CommandTools;

public class TMCommand extends CommandBase
{
    private List<String> aliases;

    public TMCommand()
    {
        this.aliases = new ArrayList<String>();
        this.aliases.add("poketm");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
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
        if (args.length == 0)
        {
            CommandTools.sendBadArgumentsMissingArg(sender);
            return;
        }
        if (args.length >= 1)
        {
            String temp = args[0];

            boolean isMove = false;
            isMove = MovesUtils.isMoveImplemented(temp);

            if (isMove)
            {
                int index = 0;
                String name = null;
                EntityPlayer player = null;

                WorldServer world = (WorldServer) sender.getEntityWorld();
                if (args.length == 2)
                {
                    name = args[1];
                    player = world.getPlayerEntityByName(name);
                }

                ItemStack tm = ItemTM.getTM(temp);

                if (targets != null)
                {
                    player = targets[index];
                }

                if (player == null) player = world.getPlayerEntityByName(sender.getName());

                if (player != null)
                {
                    player.inventory.addItemStackToInventory(tm);
                }
                return;
            }
            CommandTools.sendBadArgumentsTryTab(sender);
            return;
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
        return aliases.get(0);
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/" + aliases.get(0) + "<move name>";
    }

    @Override
    /** Return the required permission level for this command. */
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        Collection<String> moves = MovesUtils.moves.keySet();
        List<String> ret = new ArrayList<String>();
        if (args.length == 1)
        {
            String text = args[0];
            for (String name : moves)
            {
                if (name.startsWith(text.toLowerCase(java.util.Locale.ENGLISH)))
                {
                    ret.add(name);
                }
            }
            Collections.sort(ret, new Comparator<String>()
            {
                @Override
                public int compare(String o1, String o2)
                {

                    return o1.compareToIgnoreCase(o2);
                }
            });
        }
        return ret;
    }

}