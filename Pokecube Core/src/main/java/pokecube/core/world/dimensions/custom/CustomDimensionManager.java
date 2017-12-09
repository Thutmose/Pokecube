package pokecube.core.world.dimensions.custom;

import java.util.logging.Level;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.world.dimensions.PokecubeDimensionManager;

public class CustomDimensionManager
{
    public static void initDimension(int dim, String worldName, String worldType, String generatorOptions)
    {
        World overworld = DimensionManager.getWorld(0);
        if (!DimensionManager.isDimensionRegistered(dim))
        {
            DimensionManager.registerDimension(dim, DimensionType.OVERWORLD);
            PokecubeDimensionManager.registerDim(dim);
        }
        else
        {
            System.out.println(DimensionManager.getProviderType(dim));
        }
        World oldWorld = DimensionManager.getWorld(dim);
        if (generatorOptions.isEmpty()) generatorOptions = null;
        WorldInfo old = overworld.getWorldInfo();
        WorldSettings settings = new WorldSettings(overworld.getSeed(), old.getGameType(), old.isMapFeaturesEnabled(),
                old.isHardcoreModeEnabled(), WorldType.parseWorldType(worldType));
        settings.setGeneratorOptions(generatorOptions);
        WorldInfo info = new WorldInfo(settings, worldName);
        WorldServer newWorld = CustomDimensionManager.initDimension(dim, info);
        DimensionManager.setWorld(dim, newWorld, FMLCommonHandler.instance().getMinecraftServerInstance());
        if (oldWorld != null && newWorld != null)
        {
            PokecubeMod.log("Replaced " + oldWorld + " with " + newWorld);
        }
        else if (newWorld != null)
        {
            PokecubeMod.log("Set World " + newWorld);
        }
        else
        {
            PokecubeMod.log(Level.WARNING,
                    "Unable to create world " + dim + " " + worldName + " " + worldType + " " + generatorOptions);
        }
        PokecubeDimensionManager.getInstance().syncToAll();
    }

    public static WorldServer initDimension(int dim, WorldInfo info)
    {
        WorldServer overworld = DimensionManager.getWorld(0);
        if (overworld == null) { throw new RuntimeException("Cannot Hotload Dim: Overworld is not Loaded!"); }
        try
        {
            DimensionManager.getProviderType(dim);
        }
        catch (Exception e)
        {
            FMLLog.log.error("Cannot Hotload Dim: {}", dim, e);
            return null; // If a provider hasn't been registered then we can't
            // hotload the dim
        }
        MinecraftServer mcServer = overworld.getMinecraftServer();
        ISaveHandler savehandler = overworld.getSaveHandler();

        WorldServer world = (dim == 0 ? overworld
                : (WorldServer) (new WorldServerMulti(mcServer, savehandler, dim, overworld, info, mcServer.profiler)
                        .init()));
        world.addEventListener(new ServerWorldEventHandler(mcServer, world));
        MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(world));
        if (!mcServer.isSinglePlayer())
        {
            world.getWorldInfo().setGameType(mcServer.getGameType());
        }

        mcServer.setDifficultyForAllWorlds(mcServer.getDifficulty());
        PokecubeMod.log("Hotloaded Dim: " + dim);
        return world;
    }

}
