package pokecube.core.world.dimensions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.handlers.PlayerDataHandler;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.network.packets.PacketSyncDimIds;
import pokecube.core.world.dimensions.secretpower.WorldProviderSecretBase;
import thut.api.entity.Transporter;
import thut.api.maths.Vector3;

public class PokecubeDimensionManager
{
    private static PokecubeDimensionManager INSTANCE;
    public static DimensionType             SECRET_BASE_TYPE;

    public static void createNewSecretBaseDimension(int dim)
    {
        if (!DimensionManager.isDimensionRegistered(dim)) DimensionManager.registerDimension(dim, SECRET_BASE_TYPE);
        WorldServer overworld = DimensionManager.getWorld(0);

        WorldServer world1 = DimensionManager.getWorld(dim);
        if (world1 == null)
        {
            MinecraftServer mcServer = overworld.getMinecraftServer();
            ISaveHandler savehandler = overworld.getSaveHandler();
            world1 = (WorldServer) (new WorldServerMulti(mcServer, savehandler, dim, overworld, mcServer.theProfiler)
                    .init());
            world1.getWorldBorder().setSize(32);
            world1.addEventListener(new ServerWorldEventHandler(mcServer, world1));
            MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(world1));
            mcServer.setDifficultyForAllWorlds(mcServer.getDifficulty());
            registerDim(dim);
            SpawnHandler.dimensionBlacklist.add(dim);
            PokecubeCore.core.getConfig().save();
            getInstance().syncToAll();
        }
        for (int i = -2; i <= 2; i++)
        {
            for (int j = -2; j <= 2; j++)
            {
                for (int k = -2; k <= 0; k++)
                {
                    world1.setBlockState(new BlockPos(i, k + 63, j), Blocks.STONE.getDefaultState());
                }
            }
        }
    }

    public static int getDimensionForPlayer(EntityPlayer player)
    {
        return getDimensionForPlayer(player.getCachedUniqueIdString());
    }

    public static int getDimensionForPlayer(String player)
    {
        int dim = 0;
        NBTTagCompound tag = PlayerDataHandler.getCustomDataTag(player);
        System.out.println(tag);
        if (tag.hasKey("secretPowerDimID"))
        {
            dim = tag.getInteger("secretPowerDimID");
        }
        else
        {
            dim = DimensionManager.getNextFreeDimId();
            tag.setInteger("secretPowerDimID", dim);
            PlayerDataHandler.saveCustomData(player);
            Thread.dumpStack();
        }
        return dim;
    }

    public static BlockPos getBaseEntrance(EntityPlayer player, int dim)
    {
        return getBaseEntrance(player.getCachedUniqueIdString(), dim);
    }

    public static BlockPos getBaseEntrance(String player, int dim)
    {
        BlockPos ret = null;
        NBTTagCompound tag = PlayerDataHandler.getCustomDataTag(player);
        if (tag.hasKey("secretBase"))
        {
            NBTTagCompound base = tag.getCompoundTag("secretBase");
            if (base.hasKey(dim + "X"))
                ret = new BlockPos(base.getInteger(dim + "X"), base.getInteger(dim + "Y"), base.getInteger(dim + "Z"));
        }
        return ret;
    }

    public static void setBaseEntrance(EntityPlayer player, int dim, BlockPos pos)
    {
        NBTTagCompound tag = PlayerDataHandler.getCustomDataTag(player);
        NBTTagCompound base;
        if (tag.hasKey("secretBase"))
        {
            base = tag.getCompoundTag("secretBase");
        }
        else
        {
            base = new NBTTagCompound();
        }
        base.setInteger(dim + "X", pos.getX());
        base.setInteger(dim + "Y", pos.getY());
        base.setInteger(dim + "Z", pos.getZ());
        tag.setTag("secretBase", base);
        PlayerDataHandler.saveCustomData(player);
    }

    public static void initPlayerBase(EntityPlayer player, BlockPos pos)
    {
        setBaseEntrance(player, player.dimension, pos);
        int dim = getDimensionForPlayer(player);
        if (!DimensionManager.isDimensionRegistered(dim) || DimensionManager.getWorld(dim) == null)
        {
            createNewSecretBaseDimension(dim);
        }
    }

    public static void sendToBase(String baseOwner, EntityPlayer toSend, BlockPos... optionalDefault)
    {
        int dim = getDimensionForPlayer(baseOwner);
        WorldServer old = DimensionManager.getWorld(dim);
        Vector3 spawnPos = Vector3.getNewVector().set(0, 64, 0);
        if (old == null && toSend.getCachedUniqueIdString().equals(baseOwner))
        {
            BlockPos pos = toSend.getEntityWorld().getSpawnPoint();
            if (optionalDefault.length > 0) pos = optionalDefault[0];
            initPlayerBase(toSend, pos);
        }
        else if (old == null) { return; }
        Transporter.teleportEntity(toSend, spawnPos, dim, false);
    }

    public static PokecubeDimensionManager getInstance()
    {
        return INSTANCE == null ? INSTANCE = new PokecubeDimensionManager() : INSTANCE;
    }

    public static boolean registerDim(int dim)
    {
        return getInstance().dims.add(dim);
    }

    Set<Integer> dims = Sets.newHashSet();

    public PokecubeDimensionManager()
    {
        MinecraftForge.EVENT_BUS.register(this);
        int id = -1;
        for (DimensionType type : DimensionType.values())
        {
            if (type.getId() > id)
            {
                id = type.getId();
            }
        }
        id++;
        PokecubeMod.log("Registering Pokecube Secret Base Dimension type at id " + id);
        SECRET_BASE_TYPE = DimensionType.register("pokecube_secretbase", "_pokecube", id, WorldProviderSecretBase.class,
                false);
    }

    @SubscribeEvent
    public void onWorldSave(WorldEvent.Save evt)
    {
        if (evt.getWorld().provider.getDimension() == 0)
        {
            NBTTagCompound nbttagcompound = getTag();
            ISaveHandler saveHandler = evt.getWorld().getSaveHandler();
            File file = saveHandler.getMapFileFromName("PokecubeDimensionIDs");
            try
            {
                FileOutputStream fileoutputstream = new FileOutputStream(file);
                CompressedStreamTools.writeCompressed(nbttagcompound, fileoutputstream);
                fileoutputstream.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load evt)
    {
        if (evt.getWorld().provider instanceof WorldProviderSecretBase)
        {
            ((WorldProviderSecretBase) evt.getWorld().provider).onWorldLoad();
        }
    }

    @SubscribeEvent
    public void playerInitialSync(FMLNetworkEvent.ServerConnectionFromClientEvent event)
    {
        PacketSyncDimIds packet = new PacketSyncDimIds();
        packet.data = getTag();
        PokecubeMod.packetPipeline.sendTo(packet, event.getManager().channel());
    }

    @SubscribeEvent
    public void playerChangeDimension(PlayerChangedDimensionEvent event)
    {
        World world = event.player.getEntityWorld();
        if (!world.isRemote)
        {
            PacketSyncDimIds packet = new PacketSyncDimIds();
            packet.data.setInteger("dim", event.player.dimension);
            packet.data.setInteger("border", world.getWorldBorder().getSize());
            PokecubeMod.packetPipeline.sendTo(packet, (EntityPlayerMP) event.player);
        }
    }

    @SubscribeEvent
    public void playerLoggin(PlayerLoggedInEvent event)
    {
        World world = event.player.getEntityWorld();
        if (!world.isRemote)
        {
            PacketSyncDimIds packet = new PacketSyncDimIds();
            packet.data.setInteger("dim", event.player.dimension);
            packet.data.setInteger("border", world.getWorldBorder().getSize());
            PokecubeMod.packetPipeline.sendTo(packet, (EntityPlayerMP) event.player);
        }
    }

    public void onServerStop(FMLServerStoppingEvent event)
    {
    }

    public void onServerStart(FMLServerStartingEvent evt) throws IOException
    {
        ISaveHandler saveHandler = evt.getServer().getEntityWorld().getSaveHandler();
        File file = saveHandler.getMapFileFromName("PokecubeDimensionIDs");
        dims.clear();
        if (file != null && file.exists())
        {
            FileInputStream fileinputstream = new FileInputStream(file);
            NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(fileinputstream);
            fileinputstream.close();
            loadFromTag(nbttagcompound);
        }
    }

    private NBTTagCompound getTag()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        int[] dim = new int[dims.size()];
        int n = 0;
        for (int i : dims)
        {
            dim[n++] = i;
            if (!DimensionManager.isDimensionRegistered(i))
            {
                DimensionType type = SECRET_BASE_TYPE;
                if (nbttagcompound.hasKey("types"))
                {
                    NBTTagCompound typesTag = nbttagcompound.getCompoundTag("types");
                    type = DimensionType.valueOf(typesTag.getString("dim-" + i));
                }
                DimensionManager.registerDimension(i, type);
            }
        }
        nbttagcompound.setIntArray("dims", dim);
        NBTTagCompound ret = new NBTTagCompound();
        ret.setTag("Data", nbttagcompound);
        return ret;
    }

    public void loadFromTag(NBTTagCompound nbttagcompound)
    {
        nbttagcompound = nbttagcompound.getCompoundTag("Data");
        int[] nums = nbttagcompound.getIntArray("dims");
        dims.clear();
        for (int i : nums)
        {
            dims.add(i);
            if (!DimensionManager.isDimensionRegistered(i))
            {
                DimensionType type = SECRET_BASE_TYPE;
                if (nbttagcompound.hasKey("types"))
                {
                    NBTTagCompound typesTag = nbttagcompound.getCompoundTag("types");
                    type = DimensionType.valueOf(typesTag.getString("dim-" + i));
                }
                DimensionManager.registerDimension(i, type);
            }
        }
    }

    public void syncToAll()
    {
        PacketSyncDimIds packet = new PacketSyncDimIds();
        packet.data = getTag();
        PokecubeMod.packetPipeline.sendToAll(packet);
    }
}
