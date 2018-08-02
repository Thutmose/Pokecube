package pokecube.core.world.dimensions.custom;

import java.util.logging.Level;

import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.border.IBorderListener;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.world.dimensions.PokecubeDimensionManager;

public class CustomDimensionManager
{
    public static void initDimension(int dim, String worldName, String worldType, String generatorOptions, Long seed)
    {
        initDimension(dim, worldName, worldType, generatorOptions, DimensionType.OVERWORLD, seed);
    }

    public static void initDimension(int dim, String worldName, String worldType, String generatorOptions,
            DimensionType dimType, Long seed)
    {
        World overworld = DimensionManager.getWorld(0);
        if (overworld == null) return;
        if (dimType == null)
        {
            dimType = DimensionType.OVERWORLD;
            PokecubeMod.log(Level.WARNING, "Dimtype should not be null!", new NullPointerException());
        }
        if (!DimensionManager.isDimensionRegistered(dim))
        {
            DimensionManager.registerDimension(dim, dimType);
            PokecubeDimensionManager.registerDim(dim);
        }
        else
        {
            System.out.println(DimensionManager.getProviderType(dim));
        }
        World oldWorld = DimensionManager.getWorld(dim);
        if (generatorOptions != null && generatorOptions.isEmpty()) generatorOptions = null;
        WorldInfo old = overworld.getWorldInfo();
        WorldSettings settings = new WorldSettings(seed == null ? overworld.getSeed() : seed, old.getGameType(),
                old.isMapFeaturesEnabled(), old.isHardcoreModeEnabled(), WorldType.parseWorldType(worldType));
        settings.setGeneratorOptions(generatorOptions);
        WorldServer newWorld = CustomDimensionManager.initDimension(dim, settings, worldName);
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

    public static WorldServer initDimension(int dim, WorldSettings settings, String worldName)
    {
        DimensionManager.initDimension(dim);
        WorldServer world = DimensionManager.getWorld(dim);
        // TODO replace this with a AT.
        ReflectionHelper.setPrivateValue(World.class, world, new WorldInfo(settings, worldName), 27);
        WorldServerMulti multi = (WorldServerMulti) world;
        WorldServer delegate = ReflectionHelper.getPrivateValue(WorldServerMulti.class, multi, 0);
        IBorderListener listener = ReflectionHelper.getPrivateValue(WorldServerMulti.class, multi, 1);
        delegate.getWorldBorder().removeListener(listener);
        PokecubeMod.log("Hotloaded Dim: " + dim);
        return world;
    }

}
