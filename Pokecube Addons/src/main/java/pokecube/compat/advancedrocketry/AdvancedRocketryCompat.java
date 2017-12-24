package pokecube.compat.advancedrocketry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
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
import pokecube.adventures.events.TrainerSpawnEvent;
import pokecube.compat.events.TransferDimension;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.SpawnData.SpawnEntry;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.events.PostPostInit;
import pokecube.core.events.SpawnEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.api.entity.Transporter;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;
import thut.lib.CompatClass;
import thut.lib.CompatClass.Phase;
import thut.lib.CompatWrapper;
import zmaster587.advancedRocketry.api.Configuration;
import zmaster587.advancedRocketry.api.IAtmosphere;
import zmaster587.advancedRocketry.api.event.AtmosphereEvent.AtmosphereTickEvent;
import zmaster587.advancedRocketry.api.stations.ISpaceObject;
import zmaster587.advancedRocketry.dimension.DimensionManager;
import zmaster587.advancedRocketry.dimension.DimensionProperties;
import zmaster587.advancedRocketry.item.ItemPlanetIdentificationChip;
import zmaster587.advancedRocketry.item.ItemStationChip;
import zmaster587.advancedRocketry.stations.SpaceObjectManager;
import zmaster587.libVulpes.util.Vector3F;

public class AdvancedRocketryCompat
{
    private static class VacuumBreathers
    {
        Set<String> pokemobs = Sets.newHashSet();
    }

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
            List<String> list = Lists.newArrayList();
            list.add("    <Spawn name=\"Lunatone\" overwrite=\"false\" "
                    + "rate=\"0.01\" min=\"1\" max=\"2\" types=\"moon\"/>");
            list.add("    <Spawn name=\"Solrock\" overwrite=\"false\" "
                    + "rate=\"0.01\" min=\"1\" max=\"2\" types=\"moon\"/>");
            list.add("    <Spawn name=\"Beldum\" overwrite=\"false\" "
                    + "rate=\"0.01\" min=\"1\" max=\"2\" types=\"moon\"/>");
            list.add("    <Spawn name=\"Minior\" overwrite=\"false\" "
                    + "rate=\"0.01\" min=\"1\" max=\"2\" types=\"moon\"/>");
            list.add("    <Spawn name=\"Clefairy\" overwrite=\"false\" "
                    + "rate=\"0.2\" min=\"4\" max=\"6\" types=\"moon\"/>");
            list.add("    <Spawn name=\"Deoxys\" overwrite=\"false\" "
                    + "rate=\"0.0001\" min=\"1\" max=\"2\" types=\"space\"/>");
            list.add("    <Spawn name=\"Rayquaza\" overwrite=\"false\" "
                    + "rate=\"0.0001\" min=\"1\" max=\"1\" types=\"space\"/>");
            fwriter = new FileWriter(CUSTOMSPAWNSFILE);
            out = new PrintWriter(fwriter);
            out.println("<?xml version=\"1.0\"?>");
            out.println("<Spawns>");
            for (String s : list)
                out.println(s);
            out.println("</Spawns>");
            out.close();
            fwriter.close();

            list.clear();
            String fileName = CUSTOMSPAWNSFILE.replace("spawns.xml", "vacuumsafe.cfg");
            temp = new File(fileName);
            if (!temp.exists())
            {
                list.add("default_vacuumsafety.json");
                fwriter = new FileWriter(fileName);
                out = new PrintWriter(fwriter);
                for (String s : list)
                    out.println(s);
                out.close();
                fwriter.close();
            }
            list.clear();
            list.add("clefairy");
            list.add("clefable");
            list.add("lunatone");
            list.add("solrock");
            list.add("deoxys");
            list.add("beldum");
            list.add("metang");
            list.add("metagross");
            list.add("metagrossmega");
            list.add("rayquaza");
            list.add("minior");
            list.add("rayquazamega");
            fileName = CUSTOMSPAWNSFILE.replace("spawns.xml", "default_vacuumsafety.json");
            temp = new File(fileName);
            fwriter = new FileWriter(fileName);
            out = new PrintWriter(fwriter);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            VacuumBreathers defaults = new VacuumBreathers();
            defaults.pokemobs.addAll(list);
            out.println(gson.toJson(defaults));
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

    @Optional.Method(modid = "advancedrocketry")
    @CompatClass(takesEvent = true, phase = Phase.PRE)
    public static void ARCompat(FMLPreInitializationEvent evt)
    {
        MinecraftForge.EVENT_BUS.register(new pokecube.compat.advancedrocketry.AdvancedRocketryCompat(evt));
        PokecubeMod.log("Registered Advanced Rocketry Compat");
    }

