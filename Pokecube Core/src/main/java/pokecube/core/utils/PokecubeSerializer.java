/**
 *
 */
package pokecube.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.healtable.TileHealTable;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;

/** @author Manchou */
public class PokecubeSerializer
{
    public static class TeleDest
    {
        public static TeleDest readFromNBT(NBTTagCompound nbt)
        {
            Vector4 loc = new Vector4(nbt);
            String name = nbt.getString("name");

            return new TeleDest(loc).setName(name);
        }

        public Vector4 loc;
        Vector3        subLoc;

        String         name;

        public TeleDest(Vector4 loc)
        {
            this.loc = loc;
            subLoc = Vector3.getNewVector().set(loc.x, loc.y, loc.z);
            name = loc.toIntString();
        }

        public int getDim()
        {
            return (int) loc.w;
        }

        public Vector3 getLoc()
        {
            return subLoc;
        }

        public String getName()
        {
            return name;
        }

        public TeleDest setName(String name)
        {
            this.name = name;
            return this;
        }

        public void writeToNBT(NBTTagCompound nbt)
        {
            loc.writeToNBT(nbt);
            nbt.setString("name", name);
        }
    }

    private static final String      POKECUBE       = "pokecube";
    private static final String      DATA           = "data";
    private static final String      HASSTARTER     = "hasStarter";
    private static final String      TPOPTIONS      = "tpOptions";
    private static final String      METEORS        = "meteors";
    private static final String      LASTUID        = "lastUid";
    public static final String       USERNAME       = "username";
    public static final String       EXP            = "exp";
    public static final String       SEXE           = "sexe";
    public static final String       POKEDEXNB      = "pokedexNb";
    public static final String       STATUS         = "status";

    public static final String       NICKNAME       = "nickname";
    public static final String       EVS            = "EVS";
    public static final String       IVS            = "IVS";
    public static final String       MOVES          = "MOVES";

    public static final byte[]       noEVs          = new byte[] { Byte.MIN_VALUE, Byte.MIN_VALUE, Byte.MIN_VALUE,
            Byte.MIN_VALUE, Byte.MIN_VALUE, Byte.MIN_VALUE };

    public static int                MeteorDistance = 3000 * 3000;

    public static PokecubeSerializer instance       = null;

    public static int[] byteArrayAsIntArray(byte[] stats)
    {
        if (stats.length != 6) return new int[] { 0, 0 };

        int value0 = 0;
        for (int i = 3; i >= 0; i--)
        {
            value0 = (value0 << 8) + (stats[i] & 0xff);
        }

        int value1 = 0;
        for (int i = 5; i >= 4; i--)
        {
            value1 = (value1 << 8) + (stats[i] & 0xff);
        }

        return new int[] { value0, value1 };
    }

    public static Long byteArrayAsLong(byte[] stats)
    {
        if (stats.length != 6) { return 0L; }

        long value = 0;
        for (int i = stats.length - 1; i >= 0; i--)
        {
            value = (value << 8) + (stats[i] & 0xff);
        }
        return value;
    }

    public static PokecubeSerializer getInstance()
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        String serverId = PokecubeCore.proxy.getFolderName();
        if (PokecubeCore
                .isOnClientSide()) { return instance == null ? instance = new PokecubeSerializer(server) : instance; }

        if (instance == null || instance.serverId == null || !instance.serverId.equals(serverId))
        {
            boolean toNew = false;

            toNew = (instance == null || instance.saveHandler == null);

            if (!toNew)
            {
                File file = instance.saveHandler.getMapFileFromName(POKECUBE);

                if ((file == null))
                {
                    instance = new PokecubeSerializer(server);
                }
            }

            if (!toNew)
            {
                instance.myWorld = PokecubeCore.proxy.getWorld();// surface
                                                                 // world folder
                                                                 // will be used
                                                                 // to save the
                                                                 // file
                serverId = PokecubeCore.proxy.getFolderName();
                if (instance.myWorld != null) instance.saveHandler = instance.myWorld.getSaveHandler();
            }
            if (toNew)
            {
                instance = new PokecubeSerializer(server);
            }
        }

