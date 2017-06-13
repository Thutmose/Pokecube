package pokecube.compat.advancedrocketry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.block.material.Material;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.compat.events.TransferDimension;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.SpawnData.SpawnEntry;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.events.PostPostInit;
import pokecube.core.events.SpawnEvent;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.entity.Transporter;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;
import thut.lib.CompatClass;
import thut.lib.CompatClass.Phase;
import zmaster587.advancedRocketry.api.AdvancedRocketryAPI;
import zmaster587.advancedRocketry.api.Configuration;
import zmaster587.advancedRocketry.api.IAtmosphere;
import zmaster587.advancedRocketry.api.event.AtmosphereEvent.AtmosphereTickEvent;
import zmaster587.advancedRocketry.api.stations.ISpaceObject;
import zmaster587.advancedRocketry.dimension.DimensionManager;
import zmaster587.advancedRocketry.dimension.DimensionProperties;

public class AdvancedRocketryCompat
{
    public static String       CUSTOMSPAWNSFILE;

    private static PrintWriter out;

    private static FileWriter  fwriter;

    public static void setSpawnsFile(FMLPreInitializationEvent evt)
    {
        File file = evt.getSuggestedConfigurationFile();
        String seperator = System.getProperty("file.separator");
        String folder = file.getAbsolutePath();
        String name = file.getName();
        folder = folder.replace(name,
                "pokecube" + seperator + "compat" + seperator + "advanced_rocketry" + seperator + "spawns.xml");
        CUSTOMSPAWNSFILE = folder;
        writeDefaultSpawnsConfig();
    }

