package pokecube.adventures.comands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import pokecube.adventures.handlers.TeamManager;
import pokecube.core.commands.CommandTools;
import pokecube.core.utils.ChunkCoordinate;

public class TeamCommands implements ICommand
{
    private static List<String> options = Lists.newArrayList();

    static
    {
        options.add("claim");
        options.add("unclaim");
        options.add("create");
        options.add("join");
        options.add("invite");
        options.add("remove");
        options.add("leave");
        options.add("admin");
        options.add("unadmin");
        options.add("team");
        options.add("list");
        options.add("admins");
        options.add("invites");
        options.add("land");
    }

    private List<String> aliases;

    public TeamCommands()
    {
        this.aliases = new ArrayList<String>();
        this.aliases.add("poketeam");
        this.aliases.add("pteam");
        this.aliases.add("pokeTeam");
    }

    @Override
    public String getCommandName()
    {
        return "pokeTeam";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_)
    {
        return "poketeam <text>";
    }

    @Override
    public List<String> getCommandAliases()
    {
        return aliases;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        if (args.length > 0)
        {
            boolean isOp = CommandTools.isOp(sender);
            ScorePlayerTeam team = null;
            if (sender instanceof EntityPlayer)
            {
                team = ((EntityPlayer) sender).worldObj.getScoreboard().getPlayersTeam(sender.getName());
            }

            for (int i = 1; i < args.length; i++)
            {
                String s = args[i];
                if (s.contains("@"))
                {
                }
            }
            String arg1 = args[0];
            if (arg1.equalsIgnoreCase("claim") && team != null)
            {
                if (!TeamManager.getInstance().isAdmin(sender.getName(), team)
                        || team.getRegisteredName().equalsIgnoreCase("Trainers"))
                {
                    sender.addChatMessage(new ChatComponentText("You are not Authorized to claim land for your team"));
                    return;
                }
                int teamCount = team.getMembershipCollection().size();

                int count = TeamManager.getInstance().countLand(team.getRegisteredName());

                boolean up = false;
                int num = 1;

                if (args.length > 2)
                {
                    try
                    {
                        if (args[1].equalsIgnoreCase("up") || args[1].equalsIgnoreCase("down"))
                        {
                            num = Integer.parseInt(args[2]);
                            up = args[1].equalsIgnoreCase("up");
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        // e.printStackTrace();
                    }
                }
                for (int i = 0; i < num; i++)
                {
                    if (count < teamCount * TeamManager.maxLandCount || isOp)
                    {
                        int dir = up ? 1 : -1;
                        teamCount = team.getMembershipCollection().size();
                        count = TeamManager.getInstance().countLand(team.getRegisteredName());

                        int x = MathHelper.floor_double(sender.getPosition().getX() / 16f);
                        int y = MathHelper.floor_double(sender.getPosition().getY() / 16f) + i * dir;
                        int z = MathHelper.floor_double(sender.getPosition().getZ() / 16f);
                        int dim = sender.getEntityWorld().provider.getDimensionId();
                        if (y < 0 || y > 15) return;

                        String owner = TeamManager.getInstance().getLandOwner(new ChunkCoordinate(x, y, z, dim));

                        if (owner != null)
                        {
                            if (owner.equals(team.getRegisteredName())) continue;

                            sender.addChatMessage(new ChatComponentText("This land is already claimed by " + owner));
                            return;
                        }

                        sender.addChatMessage(
                                new ChatComponentText("Claimed This land for Team" + team.getRegisteredName()));
                        TeamManager.getInstance().addTeamLand(team.getRegisteredName(),
                                new ChunkCoordinate(x, y, z, dim));
                        num--;
                    }
                    else
                    {
                        num = 0;
                    }
                }
                return;
            }
            if (arg1.equalsIgnoreCase("unclaim") && team != null)
            {
                if (!TeamManager.getInstance().isAdmin(sender.getName(), team)
                        || team.getRegisteredName().equalsIgnoreCase("Trainers"))
                {
                    sender.addChatMessage(
                            new ChatComponentText("You are not Authorized to unclaim land for your team"));
                    return;
                }
                boolean up = false;
                int num = 1;

                if (args.length > 2)
                {
                    try
                    {
                        if (args[1].equalsIgnoreCase("up") || args[1].equalsIgnoreCase("down"))
                        {
                            num = Integer.parseInt(args[2]);
                            up = args[1].equalsIgnoreCase("up");
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        // e.printStackTrace();
                    }
                }
                for (int i = 0; i < num; i++)
                {
                    int dir = up ? -1 : 1;
                    int x = MathHelper.floor_double(sender.getPosition().getX() / 16f);
                    int y = MathHelper.floor_double(sender.getPosition().getY() / 16f) + dir * i;
                    int z = MathHelper.floor_double(sender.getPosition().getZ() / 16f);
                    int dim = sender.getEntityWorld().provider.getDimensionId();
                    if (y < 0 || y > 15) return;
                    sender.addChatMessage(
                            new ChatComponentText("Unclaimed This land for Team" + team.getRegisteredName()));
                    TeamManager.getInstance().removeTeamLand(team.getRegisteredName(),
                            new ChunkCoordinate(x, y, z, dim));
                }
                return;
            }
            if (arg1.equalsIgnoreCase("create") && sender instanceof EntityPlayer)
            {
                if (args.length > 1)
                {
                    String teamname = args[1];
                    TeamManager.getInstance().createTeam((EntityPlayer) sender, teamname);
                    ;
                }
                return;
            }
            if (arg1.equalsIgnoreCase("join") && team != null)
            {
                if (args.length > 1)
                {
                    String teamname = args[1];
                    ScorePlayerTeam teamtojoin = sender.getEntityWorld().getScoreboard().getTeam(teamname);
                    if (teamtojoin != null)
                    {
                        boolean empty = !(teamtojoin.getRegisteredName().equalsIgnoreCase("Pokecube")
                                || teamtojoin.getRegisteredName().equalsIgnoreCase("Trainers"));
                        if (empty)
                        {
                            empty = teamtojoin.getMembershipCollection() == null
                                    || teamtojoin.getMembershipCollection().size() == 0;
                        }
                        if (empty || isOp)
                        {
                            TeamManager.getInstance().addToTeam((EntityPlayer) sender, teamname);
                            TeamManager.getInstance().addToAdmins(sender.getName(), teamname);
                            return;
                        }

                    }
                    if (TeamManager.getInstance().hasInvite(sender.getName(), teamname) || isOp)
                        TeamManager.getInstance().addToTeam((EntityPlayer) sender, teamname);
                    else sender.addChatMessage(new ChatComponentText("You do not have an invite for Team " + teamname));

                }
                return;
            }
            if (arg1.equalsIgnoreCase("invite") && team != null)
            {
                if (args.length > 1)
                {
                    String teamname = args[1];
                    EntityPlayer adding = sender.getEntityWorld().getPlayerEntityByName(teamname);
                    boolean isPlayer = adding != null;
                    if (isPlayer)
                        TeamManager.getInstance().invite(sender.getName(), adding.getName(), team.getRegisteredName());
                }
                return;
            }
            if (arg1.equalsIgnoreCase("remove") && team != null && !team.getRegisteredName().equals("Trainers"))
            {
                if (args.length > 1)
                {
                    String teamname = args[1];

                    if (teamname.equalsIgnoreCase(sender.getName())
                            || TeamManager.getInstance().isAdmin(sender.getName(), team))
                    {
                        TeamManager.getInstance().removeFromTeam((EntityPlayer) sender, team.getRegisteredName(),
                                teamname);
                        sender.addChatMessage(new ChatComponentText(
                                "Removed " + teamname + " From Team " + team.getRegisteredName()));
                    }
                }
                return;
            }
            if (arg1.equalsIgnoreCase("leave") && team != null && !team.getRegisteredName().equals("Trainers"))
            {
                String teamname = sender.getName();

                if (teamname.equalsIgnoreCase(sender.getName())
                        || TeamManager.getInstance().isAdmin(sender.getName(), team))
                {
                    TeamManager.getInstance().removeFromTeam((EntityPlayer) sender, team.getRegisteredName(), teamname);
                    sender.addChatMessage(new ChatComponentText("Left Team " + team.getRegisteredName()));
                }
                return;
            }
            if (arg1.equalsIgnoreCase("admin") && team != null
                    && !team.getRegisteredName().equalsIgnoreCase("Trainers"))
            {
                if (args.length > 1)
                {
                    String player = args[1];
                    String teamName = team.getRegisteredName();
                    if (TeamManager.getInstance().isAdmin(sender.getName(), team))
                    {
                        TeamManager.getInstance().addToAdmins(player, teamName);
                        sender.addChatMessage(
                                new ChatComponentText(player + " added as an Admin for Team " + teamName));

                    }
                    else
                    {
                        sender.addChatMessage(new ChatComponentText("You are not Authorized to do that"));
                    }
                }
                return;
            }
            if (arg1.equalsIgnoreCase("unadmin") && team != null
                    && !team.getRegisteredName().equalsIgnoreCase("Trainers"))
            {
                if (args.length > 1)
                {
                    String player = args[1];
                    String teamName = team.getRegisteredName();
                    if (TeamManager.getInstance().isAdmin(sender.getName(), team))
                    {
                        TeamManager.getInstance().removeFromAdmins(player, teamName);
                        sender.addChatMessage(
                                new ChatComponentText(player + " removed as an Admin for Team " + teamName));
                    }
                    else
                    {
                        sender.addChatMessage(new ChatComponentText("You are not Authorized to do that"));
                    }
                }
                return;
            }
            if (arg1.equalsIgnoreCase("team") && team != null)
            {
                String teamName = team.getRegisteredName();
                sender.addChatMessage(new ChatComponentText("Currently a member of Team " + teamName));
                return;
            }
            if (arg1.equalsIgnoreCase("list") && team != null)
            {
                String teamName = team.getRegisteredName();
                sender.addChatMessage(new ChatComponentText("Members of Team " + teamName));
                Collection<?> c = team.getMembershipCollection();
                for (Object o : c)
                {
                    sender.addChatMessage(new ChatComponentText("" + o));
                }
                return;
            }
            if (arg1.equalsIgnoreCase("admins") && team != null)
            {
                String teamName = team.getRegisteredName();
                sender.addChatMessage(new ChatComponentText("Admins of Team " + teamName));
                Collection<?> c = TeamManager.getInstance().getAdmins(teamName);
                for (Object o : c)
                {
                    sender.addChatMessage(new ChatComponentText("" + o));
                }
                return;
            }
            if (arg1.equalsIgnoreCase("invites") && team != null)
            {
                String teamName = sender.getName();
                sender.addChatMessage(new ChatComponentText("Invites for " + teamName));
                Collection<?> c = TeamManager.getInstance().getInvites(teamName);
                for (Object o : c)
                {
                    sender.addChatMessage(new ChatComponentText("" + o));
                }
                return;
            }
            if (arg1.equalsIgnoreCase("land") && team != null)
            {
                int x = MathHelper.floor_double(sender.getPosition().getX() / 16f);
                int y = MathHelper.floor_double(sender.getPosition().getY() / 16f);
                int z = MathHelper.floor_double(sender.getPosition().getZ() / 16f);
                int dim = sender.getEntityWorld().provider.getDimensionId();
                String owner = TeamManager.getInstance().getLandOwner(new ChunkCoordinate(x, y, z, dim));
                if (owner == null) sender.addChatMessage(new ChatComponentText("This Land is not owned"));
                else sender.addChatMessage(new ChatComponentText("This Land is owned by Team " + owner));
                System.out.println(new ChunkCoordinate(x, y, z, dim) + " " + sender.getPosition());
                return;
            }

        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
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
        String arg = args[0];
        if (arg.equalsIgnoreCase("invite") || arg.equalsIgnoreCase("remove") || arg.equalsIgnoreCase("admin")
                || arg.equalsIgnoreCase("unadmin"))
            return index == 1;

        return false;
    }

    @Override
    public int compareTo(ICommand arg0)
    {
        return 0;
    }

}