        return instance;
    }

    public static byte[] intArrayAsByteArray(int[] ints)
    {
        byte[] stats = new byte[] { (byte) ((ints[0] & 0xFF)), (byte) ((ints[0] >> 8 & 0xFF)),
                (byte) ((ints[0] >> 16 & 0xFF)), (byte) ((ints[0] >> 24 & 0xFF)), (byte) ((ints[1] & 0xFF)),
                (byte) ((ints[1] >> 8 & 0xFF)) };
        return stats;
    }

    public static byte[] intAsModifierArray(int ints)
    {
        byte[] stats = new byte[] { (byte) ((ints & 15) - 6), (byte) ((ints >> 4 & 15) - 6),
                (byte) ((ints >> 8 & 15) - 6), (byte) ((ints >> 12 & 15) - 6), (byte) ((ints >> 16 & 15) - 6),
                (byte) ((ints >> 20 & 15) - 6), (byte) ((ints >> 24 & 15) - 6), (byte) ((ints >> 28 & 15) - 6) };
        return stats;
    }

    public static byte[] longAsByteArray(Long l)
    {
        byte[] stats = new byte[] { (byte) ((l & 0xFFL)), (byte) ((l >> 8 & 0xffL)), (byte) ((l >> 16 & 0xFFL)),
                (byte) ((l >> 24 & 0xFFL)), (byte) ((l >> 32 & 0xFFL)), (byte) ((l >> 40 & 0xFFL)) };
        return stats;
    }

    public static int modifierArrayAsInt(byte[] stats)
    {
        if (stats.length != 8) return 0;
        int value = 0;
        for (int i = 7; i >= 0; i--)
        {
            value = (value << 4) + ((stats[i] + 6) & 15);
        }
        return value;
    }

    ISaveHandler                                       saveHandler;
    private HashMap<String, Boolean>                   hasStarter;
    public HashMap<String, ArrayList<TeleDest>>        teleportOptions;
    private HashMap<String, Integer>                   teleportIndex;
    private ArrayList<Vector3>                         meteors;

    public HashMap<Integer, HashMap<BlockPos, Ticket>> chunks;
    private int                                        lastId = 0;

    public World                                       myWorld;

    private String                                     serverId;

    private HashMap<Integer, IPokemob>                 pokemobsMap;

    private PokecubeSerializer(MinecraftServer server)
    {
        myWorld = PokecubeCore.proxy.getWorld();// surface world folder will be
                                                // used to save the file
        serverId = PokecubeCore.proxy.getFolderName();
        if (myWorld != null) saveHandler = myWorld.getSaveHandler();
        pokemobsMap = new HashMap<Integer, IPokemob>();
        lastId = 0;
        hasStarter = new HashMap<String, Boolean>();
        teleportOptions = new HashMap<String, ArrayList<TeleDest>>();
        teleportIndex = new HashMap<String, Integer>();
        meteors = new ArrayList<Vector3>();
        chunks = new HashMap<>();
        loadData();
    }

    public void addChunks(World world, BlockPos location)
    {
        Integer dimension = world.provider.getDimension();

        HashMap<BlockPos, Ticket> tickets = chunks.get(dimension);
        if (tickets == null)
        {
            chunks.put(dimension, tickets = new HashMap<>());
        }
        boolean found = tickets.containsKey(location);
        try
        {
            if (!found)
            {
                Ticket ticket = ForgeChunkManager.requestTicket(PokecubeCore.instance, world,
                        ForgeChunkManager.Type.NORMAL);
                NBTTagCompound pos = new NBTTagCompound();
                pos.setInteger("x", location.getX());
                pos.setInteger("y", location.getY());
                pos.setInteger("z", location.getZ());
                ticket.getModData().setTag("pos", pos);
                ChunkPos chunk = world.getChunkFromBlockCoords(location).getChunkCoordIntPair();
                ForgeChunkManager.forceChunk(ticket, chunk);
                tickets.put(location, ticket);
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        saveData();
    }

    public void addMeteorLocation(Vector3 v)
    {
        meteors.add(v);
        save();
    }

    public void addPokemob(IPokemob mob)
    {

        if (pokemobsMap.containsKey(mob.getPokemonUID())) { return; }

        pokemobsMap.put(mob.getPokemonUID(), mob);
    }

    public boolean canMeteorLand(Vector3 location)
    {
        for (Vector3 v : meteors)
        {
            if (v.distToSq(location) < MeteorDistance) return false;
        }
        return true;
    }

    public void clearInstance()
    {
        if (instance == null) return;
        instance.save();

        StatsCollector.eggsHatched.clear();
        StatsCollector.playerCaptures.clear();
        StatsCollector.playerKills.clear();
        PokecubeItems.times = new Vector<Long>();
        instance = null;
    }

    public int getNextID()
    {
        return lastId++;
    }

    public IPokemob getPokemob(int uid)
    {
        return pokemobsMap.get(uid);
    }

    public int getTeleIndex(String uuid)
    {
        if (!teleportIndex.containsKey(uuid))
        {
            teleportIndex.put(uuid, 0);
        }
        return teleportIndex.get(uuid);
    }

    public TeleDest getTeleport(String uuid)
    {
        if (!teleportOptions.containsKey(uuid))
        {
            teleportOptions.put(uuid, new ArrayList<TeleDest>());
        }
        ArrayList<TeleDest> list = teleportOptions.get(uuid);
        int index = getTeleIndex(uuid);
        TeleDest d = null;
        if (list.size() > index)
        {
            d = list.get(index);
        }
        return d;
    }

    public List<TeleDest> getTeleports(String uuid)
    {
        if (!teleportOptions.containsKey(uuid))
        {
            teleportOptions.put(uuid, new ArrayList<TeleDest>());
        }
        ArrayList<TeleDest> list = teleportOptions.get(uuid);

        return list;
    }

    public boolean hasStarter(EntityPlayer player)
    {
        Boolean bool = hasStarter.get(player.getCachedUniqueIdString());
        return bool == Boolean.TRUE;
    }

    public void loadData()
    {
        if (saveHandler != null)
        {
            try
            {
                File file = saveHandler.getMapFileFromName(POKECUBE);

                if (file != null && file.exists())
                {
                    FileInputStream fileinputstream = new FileInputStream(file);
                    NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(fileinputstream);
                    fileinputstream.close();
                    readFromNBT(nbttagcompound.getCompoundTag(DATA));
                }
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }
    }

    public void readFromNBT(NBTTagCompound nbttagcompound)
    {
        lastId = nbttagcompound.getInteger(LASTUID);

        StatsCollector.readFromNBT(nbttagcompound.getCompoundTag("globalStats"));

        NBTBase temp;

        temp = nbttagcompound.getTag(HASSTARTER);
        if (temp instanceof NBTTagList)
        {
            NBTTagList tagListHasStarter = (NBTTagList) temp;
            for (int i = 0; i < tagListHasStarter.tagCount(); i++)
            {
                NBTTagCompound pokemobData = tagListHasStarter.getCompoundTagAt(i);

                if (pokemobData != null)
                {
                    String username = pokemobData.getString(USERNAME);
                    Boolean hasStarter = pokemobData.getBoolean(HASSTARTER);
                    this.hasStarter.put(username, hasStarter);
                }
            }
        }

        temp = nbttagcompound.getTag(TPOPTIONS);
        if (temp instanceof NBTTagList)
        {
            NBTTagList tagListTeleportLocations = (NBTTagList) temp;
            if (tagListTeleportLocations.tagCount() > 0) for (int i = 0; i < tagListTeleportLocations.tagCount(); i++)
            {
                NBTTagCompound pokemobData = tagListTeleportLocations.getCompoundTagAt(i);

                if (pokemobData != null)
                {
                    String username = pokemobData.getString(USERNAME);

                    int index = pokemobData.getInteger("TPOPTION");

                    setTeleIndex(username, index);
                    NBTBase temp2 = pokemobData.getTag(TPOPTIONS);
                    if (temp2 instanceof NBTTagList)
                    {
                        NBTTagList tagListOptions = (NBTTagList) temp2;
                        NBTTagCompound pokemobData2 = null;
                        for (int j = 0; j < tagListOptions.tagCount(); j++)
                        {
                            pokemobData2 = tagListOptions.getCompoundTagAt(j);
                            TeleDest d = TeleDest.readFromNBT(pokemobData2);
                            setTeleport(username, d);
                        }

                    }
                }
            }
        }

        temp = nbttagcompound.getTag(METEORS);
        if (temp instanceof NBTTagList)
        {
            NBTTagList tagListMeteors = (NBTTagList) temp;
            if (tagListMeteors.tagCount() > 0)
            {
                meteors:
                for (int i = 0; i < tagListMeteors.tagCount(); i++)
                {
                    NBTTagCompound pokemobData = tagListMeteors.getCompoundTagAt(i);

                    if (pokemobData != null)
                    {
                        Vector3 location = Vector3.readFromNBT(pokemobData, METEORS);
                        if (location != null && !location.isEmpty())
                        {
                            for (Vector3 v : meteors)
                            {
                                if (v.distToSq(location) < 4) continue meteors;
                            }
                            meteors.add(location);
                        }
                    }
                }
            }
        }
        temp = nbttagcompound.getTag("tmtags");
        if (temp instanceof NBTTagCompound)
        {
            PokecubeItems.loadTime((NBTTagCompound) temp);
        }

    }

    public void readPlayerTeleports(NBTTagCompound tag)
    {
        String username = tag.getString(USERNAME);
        int index = tag.getInteger("TPOPTION");
        setTeleIndex(username, index);

        NBTBase temp2 = tag.getTag(TPOPTIONS);
        if (temp2 instanceof NBTTagList)
        {
            NBTTagList tagListOptions = (NBTTagList) temp2;
            NBTTagCompound pokemobData2 = null;
            for (int j = 0; j < tagListOptions.tagCount(); j++)
            {
                pokemobData2 = tagListOptions.getCompoundTagAt(j);
                TeleDest d = TeleDest.readFromNBT(pokemobData2);
                setTeleport(username, d);
            }

        }
    }

    public void reloadChunk(List<Ticket> tickets, World world)
    {
        Iterator<Ticket> next = tickets.iterator();
        while (next.hasNext())
        {
            Ticket ticket = next.next();
            if (!ticket.getModId().equals("pokecube")) continue;
            if (!ticket.getModData().hasKey("pos"))
            {
                System.out.println("invalid ticket");
                ForgeChunkManager.releaseTicket(ticket);
            }
            else
            {
                NBTTagCompound posTag = ticket.getModData().getCompoundTag("pos");
                BlockPos pos = new BlockPos(posTag.getInteger("x"), posTag.getInteger("y"), posTag.getInteger("z"));
                TileEntity tile = world.getTileEntity(pos);
                if (tile == null || !(tile instanceof TileHealTable))
                {
                    System.out.println("invalid ticket");
                    ForgeChunkManager.releaseTicket(ticket);
                }
                else ForgeChunkManager.forceChunk(ticket, new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4));
            }
        }
    }

    public void removeChunks(World world, BlockPos location)
    {
        Integer dimension = world.provider.getDimension();
        HashMap<BlockPos, Ticket> tickets = chunks.get(dimension);
        if (tickets != null)
        {
            Ticket ticket = tickets.remove(location);
            if (ticket != null)
            {
                ForgeChunkManager.releaseTicket(ticket);
            }
        }
    }

    public void removePokemob(IPokemob mob)
    {
        pokemobsMap.remove(mob.getPokemonUID());
    }

    public void save()
    {
        saveData();
    }

    private void saveData()
    {
        if (saveHandler == null || FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) { return; }

        try
        {
            File file = saveHandler.getMapFileFromName(POKECUBE);
            if (file != null)
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                writeToNBT(nbttagcompound);
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setTag(DATA, nbttagcompound);
                FileOutputStream fileoutputstream = new FileOutputStream(file);
                CompressedStreamTools.writeCompressed(nbttagcompound1, fileoutputstream);
                fileoutputstream.close();
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public void setHasStarter(EntityPlayer player)
    {
        setHasStarter(player, true);
    }

    public void setHasStarter(EntityPlayer player, boolean value)
    {
        try
        {
            this.hasStarter.put(player.getCachedUniqueIdString(), value);
        }
        catch (Exception e)
        {
            System.err.println("UUID null");
        }
        saveData();
    }

    public void setTeleIndex(String uuid, int index)
    {
        teleportIndex.put(uuid, index);
    }

    public void setTeleport(String uuid, TeleDest dest)
    {
        setTeleport(dest.loc, uuid, dest.getName());
    }

    public void setTeleport(Vector4 v, String uuid)
    {
        if (!teleportOptions.containsKey(uuid))
        {
            teleportOptions.put(uuid, new ArrayList<TeleDest>());
        }
        ArrayList<TeleDest> list = teleportOptions.get(uuid);
        boolean toRemove = false;
        ArrayList<TeleDest> old = new ArrayList<TeleDest>();

        for (TeleDest d : list)
        {
            Vector4 v1 = d.loc;
            if (v1.withinDistance(20, v))
            {
                toRemove = true;
                old.add(d);
            }
        }
        if (toRemove)
        {
            for (TeleDest d : old)
            {
                list.remove(d);
            }
        }
        TeleDest d = new TeleDest(v);
        list.add(d);
    }

    public void setTeleport(Vector4 v, String uuid, String customName)
    {
        if (!teleportOptions.containsKey(uuid))
        {
            teleportOptions.put(uuid, new ArrayList<TeleDest>());
        }
        ArrayList<TeleDest> list = teleportOptions.get(uuid);
        boolean toRemove = false;
        ArrayList<TeleDest> old = new ArrayList<TeleDest>();

        for (TeleDest d : list)
        {
            Vector4 v1 = d.loc;
            if (v1.withinDistance(20, v))
            {
                toRemove = true;
                old.add(d);
            }
        }
        if (toRemove)
        {
            for (TeleDest d : old)
            {
                list.remove(d);
            }
        }
        TeleDest d = new TeleDest(v).setName(customName);
        list.add(d);
    }

    public ItemStack starter(int pokedexNb, EntityPlayer owner)
    {
        World worldObj = owner.getEntityWorld();
        IPokemob entity = (IPokemob) PokecubeMod.core.createEntityByPokedexNb(pokedexNb, worldObj);

        if (entity != null)
        {
            entity.setExp(Tools.levelToXp(entity.getExperienceMode(), 5), true, false);
            ((EntityLivingBase) entity).setHealth(((EntityLivingBase) entity).getMaxHealth());
            entity.setPokemonOwnerByName(owner.getCachedUniqueIdString());
            entity.setPokecubeId(0);
            ItemStack item = PokecubeManager.pokemobToItem(entity);
            ((Entity) entity).isDead = true;

            EntityPlayer player = owner;

            if (player != null && !player.getEntityWorld().isRemote)
            {
                player.addStat(PokecubeMod.get1stPokemob, 1);
                player.addStat(PokecubeMod.pokemobAchievements.get(entity.getPokedexNb()), 1);
            }

            return item;
        }

        return null;
    }

    public void unsetTeleport(Vector4 v, String uuid)
    {
        if (!teleportOptions.containsKey(uuid))
        {
            teleportOptions.put(uuid, new ArrayList<TeleDest>());
        }
        ArrayList<TeleDest> list = teleportOptions.get(uuid);
        boolean toRemove = false;
        ArrayList<TeleDest> old = new ArrayList<TeleDest>();

        for (TeleDest d : list)
        {
            Vector4 v1 = d.loc;
            if (v1.withinDistance(20, v))
            {
                toRemove = true;
                old.add(d);
            }
        }
        if (toRemove)
        {
            for (TeleDest d : old)
            {
                list.remove(d);
            }
        }
    }

    public void writePlayerTeleports(UUID player, NBTTagCompound tag)
    {
        ArrayList<TeleDest> locations = teleportOptions.get(player.toString());

        if (locations == null || locations.size() == 0) return;

        tag.setString(USERNAME, player.toString());
        tag.setInteger("TPOPTION", getTeleIndex(player.toString()));
        NBTTagList list = new NBTTagList();

        for (TeleDest d : locations)
        {
            if (d != null)
            {
                NBTTagCompound loc = new NBTTagCompound();
                d.writeToNBT(loc);
                list.appendTag(loc);
            }
        }
        // System.out.println(list);
        tag.setTag(TPOPTIONS, list);
    }

    public void writeToNBT(NBTTagCompound nbttagcompound)
    {
        nbttagcompound.setInteger(LASTUID, lastId);

        NBTTagCompound statsTag = new NBTTagCompound();

        StatsCollector.writeToNBT(statsTag);
        nbttagcompound.setTag("globalStats", statsTag);

        NBTTagList tagListHasStarter = new NBTTagList();
        for (String username : hasStarter.keySet())
        {
            NBTTagCompound hasStarterTagCompound = new NBTTagCompound();
            hasStarterTagCompound.setBoolean(HASSTARTER, hasStarter.get(username));
            hasStarterTagCompound.setString(USERNAME, username);
            tagListHasStarter.appendTag(hasStarterTagCompound);
        }
        nbttagcompound.setTag(HASSTARTER, tagListHasStarter);

        NBTTagList tagListTpOp = new NBTTagList();
        for (String username : teleportOptions.keySet())
        {
            ArrayList<TeleDest> locations = teleportOptions.get(username);
            boolean valid = false;

            try
            {
                UUID.fromString(username);
                valid = true;
            }
            catch (Exception e)
            {

            }

            if (locations.size() == 0 || !valid) continue;
            NBTTagCompound tpTagCompound = new NBTTagCompound();
            tpTagCompound.setString(USERNAME, username);
            tpTagCompound.setInteger("TPOPTION", getTeleIndex(username));
            NBTTagList list = new NBTTagList();

            for (TeleDest d : locations)
            {
                if (d != null)
                {
                    NBTTagCompound loc = new NBTTagCompound();
                    d.writeToNBT(loc);
                    list.appendTag(loc);
                }
            }
            tpTagCompound.setTag(TPOPTIONS, list);
            tagListTpOp.appendTag(tpTagCompound);
        }
        nbttagcompound.setTag(TPOPTIONS, tagListTpOp);

        NBTTagList tagListMeteors = new NBTTagList();
        // int num = 0;
        for (Vector3 v : meteors)
        {
            if (v != null && !v.isEmpty())
            {
                NBTTagCompound nbt = new NBTTagCompound();
                v.writeToNBT(nbt, METEORS);
                tagListMeteors.appendTag(nbt);
            }
        }
        nbttagcompound.setTag(METEORS, tagListMeteors);

        NBTTagCompound tms = new NBTTagCompound();
        PokecubeItems.saveTime(tms);
        nbttagcompound.setTag("tmtags", tms);

    }

    public void writeToNBT2(NBTTagCompound nbttagcompound)
    {
        nbttagcompound.setInteger(LASTUID, lastId);

        NBTTagCompound statsTag = new NBTTagCompound();

        StatsCollector.writeToNBT(statsTag);
        nbttagcompound.setTag("globalStats", statsTag);

        NBTTagList tagListHasStarter = new NBTTagList();
        for (String username : hasStarter.keySet())
        {
            NBTTagCompound hasStarterTagCompound = new NBTTagCompound();
            hasStarterTagCompound.setBoolean(HASSTARTER, hasStarter.get(username));
            hasStarterTagCompound.setString(USERNAME, username);
            tagListHasStarter.appendTag(hasStarterTagCompound);
        }
        nbttagcompound.setTag(HASSTARTER, tagListHasStarter);

        NBTTagList tagListTpOp = new NBTTagList();
        for (String username : teleportOptions.keySet())
        {
            ArrayList<TeleDest> locations = teleportOptions.get(username);
            boolean valid = false;
            try
            {
                valid = true;
            }
            catch (Exception e)
            {
            }

            if (locations.size() == 0 || !valid) continue;

            NBTTagCompound tpTagCompound = new NBTTagCompound();
            tpTagCompound.setString(USERNAME, username);
            tpTagCompound.setInteger("TPOPTION", getTeleIndex(username));

            NBTTagList list = new NBTTagList();

            for (TeleDest d : locations)
            {
                if (d != null)
                {
                    NBTTagCompound loc = new NBTTagCompound();
                    d.writeToNBT(loc);
                    list.appendTag(loc);
                }
            }
            tpTagCompound.setTag(TPOPTIONS, list);
            tagListTpOp.appendTag(tpTagCompound);
        }
        nbttagcompound.setTag(TPOPTIONS, tagListTpOp);

    }

}
