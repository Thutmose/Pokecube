package pokecube.adventures.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.network.PacketPokeAdv;
import pokecube.adventures.network.PacketPokeAdv.MessageClient;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.utils.ChunkCoordinate;

public class TeamManager
{

    public static class TeamLand
    {
        HashSet<ChunkCoordinate> land = Sets.newHashSet();

        public boolean addLand(ChunkCoordinate land)
        {
            return this.land.add(land);
        }

        public boolean removeLand(ChunkCoordinate land)
        {
            return this.land.remove(land);
        }

        public int countLand()
        {
            return land.size();
        }

        public NBTTagCompound saveToNBT()
        {
            NBTTagCompound tag = new NBTTagCompound();
            NBTTagList list = new NBTTagList();
            for (ChunkCoordinate c : land)
            {
                NBTTagCompound entry = new NBTTagCompound();
                entry.setIntArray("Location", new int[] { c.getX(), c.getY(), c.getZ(), c.dim });
                list.appendTag(entry);
            }
            tag.setTag("Land", list);
            return tag;
        }

        public void loadFromNBT(NBTTagCompound tag)
        {
            NBTTagList list = tag.getTagList("Land", 10);
            for (int i = 0; i < list.tagCount(); i++)
            {
                NBTTagCompound landTag = list.getCompoundTagAt(i);
                int[] loc = landTag.getIntArray("Location");
                if (loc.length != 4) continue;
                ChunkCoordinate c = new ChunkCoordinate(loc[0], loc[1], loc[2], loc[3]);
                land.add(c);
            }
        }
    }

    public static class PokeTeam
    {
        public PokeTeam(String name)
        {
            teamName = name;
        }

        TeamLand     land   = new TeamLand();
        final String teamName;
        Set<String>  admins = Sets.newHashSet();

        public void writeToNBT(NBTTagCompound nbt)
        {
            nbt.setString("name", teamName);
            nbt.setTag("land", land.saveToNBT());
            NBTTagList adminList = new NBTTagList();
            for (String s : admins)
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setString("N", s);
                adminList.appendTag(tag);
            }
            nbt.setTag("admins", adminList);
        }

