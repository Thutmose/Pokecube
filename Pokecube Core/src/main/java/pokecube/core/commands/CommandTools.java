package pokecube.core.commands;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
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
            UserListOpsEntry userentry = ((EntityPlayerMP) player).mcServer.getPlayerList().getOppedPlayers()
                    .getEntry(player.getGameProfile());
            return userentry != null && userentry.getPermissionLevel() >= 4;
        }
        else if (sender instanceof TileEntityCommandBlock) { return true; }
        return sender.getName().equalsIgnoreCase("@") || sender.getName().equals("Server");
    }

    public static ITextComponent makeError(String text)
    {
        text = TextFormatting.RED + "" + TextFormatting.ITALIC + I18n.translateToLocal(text);
        ITextComponent message;
        message = ITextComponent.Serializer.jsonToComponent("[\"" + text + "\"]");
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
        text = I18n.translateToLocal(text);
        ITextComponent message;
        message = ITextComponent.Serializer.jsonToComponent("[\"" + text + "\"]");
        sender.addChatMessage(message);
    }

    public static void sendNoPermissions(ICommandSender sender)
    {
        sender.addChatMessage(makeError("pokecube.command.noperms"));
    }

    public static ITextComponent makeTranslatedMessage(String key, String formatting, String... args)
    {
        ITextComponent message = null;
        if (formatting == null) formatting = "";
        String argString = "";
        int num = 1;
        if (args != null) for (String s : args)
        {
            argString = argString + "{\"translate\":\"" + s + "\"}";
            num++;
            if (num <= args.length) argString = argString + ",";
        }
        if (argString.isEmpty()) argString = "\"\"";
        String text = "{\"translate\":\"" + key + "\",\"with\":[" + argString + "],\"color\":\""+formatting+"\"}";
        text = "[" + text + "]";
        try
        {
            message = ITextComponent.Serializer.jsonToComponent(text);
        }
        catch (Exception e)
        {
            message = new TextComponentString(TextFormatting.RED + "message error");
        }
        return message;
    }
}
