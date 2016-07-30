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
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
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
    public List<String> addTabCompletionOptions(ICommandSender p_71516_1_, String[] args, BlockPos pos)
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
    public boolean canCommandSenderUseCommand(ICommandSender p_71519_1_)
    {
        return true;
    }

    @Override
    public int compareTo(ICommand o)
    {
        return 0;
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

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        String text = "";
        IChatComponent message;
        if (args.length == 0)
        {
            text = EnumChatFormatting.RED + "" + EnumChatFormatting.ITALIC + "Invalid arguments, try TAB for options";
            message = IChatComponent.Serializer.jsonToComponent("[\"" + text + "\"]");
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
                text = EnumChatFormatting.RED + "" + EnumChatFormatting.ITALIC + "Invalid arguments, missing option";
                message = IChatComponent.Serializer.jsonToComponent("[\"" + text + "\"]");
                sender.addChatMessage(message);
                return;
            }
            boolean check = args.length <= 2;
            Field field = fieldMap.get(args[1]);

            if (field == null)
            {
                text = EnumChatFormatting.RED + "" + EnumChatFormatting.ITALIC
                        + "Invalid arguments, invalid option, use TAB to see valid choices";
                message = IChatComponent.Serializer.jsonToComponent("[\"" + text + "\"]");
                sender.addChatMessage(message);
                return;
            }
            try
            {
                Object o = field.get(Config.instance);
                if (check)
                {
                    text += EnumChatFormatting.GREEN + args[1] + EnumChatFormatting.WHITE + " is set to: "
                            + EnumChatFormatting.GOLD;
                    if (o instanceof String[] || o instanceof int[])
                    {
                        text += Arrays.toString((Object[]) o);
                    }
                    else
                    {
                        text += o;
                    }
                    message = IChatComponent.Serializer.jsonToComponent("[\"" + text + "\"]");
                    sender.addChatMessage(message);
                    return;
                }
                else
                {
                    if (!isOp)
                    {
                        text = EnumChatFormatting.RED + "" + EnumChatFormatting.ITALIC + "Insufficient Permissions";
                        message = IChatComponent.Serializer.jsonToComponent("[\"" + text + "\"]");
                        sender.addChatMessage(message);
                        return;
                    }
                    try
                    {
                        Config.instance.updateField(field, args[2]);
                    }
                    catch (Exception e)
                    {
                        text = EnumChatFormatting.RED + "" + EnumChatFormatting.ITALIC + "Invalid option for "
                                + args[1];
                        message = IChatComponent.Serializer.jsonToComponent("[\"" + text + "\"]");
                        sender.addChatMessage(message);
                        return;
                    }
                    o = field.get(Config.instance);
                    text += EnumChatFormatting.GREEN + args[1] + EnumChatFormatting.WHITE + " set to: "
                            + EnumChatFormatting.GOLD;
                    if (o instanceof String[] || o instanceof int[])
                    {
                        text += Arrays.toString((Object[]) o);
                    }
                    else
                    {
                        text += o;
                    }

                    message = IChatComponent.Serializer.jsonToComponent("[\"" + text + "\"]");
                    sender.addChatMessage(message);
                    return;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                text = EnumChatFormatting.RED + "" + EnumChatFormatting.ITALIC
                        + "Error while checking config field, please report this as a bug.";
                message = IChatComponent.Serializer.jsonToComponent("[\"" + text + "\"]");
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
                        text = EnumChatFormatting.GREEN + "Trainer all dieing set to " + on;
                        message = IChatComponent.Serializer.jsonToComponent("[\"" + text + "\"]");
                        sender.addChatMessage(message);
                        return;
                    }
                    else
                    {
                        text = EnumChatFormatting.RED + "" + EnumChatFormatting.ITALIC + "Insufficient Permissions";
                        message = IChatComponent.Serializer.jsonToComponent("[\"" + text + "\"]");
                        sender.addChatMessage(message);
                        return;
                    }
                }

            }
        }

    }

}
