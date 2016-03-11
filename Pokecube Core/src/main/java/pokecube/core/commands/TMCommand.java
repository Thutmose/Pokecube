package pokecube.core.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerSelector;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldServer;
import pokecube.core.PokecubeItems;
import pokecube.core.items.ItemTM;
import pokecube.core.moves.MovesUtils;

public class TMCommand extends CommandBase
{
    private List<String> aliases;

    public TMCommand()
    {
        this.aliases = new ArrayList<String>();
        this.aliases.add("poketm");
    }

    @Override
    public String getCommandName()
    {
        return aliases.get(0);
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/" + aliases.get(0) + "<move name>";
    }

    @Override
    public List<String> getCommandAliases()
    {
        return this.aliases;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return true;
    }

    @Override
    /** Return the required permission level for this command. */
    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        String text = "";
        IChatComponent message;
        EntityPlayerMP[] targets = null;
        for (int i = 1; i < args.length; i++)
        {
            String s = args[i];
            if (s.contains("@"))
            {
                ArrayList<EntityPlayer> targs = new ArrayList<EntityPlayer>(
                        PlayerSelector.matchEntities(sender, s, EntityPlayer.class));
                targets = (EntityPlayerMP[]) targs.toArray(new EntityPlayerMP[0]);
            }
        }
        if (args.length == 0)
        {
            text = EnumChatFormatting.RED + "" + EnumChatFormatting.ITALIC + "Invalid arguments, missing movename";
            message = IChatComponent.Serializer.jsonToComponent("[\"" + text + "\"]");
            sender.addChatMessage(message);
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

                ItemStack tm = PokecubeItems.getStack("tm");
                ItemTM.addMoveToStack(temp, tm);

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
            text = EnumChatFormatting.RED + "" + EnumChatFormatting.ITALIC + "Invalid arguments, invalid move name";
            message = IChatComponent.Serializer.jsonToComponent("[\"" + text + "\"]");
            sender.addChatMessage(message);
            return;
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        Collection<String> moves = MovesUtils.moves.keySet();
        List<String> ret = new ArrayList<String>();
        if (args.length == 1)
        {
            String text = args[0];
            for (String name : moves)
            {
                if (name.startsWith(text.toLowerCase()))
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