package pokecube.core.commands;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
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
        return makeTranslatedMessage(text, "red:italic");
    }

    public static ITextComponent makeTranslatedMessage(String key, String formatting, Object... args)
    {
        if (formatting == null) formatting = "";
        for (int i = 0; i < args.length; i++)
        {
            if (args[i] instanceof String)
            {
                args[i] = new TextComponentTranslation((String) args[i]);
            }

            if (!formatting.isEmpty() && args[i] instanceof ITextComponent)
            {
                ITextComponent component = (ITextComponent) args[i];
                String[] args2 = formatting.split(":");
                String colour = args2[0].toUpperCase(java.util.Locale.ENGLISH);
                component.getStyle().setColor(TextFormatting.getValueByName(colour));
                if (args2.length > 1)
                {
                    for (int i1 = 1; i1 < args2.length; i1++)
                    {
                        String arg = args2[i1];
                        if (arg.equalsIgnoreCase("italic"))
                        {
                            component.getStyle().setItalic(true);
                        }
                        if (arg.equalsIgnoreCase("bold"))
                        {
                            component.getStyle().setBold(true);
                        }
                        if (arg.equalsIgnoreCase("underlined"))
                        {
                            component.getStyle().setUnderlined(true);
                        }
                        if (arg.equalsIgnoreCase("strikethrough"))
                        {
                            component.getStyle().setStrikethrough(true);
                        }
                        if (arg.equalsIgnoreCase("obfuscated"))
                        {
                            component.getStyle().setObfuscated(true);
                        }
                    }
                }
            }
        }
        TextComponentTranslation translated = new TextComponentTranslation(key, args);
        return translated;
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
        ITextComponent message = makeTranslatedMessage(text, null);
        sender.addChatMessage(message);
    }

    public static void sendNoPermissions(ICommandSender sender)
    {
        sender.addChatMessage(makeError("pokecube.command.noperms"));
    }
}
