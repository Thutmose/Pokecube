package pokecube.adventures.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.network.PacketPokeAdv;
import pokecube.adventures.network.PacketPokeAdv.MessageClient;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.utils.ChunkCoordinate;

public class TeamManager
{

    private static TeamManager instance;

    public static int maxLandCount = 125;

    private HashMap<ChunkCoordinate, String> landMap;
    private HashMap<String, Integer>         landCounts;
    private HashMap<String, String>          teamAdmins;
    private HashSet<ChunkCoordinate>         publicBlocks;
    private HashMap<String, String>          teamInvites;

    private TeamManager()
    {
        landMap = new HashMap<ChunkCoordinate, String>();
        landCounts = new HashMap<String, Integer>();
        teamAdmins = new HashMap<String, String>();
        teamInvites = new HashMap<String, String>();
        publicBlocks = new HashSet<ChunkCoordinate>();
    }

    public static void clearInstance()
    {
        instance = null;
    }

    public static TeamManager getInstance()
    {
        if (instance == null) instance = new TeamManager();
        return instance;
    }

    public int countLand(String team)
    {
        if (landCounts.containsKey(team)) return landCounts.get(team);
        return 0;
    }

    public void addToAdmins(String admin, String team)
    {
        String admins = "";
        if (teamAdmins.containsKey(team))
        {
            admins = teamAdmins.get(team);
        }
        admins += admin + ":";
        teamAdmins.put(team, admins);
        PASaveHandler.getInstance().saveTeams();
    }

    public void removeFromAdmins(String admin, String team)
    {
        String admins = "";
        if (teamAdmins.containsKey(team))
        {
            admins = teamAdmins.get(team);
            String[] list = admins.split(":");
            for (int i = 0; i < list.length; i++)
            {
                if (list[i].equals(admin))
                {
                    list[i] = null;
                }
            }
            admins = "";
            for (String s : list)
            {
                if (s != null) admins += s + ":";
            }
            teamAdmins.put(team, admins);
            PASaveHandler.getInstance().saveTeams();
        }
    }

    public List<String> getAdmins(String team)
    {
        List<String> ret = new ArrayList<String>();
        if (teamAdmins.containsKey(team))
        {
            String[] admins = teamAdmins.get(team).split(":");
            for (String s : admins)
            {
                if (s != null && !s.isEmpty()) ret.add(s);
            }
        }

        return ret;
    }

    public void invite(String admin, String player, String team)
    {
        String invites = "";
        if (!isAdmin(admin, teamAdmins.get(team))) return;
        if (hasInvite(player, team)) return;

        if (teamInvites.containsKey(player))
        {
            invites = teamInvites.get(player);
        }
        invites += team + ":";
        teamInvites.put(player, invites);
    }

    public List<String> getInvites(String player)
    {
        List<String> ret = new ArrayList<String>();
        String adminList = teamInvites.get(player);
        if (adminList == null) return ret;

        String[] names = adminList.split(":");
        for (String s : names)
            ret.add(s);
        return ret;
    }

    public boolean hasInvite(String player, String team)
    {
        String adminList = teamInvites.get(player);
        if (adminList == null) return false;

        String[] names = adminList.split(":");
        for (String s : names)
        {
            if (team.equalsIgnoreCase(s)) return true;
        }
        return false;
    }

