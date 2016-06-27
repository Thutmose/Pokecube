package pokecube.adventures.comands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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
        options.add("kick");
        options.add("leave");
        options.add("admin");
        options.add("unadmin");
        options.add("team");
        options.add("list");
        options.add("admins");
        options.add("invites");
        options.add("land");
    }

    private List<String>               aliases;
    private Map<EntityPlayer, Boolean> claimers = Maps.newHashMap();

    public TeamCommands()
    {
        this.aliases = new ArrayList<String>();
        this.aliases.add("poketeam");
        this.aliases.add("pteam");
        this.aliases.add("pokeTeam");
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void livingUpdate(LivingUpdateEvent evt)
    {
        if (evt.getEntity().worldObj.isRemote || evt.getEntity().isDead || claimers.isEmpty()) return;

        if (evt.getEntityLiving() instanceof EntityPlayer && claimers.containsKey(evt.getEntityLiving()))
        {
            boolean all = claimers.get(evt.getEntityLiving());
            ScorePlayerTeam team = null;
            team = evt.getEntityLiving().worldObj.getScoreboard().getPlayersTeam(evt.getEntityLiving().getName());
            int num = all ? 16 : 1;
            int n = 0;
            for (int i = 0; i < num; i++)
            {
                int x = MathHelper.floor_double(evt.getEntityLiving().getPosition().getX() / 16f);
                int y = MathHelper.floor_double(evt.getEntityLiving().getPosition().getY() / 16f) + i;
                if (all) y = i;
                int z = MathHelper.floor_double(evt.getEntityLiving().getPosition().getZ() / 16f);
                int dim = evt.getEntityLiving().getEntityWorld().provider.getDimension();
                if (y < 0 || y > 15) return;
                String owner = TeamManager.getInstance().getLandOwner(new ChunkCoordinate(x, y, z, dim));
                if (owner != null)
                {
                    if (owner.equals(team.getRegisteredName())) continue;
                    continue;
                }
                n++;
                TeamManager.getInstance().addTeamLand(team.getRegisteredName(), new ChunkCoordinate(x, y, z, dim),
                        true);
            }
            if (n > 0)
            {
                evt.getEntityLiving().addChatMessage(
                        new TextComponentString("Claimed This land for Team" + team.getRegisteredName()));
            }
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return true;
    }

    @Override
    public int compareTo(ICommand arg0)
    {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
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
            if (isOp && arg1.equalsIgnoreCase("autoclaim") && sender instanceof EntityPlayer)
            {
                boolean all = false;
                if (args.length > 1)
                {
                    all = args[1].equalsIgnoreCase("all");
                }
                if (claimers.containsKey(sender))
                {
                    claimers.remove(sender);
                    sender.addChatMessage(new TextComponentString("Set Autoclaiming off"));
                }
                else
                {
                    claimers.put((EntityPlayer) sender, all);
                    sender.addChatMessage(new TextComponentString("Set Autoclaiming on"));
                }
            }

            boolean valid = doClaim(args, sender, isOp, team);
            valid = valid || doUnclaim(args, sender, isOp, team);
            valid = valid || doListAdmins(args, sender, isOp, team);
            valid = valid || doListInvites(args, sender, isOp, team);
            valid = valid || doListLand(args, sender, isOp, team);
            valid = valid || doListPlayers(args, sender, isOp, team);
            valid = valid || doListTeam(args, sender, isOp, team);
            valid = valid || doAddAdmin(args, sender, isOp, team);
            valid = valid || doRemoveAdmin(args, sender, isOp, team);
            valid = valid || doLeave(args, sender, isOp, team);
            valid = valid || doKick(args, sender, isOp, team);

            if (arg1.equalsIgnoreCase("create") && sender instanceof EntityPlayer)
            {
                if (args.length > 1)
                {
                    String teamname = args[1];
                    TeamManager.getInstance().createTeam((EntityPlayer) sender, teamname);
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
                    else sender
                            .addChatMessage(new TextComponentString("You do not have an invite for Team " + teamname));

                }
                return;
            }
            if (arg1.equalsIgnoreCase("invite") && team != null)
            {
                if (args.length > 1)
                {
                    String player = args[1];
                    EntityPlayer adding = sender.getEntityWorld().getPlayerEntityByName(player);
                    boolean isPlayer = adding != null;
                    if (isPlayer)
                    {
                        TeamManager.getInstance().invite(sender.getName(), adding.getName(), team.getRegisteredName());
                        String links = "";
                        String cmd = "poketeam join";
                        String command = "/" + cmd + " " + team.getRegisteredName();
                        String abilityJson = "{\"text\":\"" + team.getRegisteredName()
                                + "\",\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\""
                                + command + "" + "\"}}";
                        links = abilityJson;
                        sender.addChatMessage(
                                new TextComponentString("New Invite to Team " + team.getRegisteredName()));
                        ITextComponent message = ITextComponent.Serializer
                                .jsonToComponent("[\" [\"," + links + ",\"]\"]");
                        sender.addChatMessage(message);
                    }
                }
                return;
            }
        }
    }

    private boolean doClaim(String[] args, ICommandSender sender, boolean isOp, ScorePlayerTeam team)
    {
        if (args[0].equalsIgnoreCase("claim") && team != null)
        {
            if (!TeamManager.getInstance().isAdmin(sender.getName(), team)
                    || team.getRegisteredName().equalsIgnoreCase("Trainers"))
            {
                sender.addChatMessage(new TextComponentString("You are not Authorized to claim land for your team"));
                return false;
            }
            int teamCount = team.getMembershipCollection().size();

            int count = TeamManager.getInstance().countLand(team.getRegisteredName());

            boolean up = false;
            boolean all = false;
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
                    if (args[1].equalsIgnoreCase("all"))
                    {
                        all = true;
                        up = true;
                        num = 16;
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
                    if (all) y = i * dir;
                    int z = MathHelper.floor_double(sender.getPosition().getZ() / 16f);
                    int dim = sender.getEntityWorld().provider.getDimension();
                    if (y < 0 || y > 15) continue;

                    String owner = TeamManager.getInstance().getLandOwner(new ChunkCoordinate(x, y, z, dim));

                    if (owner != null)
                    {
                        if (owner.equals(team.getRegisteredName())) continue;

                        sender.addChatMessage(new TextComponentString("This land is already claimed by " + owner));
                        return false;
                    }
                    sender.addChatMessage(
                            new TextComponentString("Claimed This land for Team" + team.getRegisteredName()));
                    TeamManager.getInstance().addTeamLand(team.getRegisteredName(), new ChunkCoordinate(x, y, z, dim),
                            true);
                    num--;
                }
                else
                {
                    num = 0;
                }
            }
            return true;
        }
        return false;
    }

    private boolean doUnclaim(String[] args, ICommandSender sender, boolean isOp, ScorePlayerTeam team)
    {
        if (args[0].equalsIgnoreCase("unclaim") && team != null)
        {
            if (!TeamManager.getInstance().isAdmin(sender.getName(), team)
                    || team.getRegisteredName().equalsIgnoreCase("Trainers"))
            {
                sender.addChatMessage(new TextComponentString("You are not Authorized to unclaim land for your team"));
                return false;
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
            int n = 0;
            for (int i = 0; i < num; i++)
            {
                int dir = up ? -1 : 1;
                int x = MathHelper.floor_double(sender.getPosition().getX() / 16f);
                int y = MathHelper.floor_double(sender.getPosition().getY() / 16f) + dir * i;
                int z = MathHelper.floor_double(sender.getPosition().getZ() / 16f);
                int dim = sender.getEntityWorld().provider.getDimension();
                if (y < 0 || y > 15) continue;
                n++;
                TeamManager.getInstance().removeTeamLand(team.getRegisteredName(), new ChunkCoordinate(x, y, z, dim));
            }
            if (n > 0) sender
                    .addChatMessage(new TextComponentString("Unclaimed This land for Team" + team.getRegisteredName()));
            return true;
        }
        return false;
    }

    private boolean doListPlayers(String[] args, ICommandSender sender, boolean isOp, ScorePlayerTeam team)
    {
        if (args[0].equalsIgnoreCase("list") && team != null)
        {
            String teamName = team.getRegisteredName();
            sender.addChatMessage(new TextComponentString("Members of Team " + teamName));
            Collection<?> c = team.getMembershipCollection();
            for (Object o : c)
            {
                sender.addChatMessage(new TextComponentString("" + o));
            }
            return true;
        }
        return false;
    }

    private boolean doListLand(String[] args, ICommandSender sender, boolean isOp, ScorePlayerTeam team)
    {
        if (args[0].equalsIgnoreCase("land") && team != null)
        {
            int x = MathHelper.floor_double(sender.getPosition().getX() / 16f);
            int y = MathHelper.floor_double(sender.getPosition().getY() / 16f);
            int z = MathHelper.floor_double(sender.getPosition().getZ() / 16f);
            int dim = sender.getEntityWorld().provider.getDimension();
            String owner = TeamManager.getInstance().getLandOwner(new ChunkCoordinate(x, y, z, dim));
            if (owner == null) sender.addChatMessage(new TextComponentString("This Land is not owned"));
            else sender.addChatMessage(new TextComponentString("This Land is owned by Team " + owner));
            return true;
        }
        return false;
    }

    private boolean doListAdmins(String[] args, ICommandSender sender, boolean isOp, ScorePlayerTeam team)
    {
        if (args[0].equalsIgnoreCase("admins") && team != null)
        {
            String teamName = team.getRegisteredName();
            sender.addChatMessage(new TextComponentString("Admins of Team " + teamName));
            Collection<?> c = TeamManager.getInstance().getAdmins(teamName);
            for (Object o : c)
            {
                sender.addChatMessage(new TextComponentString("" + o));
            }
            return true;
        }
        return false;
    }

    private boolean doListInvites(String[] args, ICommandSender sender, boolean isOp, ScorePlayerTeam team)
    {
        if (args[0].equalsIgnoreCase("invites") && team != null)
        {
            String player = sender.getName();
            List<String> c = TeamManager.getInstance().getInvites(player);

            if (c.isEmpty())
            {
                sender.addChatMessage(new TextComponentString("You have no team invites"));
                return true;
            }
            else
            {
                sender.addChatMessage(new TextComponentString("List of Team Invites, You can click one to join."));
            }
            String links = "";
            String cmd = "poketeam join";
            String command = "/" + cmd + " " + c.get(0);
            String abilityJson = "{\"text\":\"" + c.get(0)
                    + "\",\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + command + ""
                    + "\"}}";
            links = abilityJson;
            for (int i = 1; i < c.size(); i++)
            {
                String command2 = "/" + cmd + " " + c.get(i);
                String abilityJson2 = "{\"text\":\"" + c.get(i)
                        + "\",\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + command2
                        + "" + "\"}}";
                links = links + ",\"]\"" + ",\"[\"," + abilityJson2;
            }
            ITextComponent message = ITextComponent.Serializer.jsonToComponent("[\" [\"," + links + ",\"]\"]");
            sender.addChatMessage(message);
            return true;
        }
        return false;
    }

    private boolean doListTeam(String[] args, ICommandSender sender, boolean isOp, ScorePlayerTeam team)
    {
        if (args[0].equalsIgnoreCase("team") && team != null)
        {
            String teamName = team.getRegisteredName();
            sender.addChatMessage(new TextComponentString("Currently a member of Team " + teamName));
            return true;
        }
        return false;
    }

    private boolean doRemoveAdmin(String[] args, ICommandSender sender, boolean isOp, ScorePlayerTeam team)
    {
        if (args[0].equalsIgnoreCase("unadmin") && team != null
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
                            new TextComponentString(player + " removed as an Admin for Team " + teamName));
                }
                else
                {
                    CommandTools.sendNoPermissions(sender);
                }
                return true;
            }
        }
        return false;
    }

    private boolean doAddAdmin(String[] args, ICommandSender sender, boolean isOp, ScorePlayerTeam team)
    {
        if (args[0].equalsIgnoreCase("admin") && team != null && !team.getRegisteredName().equalsIgnoreCase("Trainers"))
        {
            if (args.length > 1)
            {
                String player = args[1];
                String teamName = team.getRegisteredName();
                if (TeamManager.getInstance().isAdmin(sender.getName(), team))
                {
                    TeamManager.getInstance().addToAdmins(player, teamName);
                    sender.addChatMessage(new TextComponentString(player + " added as an Admin for Team " + teamName));

                }
                else
                {
                    CommandTools.sendNoPermissions(sender);
                }
                return true;
            }
        }
        return false;
    }

    private boolean doLeave(String[] args, ICommandSender sender, boolean isOp, ScorePlayerTeam team)
    {
        if (args[0].equalsIgnoreCase("leave") && team != null && !team.getRegisteredName().equals("Trainers"))
        {
            String teamname = sender.getName();

            if (teamname.equalsIgnoreCase(sender.getName())
                    || TeamManager.getInstance().isAdmin(sender.getName(), team))
            {
                TeamManager.getInstance().removeFromTeam((EntityPlayer) sender, team.getRegisteredName(), teamname);
                sender.addChatMessage(new TextComponentString("Left Team " + team.getRegisteredName()));
            }
            return true;
        }
        return false;
    }

    private boolean doKick(String[] args, ICommandSender sender, boolean isOp, ScorePlayerTeam team)
    {
        if (args[0].equalsIgnoreCase("kick") && team != null && !team.getRegisteredName().equals("Trainers"))
        {
            if (args.length > 1)
            {
                String teamname = args[1];

                if (teamname.equalsIgnoreCase(sender.getName())
                        || TeamManager.getInstance().isAdmin(sender.getName(), team))
                {
                    TeamManager.getInstance().removeFromTeam((EntityPlayer) sender, team.getRegisteredName(), teamname);
                    sender.addChatMessage(
                            new TextComponentString("Removed " + teamname + " From Team " + team.getRegisteredName()));
                }
                else
                {
                    CommandTools.sendNoPermissions(sender);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> getCommandAliases()
    {
        return aliases;
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
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args,
            BlockPos pos)
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

}
