package pokecube.core.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import pokecube.core.database.Database;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.Tools;
import thut.api.entity.IMobColourable;

public class GiftCommand extends CommandBase
{
    private List<String> aliases;

    public GiftCommand()
    {
        this.aliases = new ArrayList<String>();
        this.aliases.add("pokegift");
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return true;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (sender instanceof EntityPlayer)
        {
            if (!PokecubeMod.core.getConfig().mysterygift)
            {
                CommandTools.sendError(sender, "pokecube.command.giftdisabled");
                return;
            }
            if (args.length > 0)
            {
                String code = args[0];
                String giftSt = PokecubeMod.gifts.get(code);
                if (giftSt != null)
                {
                    EntityPlayer player = (EntityPlayer) sender;

                    if (player.getEntityData().getString("code:" + code).equals(code))
                    {

                        CommandTools.sendError(sender, "pokecube.command.giftdenyused");
                        return;
                    }
                    String[] gift = giftSt.split(";");

                    String name = gift[0];

                    IPokemob mob = (IPokemob) PokecubeMod.core.createEntityByPokedexEntry(Database.getEntry(name),
                            sender.getEntityWorld());

                    boolean shiny = false;
                    boolean shadow = false;
                    int red, green, blue;
                    byte gender = -3;
                    red = green = blue = 255;

                    int exp = 10;
                    int level = -1;
                    String ability = null;
                    String[] moves = new String[4];
                    int index = 0;
                    for (int i = 1; i < gift.length; i++)
                    {
                        String[] vals = gift[i].trim().split(":");
                        String arg = vals[0].trim();
                        String val = "";
                        if (vals.length > 1) val = vals[1];
                        if (arg.equalsIgnoreCase("s"))
                        {
                            shiny = true;
                        }
                        if (arg.equalsIgnoreCase("sh"))
                        {
                            shadow = true;
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
                            red = Byte.parseByte(val);
                        }
                        else if (arg.equalsIgnoreCase("g"))
                        {
                            green = Byte.parseByte(val);
                        }
                        else if (arg.equalsIgnoreCase("b"))
                        {
                            blue = Byte.parseByte(val);
                        }
                        else if (arg.equalsIgnoreCase("a"))
                        {
                            ability = val;
                        }
                        else if (arg.equalsIgnoreCase("m") && index < 4)
                        {
                            moves[index] = val;
                            index++;
                        }
                        else if (arg.equalsIgnoreCase("n") && !val.isEmpty())
                        {
                            mob.setPokemonNickname(val);
                        }
                    }
                    mob.setOriginalOwnerUUID(new UUID(12345, 54321));
                    mob.setPokecubeId(13);
                    mob.setExp(exp, false, true);
                    mob.setShiny(shiny);
                    if (gender != -3) mob.setSexe(gender);
                    if (mob instanceof IMobColourable) ((IMobColourable) mob).setRGBA(red, green, blue, 255);
                    if (shadow) mob.setShadow(shadow);
                    if (AbilityManager.abilityExists(ability)) mob.setAbility(AbilityManager.getAbility(ability));
                    for (int i = 0; i < 4; i++)
                    {
                        if (moves[i] != null)
                        {
                            String arg = moves[i];
                            if (!arg.isEmpty())
                            {
                                if (arg.equalsIgnoreCase("none"))
                                {
                                    mob.setMove(i, null);
                                }
                                else
                                {
                                    mob.setMove(i, arg);
                                }
                            }
                        }
                    }
                    mob.setPokemonOwner(player);
                    mob.setHp(((EntityLiving) mob).getMaxHealth());
                    mob.returnToPokecube();
                    CommandTools.sendMessage(sender, "pokecube.command.gift");
                    player.getEntityData().setString("code:" + code, code);

                    return;
                }
                CommandTools.sendError(sender, "pokecube.command.giftinvalid");
                return;
            }
            CommandTools.sendError(sender, "pokecube.command.giftneedcode");
            return;
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
        return aliases.get(0);
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/" + aliases.get(0) + "<giftCode>";
    }

    @Override
    /** Return the required permission level for this command. */
    public int getRequiredPermissionLevel()
    {
        return 0;
    }
}