    public AdvancedRocketryCompat(FMLPreInitializationEvent event)
    {
        setSpawnsFile(event);
        Database.addSpawnData(CUSTOMSPAWNSFILE);
    }

    private boolean canBreatheHere(PokedexEntry entry, World world, BlockPos pos)
    {
        try
        {
            IAtmosphere atmos;
            atmos = (IAtmosphere) getAtmosphereType.invoke(getOxygenHandler.invoke(null, world.provider.getDimension()),
                    pos);
            if (!atmos.isBreathable() && !vacuumBreathers.contains(entry)) { return false; }
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            PokecubeMod.log(Level.SEVERE, "Error checking whether " + entry + " can breathe " + world + " " + pos, e);
        }
        return true;
    }

    @SubscribeEvent
    public void postpost(PostPostInit event)
    {
        BiomeType.getBiome("moon", true);
        BiomeType.getBiome("space", true);
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
        megaray = Database.getEntry("rayquazamega");

        Set<String> mobs = Sets.newHashSet();

        try
        {
            String fileName = CUSTOMSPAWNSFILE.replace("spawns.xml", "vacuumsafe.cfg");
            File temp = new File(fileName);
            FileReader fileReader = new FileReader(temp);
            BufferedReader reader = new BufferedReader(fileReader);
            String line;
            Gson gson = new GsonBuilder().create();
            while ((line = reader.readLine()) != null)
            {
                fileName = CUSTOMSPAWNSFILE.replace("spawns.xml", line);
                temp = new File(fileName);
                if (temp.exists())
                {
                    FileReader jsonreader = new FileReader(temp);
                    VacuumBreathers breathers = gson.fromJson(jsonreader, VacuumBreathers.class);
                    if (breathers != null)
                    {
                        mobs.addAll(breathers.pokemobs);
                    }
                }
                else
                {
                    PokecubeMod.log(Level.SEVERE, "No File found of name " + temp);
                }
            }
            reader.close();
        }
        catch (Exception e)
        {
            PokecubeMod.log(Level.SEVERE, "Error reading configs for vacuum breathers", e);
        }

        for (String s : mobs)
        {
            if (Database.getEntry(s) != null) vacuumBreathers.add(Database.getEntry(s));
        }
    }

    @SubscribeEvent
    public void trainerSpawnEvent(TrainerSpawnEvent evt)
    {
        try
        {
            World world = evt.getWorld();
            BlockPos pos = evt.getLocation();
            IAtmosphere atmos;
            atmos = (IAtmosphere) getAtmosphereType.invoke(getOxygenHandler.invoke(null, world.provider.getDimension()),
                    pos);
            if (!atmos.isBreathable())
            {
                evt.setCanceled(true);
            }
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            PokecubeMod.log(Level.SEVERE, "Error checking whether " + evt.getTrainer() + " can breathe ", e);
        }
    }