    public void removeFromInvites(String player, String team)
    {
        String admins = "";
        if (teamInvites.containsKey(player))
        {
            admins = teamInvites.get(player);
            String[] list = admins.split(":");
            for (int i = 0; i < list.length; i++)
            {
                if (list[i].equals(team))
                {
                    list[i] = null;
                }
            }
            admins = "";
            for (String s : list)
            {
                if (s != null) admins += s + ":";
            }
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

    public boolean isAdmin(String name, String adminList)
    {
        String[] names = adminList.split(":");
        for (String s : names)
        {
            if (name.equalsIgnoreCase(s)) return true;
        }
        return false;
    }

    public boolean isAdmin(String name, Team team)
    {
        String adminList = teamAdmins.get(team.getRegisteredName());
        if (adminList == null) return false;

        String[] names = adminList.split(":");
        for (String s : names)
        {
            if (name.equalsIgnoreCase(s)) return true;
        }
        return false;
    }

    public void createTeam(EntityPlayer player, String team)
    {
        for (Object o : player.worldObj.getScoreboard().getTeamNames())
        {
            String s = (String) o;
            if (s.equalsIgnoreCase(team))
            {
                player.addChatMessage(new ChatComponentText("Team " + team + " Already Exists"));
                return;
            }
        }
        if (player.worldObj.getScoreboard().getTeam(team) == null)
        {
            player.worldObj.getScoreboard().createTeam(team);
            addToTeam(player, team);
            addToAdmins(player.getName(), team);
        }
    }

    public void addToTeam(EntityPlayer player, String team)
    {
        player.worldObj.getScoreboard().addPlayerToTeam(player.getName(), team);
        player.addChatMessage(new ChatComponentText("You joined Team " + team));
        teamInvites.remove(player.getName());
    }

    public String getLandOwner(ChunkCoordinate land)
    {
        return landMap.get(land);
    }

    public List<ChunkCoordinate> getTeamLand(String team)
    {
        ArrayList<ChunkCoordinate> ret = new ArrayList<ChunkCoordinate>();

        for (ChunkCoordinate c : landMap.keySet())
        {
            if (landMap.get(c).equals(team)) ret.add(c);
        }

        return ret;
    }

    public void addTeamLand(String team, ChunkCoordinate land)
    {
        landCounts.put(team, landCounts.containsKey(team) ? landCounts.get(team) + 1 : 1);
        if (landMap.containsKey(land))
        {
            String old = landMap.get(land);
            if (landCounts.containsKey(old))
            {
                landCounts.put(old, landCounts.get(old) - 1);
                if (landCounts.get(old) <= 0) landCounts.remove(old);
            }
        }
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER
                && FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
        {
            PacketBuffer message = new PacketBuffer(Unpooled.buffer());
            message.writeByte(6);
            message.writeByte(PacketPokeAdv.TYPEADDLAND);
            land.writeToBuffer(message);
            message.writeString(team);
            MessageClient packet = new MessageClient(message);
            PokecubePacketHandler.sendToAll(packet);
        }
        landMap.put(land, team);
        PASaveHandler.getInstance().saveTeams();
    }

    public void addTeamLand2(String team, ChunkCoordinate land)
    {
        landCounts.put(team, landCounts.containsKey(team) ? landCounts.get(team) + 1 : 1);
        if (landMap.containsKey(land))
        {
            String old = landMap.get(land);
            if (landCounts.containsKey(old))
            {
                landCounts.put(old, landCounts.get(old) - 1);
                if (landCounts.get(old) <= 0) landCounts.remove(old);
            }
        }
        landMap.put(land, team);
    }

    public void removeTeamLand(String team, ChunkCoordinate land)
    {
        if (landMap.get(land) != null && landMap.get(land).equals(team))
        {
            if (landCounts.containsKey(land))
                landCounts.put(team, landCounts.containsKey(team) ? landCounts.get(team) - 1 : 0);
            landMap.remove(land);
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
            PASaveHandler.getInstance().saveTeams();
        }
    }

    public boolean isOwned(ChunkCoordinate land)
    {
        return landMap.containsKey(land);
    }

    public boolean isTeamLand(ChunkCoordinate chunk, String team)
    {
        if (landMap.containsKey(chunk)) { return landMap.get(chunk).equals(team); }
        return false;
    }

    public boolean isPublic(ChunkCoordinate c)
    {
        System.out.println(publicBlocks + " " + c + " " + publicBlocks.contains(c));
        return publicBlocks.contains(c);
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
        PASaveHandler.getInstance().saveTeams();
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
        PASaveHandler.getInstance().saveTeams();
    }

    public void saveToNBT(NBTTagCompound nbt, boolean land)
    {
        NBTTagList taglist = new NBTTagList();
        if (land) for (ChunkCoordinate c : landMap.keySet())
        {
            if (c != null && landMap.get(c) != null)
            {
                NBTTagCompound landTag = new NBTTagCompound();
                landTag.setIntArray("Location", new int[] { c.getX(), c.getY(), c.getZ(), c.dim });
                landTag.setString("Team", landMap.get(c));
                taglist.appendTag(landTag);
            }
        }
        nbt.setTag("LandMap", taglist);

        NBTTagList tagadmins = new NBTTagList();
        for (String s : teamAdmins.keySet())
        {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Team", s);
            tag.setString("Admins", teamAdmins.get(s));
            tagadmins.appendTag(tag);
        }
        nbt.setTag("Admins", tagadmins);

        NBTTagList taginvites = new NBTTagList();
        for (String s : teamInvites.keySet())
        {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Player", s);
            tag.setString("Team", teamAdmins.get(s));
            taginvites.appendTag(tag);
        }
        nbt.setTag("Invites", taginvites);

        NBTTagList tagpublic = new NBTTagList();
        for (ChunkCoordinate c : publicBlocks)
        {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setIntArray("Location", new int[] { c.getX(), c.getY(), c.getZ(), c.dim });
            tagpublic.appendTag(tag);
        }
        nbt.setTag("PublicBlocks", tagpublic);

    }

    public void loadFromNBT(NBTTagCompound nbt)
    {
        if (!(nbt.getTag("LandMap") instanceof NBTTagList)) return;

        NBTTagList tagList = (NBTTagList) nbt.getTag("LandMap");
        for (int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound landTag = tagList.getCompoundTagAt(i);
            int[] loc = landTag.getIntArray("Location");
            String team = landTag.getString("Team");
            if (loc.length != 4 || team.isEmpty()) continue;
            ChunkCoordinate c = new ChunkCoordinate(loc[0], loc[1], loc[2], loc[3]);
            addTeamLand2(team, c);
        }
        if (!(nbt.getTag("Admins") instanceof NBTTagList)) return;

        tagList = (NBTTagList) nbt.getTag("Admins");
        for (int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound adminTag = tagList.getCompoundTagAt(i);
            teamAdmins.put(adminTag.getString("Team"), adminTag.getString("Admins"));
        }
        if (!(nbt.getTag("Invites") instanceof NBTTagList)) return;

        tagList = (NBTTagList) nbt.getTag("Invites");
        for (int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound adminTag = tagList.getCompoundTagAt(i);
            teamInvites.put(adminTag.getString("Player"), adminTag.getString("Team"));
        }
        if (!(nbt.getTag("PublicBlocks") instanceof NBTTagList)) return;

        tagList = (NBTTagList) nbt.getTag("PublicBlocks");
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
