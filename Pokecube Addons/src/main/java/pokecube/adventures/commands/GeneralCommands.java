package pokecube.adventures.commands;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.adventures.PokecubeAdv;
import thut.core.common.commands.CommandTools;
import thut.core.common.config.Configure;

public class GeneralCommands extends CommandBase
{
    private static final String KILLPERM = PokecubeAdv.ID + ".command.killtrainers";

    private static List<String> options  = Lists.newArrayList();

    static
    {
        options.add("killtrainers");
        PermissionAPI.registerNode(KILLPERM, DefaultPermissionLevel.OP,
                "Can this player set all trainers to die via the command.");
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
            sender.sendMessage(message);
            return;
        }
        for (int i = 1; i < args.length; i++)
        {
            String s = args[i];
            if (s.contains("@"))
            {
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
                    if (CommandTools.isOp(sender, KILLPERM)
                            || !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
                    {
                        TRAINERSDIE = on;
                        text = TextFormatting.GREEN + "Trainer all dieing set to " + on;
                        message = ITextComponent.Serializer.jsonToComponent("[\"" + text + "\"]");
                        sender.sendMessage(message);
                        return;
                    }
                    text = TextFormatting.RED + "" + TextFormatting.ITALIC + "Insufficient Permissions";
                    message = ITextComponent.Serializer.jsonToComponent("[\"" + text + "\"]");
                    sender.sendMessage(message);
                    return;
                }

            }
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
        return "pokeAdv";
    }

    @Override
    public String getUsage(ICommandSender icommandsender)
    {
        return "pokeAdv <text>";
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
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
            ret = getListOfStringsMatchingLastWord(args, ret);
            return ret;
        }
        else if (args.length == 1)
        {
            List<String> ret = new ArrayList<String>();
            for (String s : options)
            {
                ret.add(s);
            }
            ret = getListOfStringsMatchingLastWord(args, ret);
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