    @SubscribeEvent
    public void spawn(SpawnEvent.Check event) throws Exception
    {
        Biome biome = event.location.getBiome(event.world);
        Biome moon = Biome.REGISTRY.getObject(new ResourceLocation("advancedrocketry:Moon"));
        Vector3 v = event.location;
        BlockPos pos = v.getPos();
        World world = event.world;
        if (moon == null)
        {
            moon = Biome.REGISTRY.getObject(new ResourceLocation("advancedrocketry:moon"));
        }
        if (biome == moon)
        {
            BiomeType moonType = BiomeType.getBiome("moon", true);
            PokedexEntry dbe = event.entry;
            if (dbe.getSpawnData().isValid(moonType))
            {
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
                else if (!canBreatheHere(dbe, world, pos)) event.setCanceled(true);
            }
            else if (!canBreatheHere(dbe, world, pos)) event.setCanceled(true);
            return;
        }
        Biome space = Biome.REGISTRY.getObject(new ResourceLocation("advancedrocketry:Space"));
        if (space == null)
        {
            space = Biome.REGISTRY.getObject(new ResourceLocation("advancedrocketry:space"));
        }
        if (biome == space)
        {
            BiomeType spaceType = BiomeType.getBiome("space", true);
            PokedexEntry dbe = event.entry;
            if (dbe.getSpawnData().isValid(spaceType))
            {
                SpawnCheck checker = new SpawnCheck(v, world);
                SpawnBiomeMatcher match = null;
                for (SpawnBiomeMatcher matcher : dbe.getSpawnData().matchers.keySet())
                {
                    if (matcher.validSubBiomes.contains(spaceType))
                    {
                        match = matcher;
                        break;
                    }
                }
                if (((boolean) conditionsMatch.invoke(match, checker))) event.setResult(Result.ALLOW);
                else if (!canBreatheHere(dbe, world, pos)) event.setCanceled(true);
            }
            else if (!canBreatheHere(dbe, world, pos)) event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void spawn(SpawnEvent.Pick.Pre event) throws Exception
    {
        if (event.getPicked() == null) return;
        if (!canBreatheHere(event.getPicked(), event.world, event.location.getPos())) event.setPick(null);
    }

    @SubscribeEvent
    public void breathe(AtmosphereTickEvent event)
    {
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(event.getEntity());
        if (pokemob != null)
        {
            if (vacuumBreathers.contains((pokemob.getPokedexEntry()))) event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void breathe(LivingAttackEvent event)
    {
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(event.getEntity());
        if (pokemob != null && event.getSource() == vacuumDamage)
        {
            if (vacuumBreathers.contains((pokemob.getPokedexEntry()))) event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void toOrbit(LivingUpdateEvent event)
    {
        if (event.getEntity().getEntityWorld().isRemote) return;
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(event.getEntity());
        if (pokemob != null)
        {
            PokedexEntry entry = pokemob.getPokedexEntry();
            if (entry == megaray && event.getEntityLiving().isBeingRidden())
            {
                boolean goUp = event.getEntity().posY > Configuration.orbit / 2
                        && event.getEntity().dimension != Configuration.spaceDimId;
                boolean goDown = event.getEntity().posY < 2 || (event.getEntity().posY > Configuration.orbit / 2
                        && event.getEntity().dimension == Configuration.spaceDimId);
                if (!(goUp || goDown)) return;
                Vector3 pos = Vector3.getNewVector().set(event.getEntity());
                int targetDim = -1;
                ItemStack stack = pokemob.getPokemobInventory().getStackInSlot(2);
                if (CompatWrapper.isValid(stack))
                {
                    Item itemType = stack.getItem();
                    if (itemType instanceof ItemPlanetIdentificationChip)
                    {
                        ItemPlanetIdentificationChip item = (ItemPlanetIdentificationChip) itemType;
                        targetDim = item.getDimensionId(stack);
                        if (!DimensionManager.getInstance().canTravelTo(targetDim)) targetDim = -1;
                        if (targetDim != -1)
                        {
                            DimensionProperties dest = DimensionManager.getInstance().getDimensionProperties(targetDim);
                            DimensionProperties source = DimensionManager.getInstance()
                                    .getDimensionProperties(event.getEntity().dimension);
                            int destParent = dest.isMoon() ? dest.getParentPlanet() : targetDim;
                            int sourceParent = source.isMoon() ? source.getParentPlanet() : event.getEntity().dimension;
                            if (destParent != sourceParent) targetDim = -1;
                        }
                    }
                    else if (itemType instanceof ItemStationChip)
                    {
                        if (Configuration.spaceDimId == event.getEntity().dimension)
                        {
                            ISpaceObject object = SpaceObjectManager.getSpaceManager()
                                    .getSpaceStationFromBlockCoords(event.getEntity().getPosition());
                            if (object != null)
                            {
                                targetDim = object.getOrbitingPlanetId();
                            }
                            else targetDim = -1;
                        }
                        else
                        {
                            targetDim = Configuration.spaceDimId;
                            ISpaceObject object = SpaceObjectManager.getSpaceManager()
                                    .getSpaceStation(ItemStationChip.getUUID(stack));
                            if (object != null)
                            {
                                pos.x = object.getSpawnLocation().x;
                                pos.z = object.getSpawnLocation().z;
                                int dimId = event.getEntity().world.provider.getDimension();
                                DimensionProperties props = DimensionManager.getInstance()
                                        .getDimensionProperties(dimId);
                                int stationParent = object.getOrbitingPlanetId();
                                int currentParent = props.isMoon() ? props.getParentPlanet()
                                        : props.isStation() ? props.getParentPlanet() : dimId;
                                if (currentParent != stationParent) targetDim = -1;
                            }
                            else
                            {
                                Vector3F<Float> vec = ((ItemStationChip) itemType).getTakeoffCoords(stack, targetDim);
                                if (vec != null)
                                {
                                    pos.x = vec.x;
                                    pos.z = vec.z;
                                }
                            }
                        }
                    }
                }
                pos.y = Configuration.orbit / 2 - 100;
                int dim = targetDim;
                if (targetDim == -1)
                {
                    DimensionProperties props = DimensionManager.getInstance()
                            .getDimensionProperties(dim = event.getEntity().world.provider.getDimension());
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
                    else if (props.isStation())
                    {
                        dim = props.getParentPlanet();
                        ISpaceObject object = SpaceObjectManager.getSpaceManager()
                                .getSpaceStationFromBlockCoords(event.getEntity().getPosition());
                        if (object != null)
                        {
                            dim = object.getOrbitingPlanetId();
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
