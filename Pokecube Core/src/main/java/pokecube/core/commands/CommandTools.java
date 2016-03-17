package pokecube.core.commands;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class CommandTools
{
    public static boolean isOp(ICommandSender sender)
    {
        if (FMLCommonHandler.instance().getMinecraftServerInstance() != null
                && !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer()) { return true; }

        if (sender instanceof EntityPlayer)
        {
            EntityPlayer player = sender.getEntityWorld().getPlayerEntityByName(sender.getName());
            UserListOpsEntry userentry = ((EntityPlayerMP) player).mcServer.getConfigurationManager().getOppedPlayers()
                    .getEntry(player.getGameProfile());
            return userentry != null && userentry.getPermissionLevel() >= 4;
        }
        else if (sender instanceof TileEntityCommandBlock) { return true; }
        return sender.getName().equalsIgnoreCase("@") || sender.getName().equals("Server");
    }

    public static IChatComponent makeError(String text)
    {
        text = EnumChatFormatting.RED + "" + EnumChatFormatting.ITALIC + StatCollector.translateToLocal(text);
        IChatComponent message;
        message = IChatComponent.Serializer.jsonToComponent("[\"" + text + "\"]");
        return message;
    }

    public static void sendBadArgumentsMissingArg(ICommandSender sender)
    {
        sender.addChatMessage(makeError("pokecube.command.invalidmissing"));
    }

    public static void sendBadArgumentsTryTab(ICommandSender sender)
    {
        sender.addChatMessage(makeError("pokecube.command.invalidtab"));
    }

    public static void sendError(ICommandSender sender, String text)
    {
        sender.addChatMessage(makeError(text));
    }

    public static void sendMessage(ICommandSender sender, String text)
    {
        text = StatCollector.translateToLocal(text);
        IChatComponent message;
        message = IChatComponent.Serializer.jsonToComponent("[\"" + text + "\"]");
        sender.addChatMessage(message);
    }

    public static void sendNoPermissions(ICommandSender sender)
    {
        sender.addChatMessage(makeError("pokecube.command.noperms"));
    }
}
