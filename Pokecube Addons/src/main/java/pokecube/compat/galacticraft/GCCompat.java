package pokecube.compat.galacticraft;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IRETURN;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.block.material.Material;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.SpawnData.SpawnEntry;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.events.ClassGenEvent;
import pokecube.core.events.PostPostInit;
import pokecube.core.events.SpawnEvent;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;
import thut.lib.CompatClass;
import thut.lib.CompatClass.Phase;

public class GCCompat
{

    public static String       CUSTOMSPAWNSFILE;

    private static PrintWriter out;

    private static FileWriter  fwriter;
    private Method             conditionsMatch;
    private Field              entryWeight;
    Set<PokedexEntry>          vacuumBreathers = Sets.newHashSet();
    List<PokedexEntry>         moonmon         = Lists.newArrayList();
    PokedexEntry               megaray;

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

    @SubscribeEvent
    public void postpost(PostPostInit event)
    {
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

    private void doMoonSpawns(SpawnEvent.Pick.Pre event)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
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

    @SubscribeEvent
    public void spawn(SpawnEvent.Pick.Pre event) throws Exception
    {
        Biome biome = event.location.getBiome(event.world);
        Biome moon = Biome.REGISTRY.getObject(new ResourceLocation("Moon"));
        if (biome == moon)
        {
            doMoonSpawns(event);
        }
        else
        {
            // TODO prevent things spawning on planets that shouldn't be there.
        }
    }

    boolean init = false;

    @Optional.Method(modid = "galacticraft")
    @CompatClass(takesEvent = true, phase = Phase.PRE)
    public static void Compat(FMLPreInitializationEvent evt)
    {
        MinecraftForge.EVENT_BUS.register(new GCCompat());
        setSpawnsFile(evt);
        Database.addSpawnData(CUSTOMSPAWNSFILE);
        BiomeType.getBiome("Moon", true);
    }

    @SubscribeEvent
    public void makeVacuumSafe(ClassGenEvent evt)
    {
        if (!init)
        {
            Set<String> mobs = Sets.newHashSet();// TODO make this load from a
                                                 // file.
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
            init = true;
        }

        if (vacuumBreathers.contains(evt.pokedexEntry))
        {
            try
            {
                Class<?> inter = Class.forName("micdoodle8.mods.galacticraft.api.entity.IEntityBreathable");

                ClassWriter cw = evt.writer;

                Field interfaces = ClassWriter.class.getDeclaredField("interfaces");
                Field count = ClassWriter.class.getDeclaredField("interfaceCount");
                interfaces.setAccessible(true);
                count.setAccessible(true);

                int[] ints = (int[]) interfaces.get(cw);
                int n = count.getInt(cw);

                int[] newints = new int[ints != null ? ints.length + 1 : 1];
                n = newints.length;
                count.set(cw, n);
                if (ints != null) for (int i = 0; i < ints.length; i++)
                {
                    newints[i] = ints[i];
                }
                newints[n - 1] = cw.newClass(Type.getInternalName(inter));
                interfaces.set(cw, newints);

                MethodVisitor mv;
                {
                    mv = cw.visitMethod(ACC_PUBLIC, "canBreath", "()Z", null, null);
                    mv.visitCode();
                    Label l0 = new Label();
                    mv.visitLabel(l0);
                    mv.visitInsn(ICONST_1);
                    mv.visitInsn(IRETURN);
                    Label l1 = new Label();
                    mv.visitLabel(l1);
                    mv.visitMaxs(1, 1);
                    mv.visitEnd();
                }

            }
            catch (Throwable t)
            {

            }
        }
    }
}