        public static PokeTeam loadFromNBT(NBTTagCompound nbt)
        {
            if (!nbt.hasKey("name")) return null;
            PokeTeam team = new PokeTeam(nbt.getString("name"));
            team.land.loadFromNBT(nbt.getCompoundTag("land"));
            NBTTagList adminList = nbt.getTagList("admins", 10);
            for (int i = 0; i < adminList.tagCount(); i++)
            {
                team.admins.add(adminList.getCompoundTagAt(i).getString("N"));
            }
            return team;
        }
    }

    public static class Invites
    {
        public Set<String> teams = Sets.newHashSet();
    }

    private static TeamManager instance;

    public static int          maxLandCount = 125;
    public static boolean      denyBlasts   = false;
    public static final int    VERSION      = 1;

    public static void clearInstance()
    {
        instance = null;
    }

    public static TeamManager getInstance()
    {
        if (instance == null) instance = new TeamManager();
        return instance;
    }

    private HashMap<String, PokeTeam>        teamMap;
    private HashMap<ChunkCoordinate, String> landMap;
    private HashMap<String, Invites>         inviteMap;
    private HashSet<ChunkCoordinate>         publicBlocks;

    private TeamManager()
    {
        publicBlocks = Sets.newHashSet();
        inviteMap = Maps.newHashMap();
        teamMap = Maps.newHashMap();
        landMap = Maps.newHashMap();
    }

    public void addTeamLand(String team, ChunkCoordinate land, boolean sync)
    {
        PokeTeam t = teamMap.get(team);
        if (t == null)
        {
            Thread.dumpStack();
            return;
        }
        t.land.addLand(land);
        landMap.put(land, team);
        for (PokeTeam t1 : teamMap.values())
        {
            if (t != t1) t1.land.removeLand(land);
        }
        if (sync)
        {
            // if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER
            // &&
            // FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
            // {
            // PacketBuffer message = new PacketBuffer(Unpooled.buffer());
            // message.writeByte(6);
            // message.writeByte(PacketPokeAdv.TYPEADDLAND);
            // land.writeToBuffer(message);
            // message.writeString(team);
            // MessageClient packet = new MessageClient(message);
            // PokecubePacketHandler.sendToAll(packet);
            // }
            PASaveHandler.getInstance().saveTeams(team);
        }
    }

    public void addToAdmins(String admin, String team)
    {
        PokeTeam t = getTeam(team, true);
        System.out.println("Adding Admin " + admin + " to " + team);
        t.admins.add(admin);
        PASaveHandler.getInstance().saveTeams(team);
    }

    public void addToTeam(EntityPlayer player, String team)
    {
        player.worldObj.getScoreboard().addPlayerToTeam(player.getName(), team);
        player.addChatMessage(new TextComponentString("You joined Team " + team));
        PokeTeam t = getTeam(team, true);
        if (t.admins.isEmpty())
        {
            addToAdmins(player.getName(), team);
        }
        Invites invite = inviteMap.get(player.getName());
        if (invite != null)
        {
            invite.teams.remove(team);
        }
    }

    public int countLand(String team)
    {
        PokeTeam t = teamMap.get(team);
        if (t != null) { return t.land.countLand(); }
        return 0;
    }

    public void createTeam(EntityPlayer player, String team)
    {
        for (Object o : player.worldObj.getScoreboard().getTeamNames())
        {
            String s = (String) o;
            if (s.equalsIgnoreCase(team))
            {
                player.addChatMessage(new TextComponentString("Team " + team + " Already Exists"));
                return;
            }
        }
        if (player.worldObj.getScoreboard().getTeam(team) == null)
        {
            player.worldObj.getScoreboard().createTeam(team);
            getTeam(team, true);
            addToTeam(player, team);
            addToAdmins(player.getName(), team);
        }
    }

    public List<String> getAdmins(String team)
    {
        List<String> ret = new ArrayList<String>();
        PokeTeam t = teamMap.get(team);
        if (t != null) return Lists.newArrayList(t.admins);
        return ret;
    }

    public List<String> getInvites(String player)
    {
        List<String> ret = new ArrayList<String>();
        Invites invite = inviteMap.get(player);
        if (invite == null) return ret;
        return Lists.newArrayList(invite.teams);
    }

    public String getLandOwner(ChunkCoordinate land)
    {
        return landMap.get(land);
    }

    public List<ChunkCoordinate> getTeamLand(String team)
    {
        ArrayList<ChunkCoordinate> ret = new ArrayList<ChunkCoordinate>();
        PokeTeam t = teamMap.get(team);
        if (t != null) ret.addAll(t.land.land);
        return ret;
    }

    public boolean hasInvite(String player, String team)
    {
        Invites invite = inviteMap.get(player);
        if (invite != null) return invite.teams.contains(team);
        return false;
    }

    public void invite(String admin, String player, String team)
    {
        if (!isAdmin(admin, team)) return;
        if (hasInvite(player, team)) return;
        Invites invite = inviteMap.get(player);
        invite.teams.add(team);
    }

    public boolean isAdmin(String name, String team)
    {
        PokeTeam t = teamMap.get(team);
        if (t != null) return t.admins.contains(name);
        return false;
    }

    public boolean isAdmin(String name, Team team)
    {
        return isAdmin(name, team.getRegisteredName());
    }

    public boolean isOwned(ChunkCoordinate land)
    {
        return landMap.containsKey(land);
    }

    public boolean isPublic(ChunkCoordinate c)
    {
        return publicBlocks.contains(c);
    }

    public boolean isTeamLand(ChunkCoordinate chunk, String team)
    {
        PokeTeam t = teamMap.get(team);
        if (t != null) return t.land.land.contains(chunk);
        return false;
    }

    public void loadFromNBTOld(NBTTagCompound nbt)
    {
        if ((nbt.getTag("LandMap") instanceof NBTTagList))
        {
            NBTTagList tagList = (NBTTagList) nbt.getTag("LandMap");
            for (int i = 0; i < tagList.tagCount(); i++)
            {
                NBTTagCompound landTag = tagList.getCompoundTagAt(i);
                int[] loc = landTag.getIntArray("Location");
                String team = landTag.getString("Team");
                if (loc.length != 4 || team.isEmpty()) continue;
                ChunkCoordinate c = new ChunkCoordinate(loc[0], loc[1], loc[2], loc[3]);
                addTeamLand(team, c, false);
            }
        }
        if ((nbt.getTag("Admins") instanceof NBTTagList))
        {
            NBTTagList tagList = (NBTTagList) nbt.getTag("Admins");
            for (int i = 0; i < tagList.tagCount(); i++)
            {
                NBTTagCompound adminTag = tagList.getCompoundTagAt(i);
                String[] admins = adminTag.getString("Admins").split(":");
                String team;
                getTeam(team = adminTag.getString("Team"), true);
                for (String s : admins)
                {
                    if (s == null || s.isEmpty())
                    {
                        addToAdmins(s, team);
                    }
                }
            }
        }
        if ((nbt.getTag("PublicBlocks") instanceof NBTTagList))
        {
            NBTTagList tagList = (NBTTagList) nbt.getTag("PublicBlocks");
            for (int i = 0; i < tagList.tagCount(); i++)
            {
                NBTTagCompound landTag = tagList.getCompoundTagAt(i);
                int[] loc = landTag.getIntArray("Location");
                if (loc.length != 4) continue;
                ChunkCoordinate c = new ChunkCoordinate(loc[0], loc[1], loc[2], loc[3]);
                publicBlocks.add(c);
            }
        }
    }

    public void removeFromAdmins(String admin, String team)
    {
        PokeTeam t = teamMap.get(team);
        if (t != null && t.admins.contains(admin))
        {
            t.admins.remove(admin);
            System.out.println("Removing Admin " + admin + " to " + team);
            PASaveHandler.getInstance().saveTeams(team);
        }
    }

    public void removeFromInvites(String player, String team)
    {
        Invites invites = inviteMap.get(player);
        if (invites != null && invites.teams.contains(team))
        {
            invites.teams.remove(team);
            PASaveHandler.getInstance().saveTeams(team);
        }
    }

    public void removeFromTeam(EntityPlayer admin, String team, String toRemove)
    {
        ScorePlayerTeam oldTeam = admin.worldObj.getScoreboard().getPlayersTeam(toRemove);
        if (oldTeam != null)
        {
            removeFromAdmins(toRemove, team);
            admin.worldObj.getScoreboard().removePlayerFromTeam(toRemove, oldTeam);
        }
    }

    public void removeTeamLand(String team, ChunkCoordinate land)
    {
        PokeTeam t = teamMap.get(team);
        landMap.remove(land);
        if (t != null && t.land.removeLand(land))
        {
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER
                    && FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
            {
                PacketBuffer message = new PacketBuffer(Unpooled.buffer());
                message.writeByte(6);
                message.writeByte(PacketPokeAdv.TYPEREMOVELAND);
                land.writeToBuffer(message);
                message.writeString(team);
                MessageClient packet = new MessageClient(message);
                PokecubePacketHandler.sendToAll(packet);
            }
            PASaveHandler.getInstance().saveTeams(team);
        }
    }

    public void saveToNBT(NBTTagCompound nbt)
    {
        NBTTagList tagList = new NBTTagList();
        nbt.setInteger("VERSION", VERSION);
        for (ChunkCoordinate c : publicBlocks)
        {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setIntArray("Location", new int[] { c.getX(), c.getY(), c.getZ(), c.dim });
            tagList.appendTag(tag);
        }
        nbt.setTag("PublicBlocks", tagList);
        tagList = new NBTTagList();
        for (String s : inviteMap.keySet())
        {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("name", s);
            NBTTagList teams = new NBTTagList();
            for (String s1 : inviteMap.get(s).teams)
            {
                NBTTagCompound compound = new NBTTagCompound();
                compound.setString("T", s1);
                teams.appendTag(compound);
            }
            tag.setTag("teams", teams);
            tagList.appendTag(tag);
        }
        nbt.setTag("Invites", tagList);
    }

    public void loadFromNBT(NBTTagCompound nbt)
    {
        NBTBase base;
        if (((base = nbt.getTag("PublicBlocks")) instanceof NBTTagList))
        {
            NBTTagList tagList = (NBTTagList) base;
            for (int i = 0; i < tagList.tagCount(); i++)
            {
                NBTTagCompound landTag = tagList.getCompoundTagAt(i);
                int[] loc = landTag.getIntArray("Location");
                if (loc.length != 4) continue;
                ChunkCoordinate c = new ChunkCoordinate(loc[0], loc[1], loc[2], loc[3]);
                publicBlocks.add(c);
            }
        }
        if (((base = nbt.getTag("Invites")) instanceof NBTTagList))
        {
            NBTTagList tagList = (NBTTagList) base;
            for (int i = 0; i < tagList.tagCount(); i++)
            {
                NBTTagCompound tag = tagList.getCompoundTagAt(i);
                String name = tag.getString("name");
                Invites invites;
                inviteMap.put(name, invites = new Invites());
                NBTTagList teams = tag.getTagList("teams", 10);
                for (int i1 = 0; i1 < teams.tagCount(); i1++)
                {
                    invites.teams.add(teams.getCompoundTagAt(i1).getString("T"));
                }
            }
        }
    }

    public void saveTeamToNBT(String team, NBTTagCompound nbt)
    {
        PokeTeam t = getTeam(team, false);
        if (t != null)
        {
            t.writeToNBT(nbt);
        }
    }

    public void loadTeamFromNBT(NBTTagCompound nbt)
    {
        PokeTeam team = PokeTeam.loadFromNBT(nbt);
        if (team != null) teamMap.put(team.teamName, team);
    }

    private PokeTeam getTeam(String name, boolean create)
    {
        PokeTeam team = teamMap.get(name);
        if (team == null && create)
        {
            team = new PokeTeam(name);
            teamMap.put(name, team);
        }
        return team;
    }

    public void setPublic(ChunkCoordinate c)
    {
        publicBlocks.add(c);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER
                && FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
        {
            PacketBuffer message = new PacketBuffer(Unpooled.buffer());
            message.writeByte(6);
            message.writeByte(PacketPokeAdv.TYPESETPUBLIC);
            c.writeToBuffer(message);
            MessageClient packet = new MessageClient(message);
            PokecubePacketHandler.sendToAll(packet);
        }
        PASaveHandler.getInstance().saveTeams(null);
    }

    public void unsetPublic(ChunkCoordinate c)
    {
        if (!publicBlocks.contains(c)) return;
        publicBlocks.remove(c);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER
                && FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
        {
            PacketBuffer message = new PacketBuffer(Unpooled.buffer());
            message.writeByte(6);
            message.writeByte(PacketPokeAdv.TYPESETPUBLIC);
            c.writeToBuffer(message);
            MessageClient packet = new MessageClient(message);
            PokecubePacketHandler.sendToAll(packet);
        }
        PASaveHandler.getInstance().saveTeams(null);
    }

}
