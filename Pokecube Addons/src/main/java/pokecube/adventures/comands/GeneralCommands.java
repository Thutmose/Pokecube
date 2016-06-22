package pokecube.adventures.comands;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import pokecube.core.commands.CommandTools;
import scala.actors.threadpool.Arrays;
import thut.core.common.config.Configure;

public class GeneralCommands implements ICommand
{
    private static List<String> options = Lists.newArrayList();

    static
    {
        options.add("settings");
        options.add("killtrainers");
    }

    public static boolean  TRAINERSDIE = false;

    private List<String>   aliases;

    ArrayList<String>      fields      = Lists.newArrayList();

    HashMap<String, Field> fieldMap    = Maps.newHashMap();

    public GeneralCommands()
    {
        this.aliases = new ArrayList<String>();
        this.aliases.add("pokeAdv");
        this.aliases.add("pokeadv");
        populateFields();
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return true;
    }

    @Override
    public int compareTo(ICommand o)
    {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
    {
        String text = "";
        ITextComponent message;
        if (args.length == 0)
        {
            text = TextFormatting.RED + "" + TextFormatting.ITALIC + "Invalid arguments, try TAB for options";
            message = ITextComponent.Serializer.jsonToComponent("[\"" + text + "\"]");
            sender.addChatMessage(message);
            return;
        }
        for (int i = 1; i < args.length; i++)
        {
            String s = args[i];
            if (s.contains("@"))
            {
            }
        }
        boolean isOp = CommandTools.isOp(sender);

        if (args[0].equalsIgnoreCase("settings"))
        {
            if (args.length == 0)
            {
                text = TextFormatting.RED + "" + TextFormatting.ITALIC + "Invalid arguments, missing option";
                message = ITextComponent.Serializer.jsonToComponent("[\"" + text + "\"]");
                sender.addChatMessage(message);
                return;
            }
            boolean check = args.length <= 2;
            Field field = fieldMap.get(args[1]);

            if (field == null)
            {
                text = TextFormatting.RED + "" + TextFormatting.ITALIC
                        + "Invalid arguments, invalid option, use TAB to see valid choices";
                message = ITextComponent.Serializer.jsonToComponent("[\"" + text + "\"]");
                sender.addChatMessage(message);
                return;
            }
            try
            {
                Object o = field.get(Config.instance);
                if (check)
                {
                    text += TextFormatting.GREEN + args[1] + TextFormatting.WHITE + " is set to: "
                            + TextFormatting.GOLD;
                    if (o instanceof String[] || o instanceof int[])
                    {
                        text += Arrays.toString((Object[]) o);
                    }
                    else
                    {
                        text += o;
                    }
                    message = ITextComponent.Serializer.jsonToComponent("[\"" + text + "\"]");
                    sender.addChatMessage(message);
                    return;
                }
                else
                {
                    if(!isOp)
                    {
                        text = TextFormatting.RED + "" + TextFormatting.ITALIC + "Insufficient Permissions";
                        message = ITextComponent.Serializer.jsonToComponent("[\"" + text + "\"]");
                        sender.addChatMessage(message);
                        return;
                    }
                    try
                    {
                        Config.instance.updateField(field, args[2]);
                    }
                    catch (Exception e)
                    {
                        text = TextFormatting.RED + "" + TextFormatting.ITALIC + "Invalid option for " + args[1];
                        message = ITextComponent.Serializer.jsonToComponent("[\"" + text + "\"]");
                        sender.addChatMessage(message);
                        return;
                    }
                    o = field.get(Config.instance);
                    text += TextFormatting.GREEN + args[1] + TextFormatting.WHITE + " set to: " + TextFormatting.GOLD;
                    if (o instanceof String[] || o instanceof int[])
                    {
                        text += Arrays.toString((Object[]) o);
                    }
                    else
                    {
                        text += o;
                    }

                    message = ITextComponent.Serializer.jsonToComponent("[\"" + text + "\"]");
                    sender.addChatMessage(message);
                    return;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                text = TextFormatting.RED + "" + TextFormatting.ITALIC
                        + "Error while checking config field, please report this as a bug.";
                message = ITextComponent.Serializer.jsonToComponent("[\"" + text + "\"]");
                sender.addChatMessage(message);
                return;
            }
        }

        if (args[0].equalsIgnoreCase("killtrainers"))
        {
            if (args.length == 1) { return; }
            if (args.length == 2)
            {
                String temp = args[1];
                boolean on = temp.equalsIgnoreCase("true") || temp.equalsIgnoreCase("on");
                boolean off = temp.equalsIgnoreCase("false") || temp.equalsIgnoreCase("off");
                if (off || on)
                {
                    if (isOp || !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
                    {
                        TRAINERSDIE = on;
                        text = TextFormatting.GREEN + "Trainer all dieing set to " + on;
                        message = ITextComponent.Serializer.jsonToComponent("[\"" + text + "\"]");
                        sender.addChatMessage(message);
                        return;
                    }
                    else
                    {
                        text = TextFormatting.RED + "" + TextFormatting.ITALIC + "Insufficient Permissions";
                        message = ITextComponent.Serializer.jsonToComponent("[\"" + text + "\"]");
                        sender.addChatMessage(message);
                        return;
                    }
                }

            }
        }

    }

    @Override
    public List<String> getCommandAliases()
    {
        return this.aliases;
    }

    @Override
    public String getCommandName()
    {
        return "pokeAdv";
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender)
    {
        return "pokeAdv <text>";
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args,
            BlockPos pos)
    {
        if (args.length == 2 && args[0].equalsIgnoreCase("settings"))
        {
            List<String> ret = new ArrayList<String>();

            String text = args[1];
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

            return ret;
        }
        else if (args.length == 1)
        {
            List<String> ret = new ArrayList<String>();
            for (String s : options)
            {
                if (s.startsWith(args[0])) ret.add(s);
            }
            return ret;
        }
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index)
    {
        return false;
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
