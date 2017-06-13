package pokecube.core.commands;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.PokecubeMod;
import thut.core.common.config.Configure;

public class SettingsCommand extends CommandBase
{
    private List<String>   aliases;

    ArrayList<String>      fields   = Lists.newArrayList();

    HashMap<String, Field> fieldMap = Maps.newHashMap();

    public SettingsCommand()
    {
        this.aliases = new ArrayList<String>();
        this.aliases.add("pokesettings");
        populateFields();
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return true;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        boolean op = CommandTools.isOp(sender);
        if (args.length == 0)
        {
            CommandTools.sendBadArgumentsTryTab(sender);
            return;
        }
        boolean check = args.length <= 1;
        Field field = fieldMap.get(args[0]);

        if (field == null)
        {
            CommandTools.sendBadArgumentsTryTab(sender);
            return;
        }
        try
        {
            String text = "";
            Object o = field.get(PokecubeMod.core.getConfig());
            if (o instanceof String[])
            {
                text += Arrays.toString((Object[]) o);
            }
            else if (o instanceof int[])
            {
                text += Arrays.toString((int[]) o);
            }
            else
            {
                text += o;
            }
            ITextComponent mess = CommandTools.makeTranslatedMessage("pokecube.command.settings.check", "gold", args[0],
                    text);
            if (check)
            {
                sender.sendMessage(mess);
                return;
            }
            if (!op)
            {
                CommandTools.sendNoPermissions(sender);
                return;
            }
            try
            {
                String val = args[1];
                if (args.length > 2)
                {
                    for (int i = 2; i < args.length; i++)
                    {
                        val = val + " " + args[i];
                    }
                }
                PokecubeMod.core.getConfig().updateField(field, val);
            }
            catch (Exception e)
            {
                mess = CommandTools.makeTranslatedMessage("pokecube.command.settings.invalid", "gold", args[0]);
                sender.sendMessage(mess);
                CommandTools.sendError(sender, text);
                return;
            }
            text = "";
            o = field.get(PokecubeMod.core.getConfig());
            if (o instanceof String[])
            {
                text += Arrays.toString((Object[]) o);
            }
            else if (o instanceof int[])
            {
                text += Arrays.toString((int[]) o);
            }
            else
            {
                text += o;
            }
            mess = CommandTools.makeTranslatedMessage("pokecube.command.settings.set", "gold", args[0], text);
            sender.sendMessage(mess);
            return;
        }
        catch (Exception e)
        {
            CommandTools.sendError(sender, "pokecube.command.settings.error");
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
        return "/" + aliases.get(0) + "<option name> <optional:newvalue>";
    }

    @Override
    /** Return the required permission level for this command. */
    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
            BlockPos pos)
    {
        List<String> ret = new ArrayList<String>();
        if (args.length == 1)
        {
            String text = args[0];
            for (String name : fields)
            {
                if (name.contains(text))
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
            ret = getListOfStringsMatchingLastWord(args, ret);
        }
        return ret;
    }

    private void populateFields()
    {
        Class<Config> me = Config.class;
        Configure c;
        for (Field f : me.getDeclaredFields())
        {
            c = f.getAnnotation(Configure.class);
            if (c != null)
            {
                f.setAccessible(true);
                fields.add(f.getName());
                fieldMap.put(f.getName(), f);
            }
        }
    }
}
