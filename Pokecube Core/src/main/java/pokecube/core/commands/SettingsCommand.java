package pokecube.core.commands;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StatCollector;
import pokecube.core.handlers.Config;
import pokecube.core.handlers.Configure;
import pokecube.core.interfaces.PokecubeMod;
import scala.actors.threadpool.Arrays;

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
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
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
        }
        return ret;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return true;
    }

    @Override
    public List<String> getCommandAliases()
    {
        return this.aliases;
    }

    @Override
    public String getCommandName()
    {
        return aliases.get(0);
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/" + aliases.get(0) + "<option name> <optional:newvalue>";
    }

    @Override
    /** Return the required permission level for this command. */
    public int getRequiredPermissionLevel()
    {
        return 4;
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

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {

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
            if (o instanceof String[] || o instanceof int[])
            {
                text += Arrays.toString((Object[]) o);
            }
            else
            {
                text += o;
            }
            String mess = StatCollector.translateToLocalFormatted("pokecube.command.settings.check", args[0], text);
            if (check)
            {
                CommandTools.sendMessage(sender, mess);
                return;
            }
            else
            {
                try
                {
                    PokecubeMod.core.getConfig().updateField(field, args[1]);
                }
                catch (Exception e)
                {
                    CommandTools.sendError(sender,
                            StatCollector.translateToLocalFormatted("pokecube.commands.settings.invalid", args[1]));
                    return;
                }
                o = field.get(PokecubeMod.core.getConfig());
                CommandTools.sendMessage(sender, mess);
                return;
            }
        }
        catch (Exception e)
        {
            CommandTools.sendError(sender, "pokecube.command.settings.error");
            return;
        }
    }
}
