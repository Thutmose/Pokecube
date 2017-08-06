package pokecube.core.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Nature;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.Tools;
import thut.api.entity.IMobColourable;
import thut.api.maths.Vector3;

public class MakeCommand extends CommandBase
{
    private List<String> aliases;

    public MakeCommand()
    {
        this.aliases = new ArrayList<String>();
        this.aliases.add("pokemake");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        String text = "";
        ITextComponent message;
        boolean deobfuscated = PokecubeMod.isDeobfuscated() || server.isDedicatedServer();
        boolean commandBlock = !(sender instanceof EntityPlayer);
        boolean isOp = CommandTools.isOp(sender) || commandBlock;
        if (deobfuscated || commandBlock)
        {
            String name;
            if (args.length > 0)
            {
                int num = 1;
                int index = 1;
                EntityPlayer player = null;
                for (int i = 0; i < num; i++)
                {
                    if (isOp || !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
                    {
                        PokedexEntry entry = null;
                        try
                        {
                            int id = Integer.parseInt(args[0]);
                            entry = Database.getEntry(id);
                            name = entry.getName();
                        }
                        catch (NumberFormatException e)
                        {
                            name = args[0];
                            if (name.startsWith("\'"))
                            {
                                for (int j = 1; j < args.length; j++)
                                {
                                    name += " " + args[j];
                                    if (args[j].contains("\'"))
                                    {
                                        index = j + 1;
                                        break;
                                    }
                                }
                            }
                            ArrayList<PokedexEntry> entries = Lists.newArrayList(Database.allFormes);
                            Collections.shuffle(entries);
                            Iterator<PokedexEntry> iterator = entries.iterator();
                            if (name.equalsIgnoreCase("random"))
                            {
                                entry = iterator.next();
                                while (entry.legendary || !entry.base)
                                {
                                    entry = iterator.next();
                                }
                            }
                            else if (name.equalsIgnoreCase("randomall"))
                            {
                                entry = iterator.next();
                                while (!entry.base)
                                {
                                    entry = iterator.next();
                                }
                            }
                            else if (name.equalsIgnoreCase("randomlegend"))
                            {
                                entry = iterator.next();
                                while (!entry.legendary || !entry.base)
                                {
                                    entry = iterator.next();
                                }
                            }
                            else entry = Database.getEntry(name);
                        }
                        Entity mob = PokecubeMod.core.createPokemob(entry, sender.getEntityWorld());
                        if (mob == null)
                        {
                            CommandTools.sendError(sender, "pokecube.command.makeinvalid");
                            return;
                        }
                        IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
                        Vector3 offset = Vector3.getNewVector().set(0, 1, 0);
                        UUID owner = null;
                        String ownerName = setToArgs(args, pokemob, index, offset);
                        if (ownerName != null && !ownerName.isEmpty())
                        {
                            player = getPlayer(server, sender, ownerName);
                        }
                        Vector3 temp = Vector3.getNewVector();
                        if (player != null)
                        {
                            offset = offset.add(temp.set(player.getLookVec()));
                            owner = player.getUniqueID();
                        }
                        if (owner != null)
                        {
                            pokemob.setPokemonOwner(owner);
                            pokemob.setPokemonAIState(IMoveConstants.TAMED, true);
                        }
                        temp.set(sender.getPosition()).addTo(offset);
                        temp.moveEntity((Entity) mob);
                        pokemob.specificSpawnInit();
                        GeneticsManager.initMob((Entity) mob);
                        ((Entity) mob).getEntityWorld().spawnEntityInWorld((Entity) mob);
                        text = TextFormatting.GREEN + "Spawned " + pokemob.getPokemonDisplayName().getFormattedText();
                        message = ITextComponent.Serializer.jsonToComponent("[\"" + text + "\"]");
                        sender.addChatMessage(message);
                        return;
                    }
                }
            }
            CommandTools.sendError(sender, "pokecube.command.makeneedname");
            return;
        }
        CommandTools.sendError(sender, "pokecube.command.makedeny");
        return;
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
        return "/" + aliases.get(0) + "<pokemob name/number> <arguments>";
    }

    @Override
    /** Return the required permission level for this command. */
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args,
            BlockPos pos)
    {
        List<String> ret = new ArrayList<String>();
        if (args.length == 1)
        {
            String text = args[0];
            for (PokedexEntry entry : Database.allFormes)
            {
                String check = entry.getName().toLowerCase(java.util.Locale.ENGLISH);
                if (check.startsWith(text.toLowerCase(java.util.Locale.ENGLISH)))
                {
                    String name = entry.getName();
                    if (name.contains(" "))
                    {
                        name = "\'" + name + "\'";
                    }
                    ret.add(name);
                }
            }
            Collections.sort(ret, new Comparator<String>()
            {
                @Override
                public int compare(String o1, String o2)
                {
                    if (o1.contains("'") && !o2.contains("'")) return 1;
                    else if (o2.contains("'") && !o1.contains("'")) return -1;
                    return o1.compareToIgnoreCase(o2);
                }
            });
            ret = getListOfStringsMatchingLastWord(args, ret);
        }
        return ret;
    }

