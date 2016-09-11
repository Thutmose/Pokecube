package pokecube.core.world.dimensions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import pokecube.core.handlers.PlayerDataHandler;
import pokecube.core.world.dimensions.secretpower.WorldTypeSecretPower;
import thut.api.entity.Transporter;
import thut.api.maths.Vector3;

public class PokecubeDimensionManager
{
    // TODO keep a list of all registered dimensions for players, and load this
    // list on server start, save and clear it on server close. This will be
    // needed to allow players to log into another dimension without causing
    // "Cannot Hotload Dim: Could not get provider type for dimension 2, does
    // not exist"

    public static int getDimensionForPlayer(EntityPlayer player)
    {
        return getDimensionForPlayer(player.getCachedUniqueIdString());
    }

    private static int getDimensionForPlayer(String player)
    {
        int dim = 0;
        NBTTagCompound tag = PlayerDataHandler.getCustomDataTag(player);
        if (tag.hasKey("secretPowerDimID"))
        {
            dim = tag.getInteger("secretPowerDimID");
        }
        else
        {
            dim = DimensionManager.getNextFreeDimId();
            tag.setInteger("secretPowerDimID", dim);
        }
        return dim;
    }

    public static BlockPos getBaseEntrance(EntityPlayer player, int dim)
    {
        BlockPos ret = null;
        NBTTagCompound tag = PlayerDataHandler.getCustomDataTag(player);
        if (tag.hasKey("secretBase"))
        {
            NBTTagCompound base = tag.getCompoundTag("secretBase");
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
    }

    public static void initPlayerBase(EntityPlayer player, BlockPos pos)
    {
        setBaseEntrance(player, player.dimension, pos);
        int dim = getDimensionForPlayer(player);
        if (!DimensionManager.isDimensionRegistered(dim))
            DimensionManager.registerDimension(dim, DimensionType.OVERWORLD);
        WorldServer old = DimensionManager.getWorld(dim);
        if (old == null)
        {
            WorldServer overworld = DimensionManager.getWorld(0);
            MinecraftServer mcServer = overworld.getMinecraftServer();
            ISaveHandler savehandler = overworld.getSaveHandler();
            WorldType type = new WorldTypeSecretPower();
            WorldSettings settings = new WorldSettings(overworld.getSeed(), overworld.getWorldInfo().getGameType(),
                    false, false, type);
            WorldInfo info = new WorldInfo(settings, "sp");
            WorldServer delegate = new WorldServer(mcServer, savehandler, info, dim, mcServer.theProfiler);
            WorldServer world1 = (WorldServer) (new WorldServerMulti(mcServer, savehandler, dim, delegate,
                    mcServer.theProfiler).init());
            world1.addEventListener(new ServerWorldEventHandler(mcServer, world1));
            MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(world1));
            if (!mcServer.isSinglePlayer())
            {
                world1.getWorldInfo().setGameType(mcServer.getGameType());
            }
            mcServer.setDifficultyForAllWorlds(mcServer.getDifficulty());
        }
    }

    public static void sendToBase(String baseOwner, EntityPlayer toSend, BlockPos... optionalDefault)
    {
        int dim = getDimensionForPlayer(baseOwner);
        if (!DimensionManager.isDimensionRegistered(dim))
            DimensionManager.registerDimension(dim, DimensionType.OVERWORLD);
        WorldServer old = DimensionManager.getWorld(dim);
        Vector3 spawnPos = Vector3.getNewVector().set(0, 64, 0);
        if (old == null && toSend.getCachedUniqueIdString().equals(baseOwner))
        {
            BlockPos pos = toSend.getEntityWorld().getSpawnPoint();
            if (optionalDefault.length > 0) pos = optionalDefault[0];
            initPlayerBase(toSend, pos);
        }
        else if (old == null) { return; }
        Entity sent = Transporter.teleportEntity(toSend, spawnPos, dim, false);
        // TODO make a platform here if not here already.
    }
}