    private static void writeDefaultSpawnsConfig()
    {
        try
        {
            File temp = new File(CUSTOMSPAWNSFILE.replace("spawns.xml", ""));
            if (!temp.exists())
            {
                temp.mkdirs();
            }
            // TODO remove this once I get around to finializing
            // File temp1 = new File(CUSTOMSPAWNSFILE);
            // if (temp1.exists()) { return; }

            List<String> spawns = Lists.newArrayList();
            spawns.add("    <Spawn name=\"Lunatone\" overwrite=\"false\" "
                    + "rate=\"0.01\" min=\"1\" max=\"2\" types=\"moon\"/>");
            spawns.add("    <Spawn name=\"Solrock\" overwrite=\"false\" "
                    + "rate=\"0.01\" min=\"1\" max=\"2\" types=\"moon\"/>");
            spawns.add("    <Spawn name=\"Clefairy\" overwrite=\"false\" "
                    + "rate=\"0.2\" min=\"4\" max=\"8\" types=\"moon\"/>");
            fwriter = new FileWriter(CUSTOMSPAWNSFILE);
            out = new PrintWriter(fwriter);
            out.println("<?xml version=\"1.0\"?>");
            out.println("<Spawns>");
            for (String s : spawns)
                out.println(s);
            out.println("</Spawns>");
            out.close();
            fwriter.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private DamageSource vacuumDamage;
    private Method       getOxygenHandler;
    private Method       getAtmosphereType;
    private Method       conditionsMatch;
    private Field        blobsField;
    private Field        entryWeight;
    Set<PokedexEntry>    vacuumBreathers = Sets.newHashSet();
    List<PokedexEntry>   moonmon         = Lists.newArrayList();
    PokedexEntry         megaray;

    @Optional.Method(modid = "advancedRocketry")
    @CompatClass(takesEvent = true, phase = Phase.PRE)
    public static void ARCompatOld(FMLPreInitializationEvent evt)
    {
        MinecraftForge.EVENT_BUS.register(new pokecube.compat.advancedrocketry.AdvancedRocketryCompat(evt));
    }

    @Optional.Method(modid = "advancedrocketry")
    @CompatClass(takesEvent = true, phase = Phase.PRE)
    public static void ARCompat(FMLPreInitializationEvent evt)
    {
        MinecraftForge.EVENT_BUS.register(new pokecube.compat.advancedrocketry.AdvancedRocketryCompat(evt));
    }

    public AdvancedRocketryCompat(FMLPreInitializationEvent event)
    {
        setSpawnsFile(event);
        Database.addSpawnData(CUSTOMSPAWNSFILE);
        BiomeType.getBiome("Moon", true);
        try
        {
            Class<?> atmosphereHandler = Class.forName("zmaster587.advancedRocketry.atmosphere.AtmosphereHandler");
            Field field = atmosphereHandler.getDeclaredField("dimensionOxygen");
            field.setAccessible(true);
            getOxygenHandler = atmosphereHandler.getMethod("getOxygenHandler", int.class);
            getAtmosphereType = atmosphereHandler.getMethod("getAtmosphereType", BlockPos.class);
            blobsField = atmosphereHandler.getDeclaredField("blobs");
            blobsField.setAccessible(true);
            entryWeight = SpawnEntry.class.getDeclaredField("rate");
            entryWeight.setAccessible(true);
            conditionsMatch = SpawnBiomeMatcher.class.getDeclaredMethod("conditionsMatch", SpawnCheck.class);
            conditionsMatch.setAccessible(true);
            vacuumDamage = (DamageSource) atmosphereHandler.getDeclaredField("vacuumDamage").get(null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void postpost(PostPostInit event)
    {
        Set<String> mobs = Sets.newHashSet();// TODO make this load from a file.
        mobs.add("clefairy");
        mobs.add("clefable");
        mobs.add("lunatone");
        mobs.add("solrock");
        mobs.add("deoxys");
        mobs.add("beldum");
        mobs.add("rayquaza");
        mobs.add("rayquazamega");
        megaray = Database.getEntry("rayquazamega");
        for (String s : mobs)
        {
            if (Database.getEntry(s) != null) vacuumBreathers.add(Database.getEntry(s));
        }
    }

    @SubscribeEvent
    public void spawn(SpawnEvent.Check event) throws Exception
    {
        if (!event.forSpawn) return;
        Biome biome = event.location.getBiome(event.world);
        Biome moon = Biome.REGISTRY.getObject(new ResourceLocation("Moon"));
        if (biome == moon)
        {
            BiomeType moonType = BiomeType.getBiome("Moon", true);
            PokedexEntry dbe = event.entry;
            if (dbe.getSpawnData().isValid(moonType))
            {
                Vector3 v = event.location;
                World world = event.world;
                SpawnCheck checker = new SpawnCheck(v, world);
                SpawnBiomeMatcher match = null;
                for (SpawnBiomeMatcher matcher : dbe.getSpawnData().matchers.keySet())
                {
                    if (matcher.validSubBiomes.contains(moonType))
                    {
                        match = matcher;
                        break;
                    }
                }
                if (((boolean) conditionsMatch.invoke(match, checker))) event.setResult(Result.ALLOW);
            }
        }
    }

    @SubscribeEvent
    public void spawn(SpawnEvent.Pick.Pre event) throws Exception
    {
        Biome biome = event.location.getBiome(event.world);
        Biome moon = Biome.REGISTRY.getObject(new ResourceLocation("Moon"));
        if (biome == moon)
        {
            BiomeType moonType = BiomeType.getBiome("Moon", true);
            if (moonmon.isEmpty())
            {
                for (PokedexEntry e : Database.spawnables)
                {
                    if (e.getSpawnData().isValid(moonType))
                    {
                        moonmon.add(e);
                    }
                }
            }
            event.setPick(null);
            Collections.shuffle(moonmon);
            int index = 0;
            Vector3 v = event.getLocation();
            World world = event.world;
            PokedexEntry dbe = moonmon.get(index);
            SpawnEntry entry = null;
            SpawnCheck checker = new SpawnCheck(v, world);
            SpawnBiomeMatcher match = null;
            for (SpawnBiomeMatcher matcher : dbe.getSpawnData().matchers.keySet())
            {
                if (matcher.validSubBiomes.contains(moonType))
                {
                    entry = dbe.getSpawnData().matchers.get(matcher);
                    match = matcher;
                    break;
                }
            }
            if (entry == null) return;
            float weight = entryWeight.getFloat(entry);
            if (!((boolean) conditionsMatch.invoke(match, checker))) weight = 0;
            double random = Math.random();
            int max = moonmon.size();
            Vector3 vbak = v.copy();
            while (weight <= random && index++ < max)
            {
                dbe = moonmon.get(index % moonmon.size());
                for (SpawnBiomeMatcher matcher : dbe.getSpawnData().matchers.keySet())
                {
                    if (matcher.validSubBiomes.contains(moonType))
                    {
                        entry = dbe.getSpawnData().matchers.get(matcher);
                        match = matcher;
                        break;
                    }
                }
                if (entry == null) continue;
                weight = entryWeight.getFloat(entry);
                if (!((boolean) conditionsMatch.invoke(match, checker))) weight = 0;
                if (weight == 0) continue;
                if (!dbe.flys() && random >= weight)
                {
                    if (!(dbe.swims() && v.getBlockMaterial(world) == Material.WATER))
                    {
                        v = Vector3.getNextSurfacePoint2(world, vbak, Vector3.secondAxisNeg, 20);
                        if (v != null)
                        {
                            v.offsetBy(EnumFacing.UP);
                            weight = dbe.getSpawnData().getWeight(dbe.getSpawnData().getMatcher(world, v));
                        }
                        else weight = 0;
                    }
                }
                if (v == null)
                {
                    v = vbak.copy();
                }
            }
            if (random > weight || v == null) return;
            if (dbe.legendary)
            {
                int level = SpawnHandler.getSpawnLevel(world, v, dbe);
                if (level < PokecubeMod.core.getConfig().minLegendLevel) { return; }
            }
            event.setLocation(v);
            event.setPick(dbe);
        }
        else
        {
            if (event.getPicked() == null) return;
            try
            {
                IAtmosphere atmos = (IAtmosphere) getAtmosphereType.invoke(
                        getOxygenHandler.invoke(null, event.world.provider.getDimension()), event.location.getPos());
                if (!atmos.isBreathable() && !vacuumBreathers.contains(event.getPicked()))
                {
                    event.setPick(null);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @SubscribeEvent
    public void breathe(AtmosphereTickEvent event)
    {
        if (event.getEntity() instanceof IPokemob)
        {
            if (vacuumBreathers.contains((((IPokemob) event.getEntity()).getPokedexEntry()))) event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void breathe(LivingAttackEvent event)
    {
        if (event.getEntity() instanceof IPokemob && event.getSource() == vacuumDamage)
        {
            if (vacuumBreathers.contains((((IPokemob) event.getEntity()).getPokedexEntry()))) event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void toOrbit(LivingUpdateEvent event)
    {
        if (event.getEntity().world.isRemote) return;
        if (event.getEntity() instanceof IPokemob)
        {
            PokedexEntry entry = (((IPokemob) event.getEntity()).getPokedexEntry());
            if (entry == megaray && event.getEntityLiving().isBeingRidden())
            {
                boolean goUp = event.getEntity().posY > Configuration.orbit;
                boolean goDown = event.getEntity().posY < 0;
                if (!(goUp || goDown)) return;
                Vector3 pos = Vector3.getNewVector().set(event.getEntity());
                pos.y = Configuration.orbit - 100;
                boolean aroundStation = false;
                ISpaceObject station = AdvancedRocketryAPI.spaceObjectManager
                        .getSpaceStationFromBlockCoords(event.getEntity().getPosition());
                aroundStation = station != null;
                int dim = goUp ? Configuration.spaceDimId : 0;
                if (aroundStation)
                {
                    if (goDown)
                    {
                        dim = station.getOrbitingPlanetId();
                        if (dim == -1) dim = 0;
                    }
                }
                else
                {
                    DimensionProperties props = DimensionManager.getInstance()
                            .getDimensionProperties(event.getEntity().world.provider.getDimension());
                    boolean moon = props.isMoon();
                    if (moon && goUp)
                    {
                        dim = props.getParentPlanet();
                    }
                    else if (!props.isStation() && !props.getChildPlanets().isEmpty())
                    {
                        List<Integer> moons = Lists.newArrayList(props.getChildPlanets());
                        Collections.sort(moons);
                        if (!moons.isEmpty())
                        {
                            double angle = ((event.getEntity().world.getWorldTime() % props.rotationalPeriod)
                                    / (double) props.rotationalPeriod) * 2 * Math.PI;
                            double diff = 2 * Math.PI;
                            int whichMoon = 0;
                            for (int i = 0; i < moons.size(); i++)
                            {
                                DimensionProperties moonProps = DimensionManager.getInstance()
                                        .getDimensionProperties(moons.get(i));
                                double moonTheta = moonProps.orbitTheta % (2 * Math.PI);
                                if (diff > Math.abs(moonTheta - angle))
                                {
                                    diff = Math.abs(moonTheta - angle);
                                    whichMoon = i;
                                }
                            }
                            dim = moons.get(whichMoon);
                        }
                    }
                }
                if (!DimensionManager.getInstance().canTravelTo(dim)) return;
                TransferDimension event2 = new TransferDimension(event.getEntity(), pos, dim);
                MinecraftForge.EVENT_BUS.post(event2);
                if (!event2.isCanceled()) Transporter.teleportEntity(event.getEntity(), event2.getDesination(),
                        event2.getDestinationDim(), false);
            }
        }
    }
}