    /** @param args
     * @param mob
     * @param command
     * @return owner name for pokemob if needed. */
    public static String setToArgs(String[] args, IPokemob mob, int index, Vector3 offset)
    {
        boolean shiny = false;
        int red, green, blue;
        byte gender = -3;
        red = green = blue = 255;
        String ability = null;
        int exp = 10;
        int level = -1;
        String[] moves = new String[4];
        int mindex = 0;
        boolean asWild = false;
        Nature nature = Nature.values()[new Random().nextInt(Nature.values().length)];
        String ownerName = "";

        if (index < args.length)
        {
            for (int j = index; j < args.length; j++)
            {
                String[] vals = args[j].split(":");
                String arg = vals[0];
                String val = "";
                if (vals.length > 1) val = vals[1];
                if (arg.equalsIgnoreCase("s"))
                {
                    shiny = true;
                }
                else if (arg.equalsIgnoreCase("l"))
                {
                    level = Integer.parseInt(val);
                    exp = Tools.levelToXp(mob.getExperienceMode(), level);
                }
                else if (arg.equalsIgnoreCase("x"))
                {
                    if (val.equalsIgnoreCase("f")) gender = IPokemob.FEMALE;
                    if (val.equalsIgnoreCase("m")) gender = IPokemob.MALE;
                }
                else if (arg.equalsIgnoreCase("r"))
                {
                    red = Integer.parseInt(val);
                }
                else if (arg.equalsIgnoreCase("g"))
                {
                    green = Integer.parseInt(val);
                }
                else if (arg.equalsIgnoreCase("b"))
                {
                    blue = Integer.parseInt(val);
                }
                else if (arg.equalsIgnoreCase("o"))
                {
                    ownerName = val;
                }
                else if (arg.equalsIgnoreCase("a"))
                {
                    ability = val;
                }
                else if (arg.equalsIgnoreCase("m") && mindex < 4)
                {
                    moves[mindex] = val;
                    mindex++;
                }
                else if (arg.equalsIgnoreCase("v") && offset != null)
                {
                    String[] vec = val.split(",");
                    offset.x = Double.parseDouble(vec[0].trim());
                    offset.y = Double.parseDouble(vec[1].trim());
                    offset.z = Double.parseDouble(vec[2].trim());
                }
                else if (arg.equalsIgnoreCase("i"))
                {
                    String[] vec = val.split(",");
                    byte[] ivs = new byte[6];
                    if (vec.length == 1)
                    {
                        byte iv = Byte.parseByte(vec[0]);
                        Arrays.fill(ivs, iv);
                    }
                    else
                    {
                        for (int i = 0; i < 6; i++)
                        {
                            ivs[i] = Byte.parseByte(vec[i]);
                        }
                    }
                }
                else if (arg.equalsIgnoreCase("w"))
                {
                    asWild = true;
                }
                else if (arg.equalsIgnoreCase("h"))
                {
                    mob.setSize(Float.parseFloat(val));
                }
                else if (arg.equalsIgnoreCase("p"))
                {
                    try
                    {
                        nature = Nature.values()[Integer.parseInt(val)];
                    }
                    catch (NumberFormatException e)
                    {
                        nature = Nature.valueOf(val.toUpperCase(Locale.ENGLISH));
                    }
                }
                else if (arg.equalsIgnoreCase("n") && !val.isEmpty())
                {
                    mob.setPokemonNickname(val);
                }
                else
                {
                    ownerName = args[j];
                }
            }
        }
        mob.setHp(mob.getEntity().getMaxHealth());
        mob.setNature(nature);
        mob.setShiny(shiny);
        if (gender != -3) mob.setSexe(gender);
        if (mob.getEntity() instanceof IMobColourable)
            ((IMobColourable) mob.getEntity()).setRGBA(red, green, blue, 255);
        if (asWild)
        {
            mob = mob.setForSpawn(exp);
        }
        else
        {
            mob = mob.setExp(exp, asWild);
            level = Tools.xpToLevel(mob.getPokedexEntry().getEvolutionMode(), exp);
            mob.levelUp(level);
        }
        if (AbilityManager.abilityExists(ability)) mob.setAbility(AbilityManager.getAbility(ability));

        for (int i1 = 0; i1 < 4; i1++)
        {
            if (moves[i1] != null)
            {
                String arg = moves[i1];
                if (!arg.isEmpty())
                {
                    if (arg.equalsIgnoreCase("none"))
                    {
                        mob.setMove(i1, null);
                    }
                    else
                    {
                        mob.setMove(i1, arg);
                    }
                }
            }
        }

        return ownerName;
    }

}
