/**
 *
 */
package pokecube.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
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
import pokecube.core.contributors.Contributor;
import pokecube.core.contributors.ContributorManager;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.playerdata.PokecubePlayerData;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
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
            int index = nbt.getInteger("i");
            return new TeleDest(loc).setName(name).setIndex(index);
        }

        public Vector4 loc;
        Vector3        subLoc;
        String         name;
        public int     index;

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

        public TeleDest setIndex(int index)
        {
            this.index = index;
            return this;
        }

        public void writeToNBT(NBTTagCompound nbt)
        {
            loc.writeToNBT(nbt);
            nbt.setString("name", name);
            nbt.setInteger("i", index);
        }
    }

    private static final String      POKECUBE       = "pokecube";
    private static final String      DATA           = "data";
    private static final String      METEORS        = "meteors";
    private static final String      LASTUID        = "lastUid";

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
        World world = PokecubeCore.proxy.getWorld();
        String serverId = PokecubeCore.proxy.getFolderName();
        if (world == null
                || world.isRemote) { return instance == null ? instance = new PokecubeSerializer(server) : instance; }

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
                instance.myWorld = PokecubeCore.proxy.getWorld();
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

    ISaveHandler                                       saveHandler;
    public ArrayList<Vector4>                          meteors;

    public HashMap<Integer, HashMap<BlockPos, Ticket>> chunks;
    private int                                        lastId = 0;

    public World                                       myWorld;

    private String                                     serverId;

    private PokecubeSerializer(MinecraftServer server)
    {
        /** This data is saved to surface world's folder. */
        myWorld = PokecubeCore.proxy.getWorld();
        serverId = PokecubeCore.proxy.getFolderName();
        if (myWorld != null) saveHandler = myWorld.getSaveHandler();
        lastId = 0;
        meteors = new ArrayList<Vector4>();
        chunks = new HashMap<>();
        loadData();
    }

    public void addChunks(World world, BlockPos location, EntityLivingBase placer)
    {
        if (!PokecubeCore.core.getConfig().chunkLoadPokecenters) return;

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
                Ticket ticket;
                if (placer instanceof EntityPlayer)
                {
                    ticket = ForgeChunkManager.requestPlayerTicket(PokecubeCore.instance,
                            placer.getCachedUniqueIdString(), world, ForgeChunkManager.Type.NORMAL);
                    NBTTagCompound pos = new NBTTagCompound();
                    pos.setInteger("x", location.getX());
                    pos.setInteger("y", location.getY());
                    pos.setInteger("z", location.getZ());
                    ticket.getModData().setTag("pos", pos);
                    ChunkPos chunk = world.getChunkFromBlockCoords(location).getPos();
                    PokecubeMod.log("Forcing Chunk at " + location);
                    ForgeChunkManager.forceChunk(ticket, chunk);
                    tickets.put(location, ticket);
                }
            }
        }
        catch (Throwable e)
        {
            PokecubeMod.log(Level.WARNING, "Error adding chunks to load.", new Exception(e));
        }
        saveData();
    }

    public void addMeteorLocation(Vector4 v)
    {
        meteors.add(v);
        save();
    }

    public boolean canMeteorLand(Vector4 location)
    {
        for (Vector4 v : meteors)
        {
            if (tooClose(location, v)) return false;
        }
        return true;
    }

    public static double distSq(Vector4 location, Vector4 meteor)
    {
        double dx = location.x - meteor.x;
        double dy = location.y - meteor.y;
        double dz = location.z - meteor.z;
        return dx * dx + dy * dy + dz * dz;
    }

    private boolean tooClose(Vector4 location, Vector4 meteor)
    {
        if (location.w != meteor.w) return false;
        return distSq(location, meteor) < MeteorDistance;
    }

    public void clearInstance()
    {
        if (instance == null) return;
        instance.save();
        PokecubeItems.times = new Vector<Long>();
        instance = null;
    }

    public int getNextID()
    {
        return lastId++;
    }

    public boolean hasStarter(EntityPlayer player)
    {
        return PokecubePlayerDataHandler.getInstance().getPlayerData(player).getData(PokecubePlayerData.class)
                .hasStarter();
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
        NBTBase temp;
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
                        Vector4 location;
                        // TODO remove this in a few versions.
                        if (pokemobData.hasKey(METEORS + "x"))
                        {
                            int posX = pokemobData.getInteger(METEORS + "x");
                            int posY = pokemobData.getInteger(METEORS + "y");
                            int posZ = pokemobData.getInteger(METEORS + "z");
                            int w = pokemobData.getInteger(METEORS + "w");
                            location = new Vector4(posX, posY, posZ, w);
                        }
                        else location = new Vector4(pokemobData);
                        if (location != null && !location.isEmpty())
                        {
                            for (Vector4 v : meteors)
                            {
                                if (distSq(location, v) < 4) continue meteors;
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
            PokecubePlayerDataHandler.getInstance().getPlayerData(player).getData(PokecubePlayerData.class)
                    .setHasStarter(value);
        }
        catch (Exception e)
        {
            PokecubeMod.log(Level.WARNING, "Error setting has starter state for " + player, e);
        }
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
            PokecubePlayerDataHandler.getInstance().save(player.getCachedUniqueIdString());
    }

    public ItemStack starter(PokedexEntry entry, EntityPlayer owner)
    {
        World worldObj = owner.getEntityWorld();
        IPokemob entity = CapabilityPokemob.getPokemobFor(PokecubeMod.core.createPokemob(entry, worldObj));

        if (entity != null)
        {
            entity.setForSpawn(Tools.levelToXp(entity.getExperienceMode(), 5));
            entity.getEntity().setHealth(entity.getEntity().getMaxHealth());
            entity.setPokemonOwner(owner);
            Contributor contrib = ContributorManager.instance().getContributor(owner.getGameProfile());
            if (contrib != null)
            {
                entity.setPokecube(contrib.getStarterCube());
            }
            else entity.setPokecube(new ItemStack(PokecubeItems.getFilledCube(PokecubeBehavior.DEFAULTCUBE)));
            ItemStack item = PokecubeManager.pokemobToItem(entity);
            entity.getEntity().isDead = true;
            return item;
        }
        return ItemStack.EMPTY;
    }

    public void writeToNBT(NBTTagCompound nbttagcompound)
    {
        nbttagcompound.setInteger(LASTUID, lastId);
        NBTTagList tagListMeteors = new NBTTagList();
        for (Vector4 v : meteors)
        {
            if (v != null && !v.isEmpty())
            {
                NBTTagCompound nbt = new NBTTagCompound();
                v.writeToNBT(nbt);
                tagListMeteors.appendTag(nbt);
            }
        }
        nbttagcompound.setTag(METEORS, tagListMeteors);
        NBTTagCompound tms = new NBTTagCompound();
        PokecubeItems.saveTime(tms);
        nbttagcompound.setTag("tmtags", tms);
    }
}
