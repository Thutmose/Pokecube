package pokecube.compat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.ForgeVersion.CheckResult;
import net.minecraftforge.common.ForgeVersion.Status;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.adventures.PokecubeAdv;
import pokecube.compat.forgeenergy.EnergyHandler;
import pokecube.core.database.Database;
import pokecube.core.events.PostPostInit;
import pokecube.core.interfaces.PokecubeMod;
import thut.core.client.ClientProxy;
import thut.lib.CompatClass;
import thut.lib.CompatClass.Phase;
import thut.lib.CompatParser;

@Mod(modid = "pokecube_compat", name = "Pokecube Compat", version = "1.0", acceptedMinecraftVersions = "*")
public class Compat
{
    public static class UpdateNotifier
    {
        static boolean done = false;

        public UpdateNotifier()
        {
            if (!done) MinecraftForge.EVENT_BUS.register(this);
            done = true;
        }

        @SubscribeEvent
        public void onPlayerJoin(TickEvent.PlayerTickEvent event)
        {
            if (event.player.getEntityWorld().isRemote
                    && event.player == FMLClientHandler.instance().getClientPlayerEntity())
            {
                MinecraftForge.EVENT_BUS.unregister(this);
                Object o = Loader.instance().getIndexedModList().get(PokecubeAdv.ID);
                CheckResult result = ForgeVersion.getResult(((ModContainer) o));
                if (result.status == Status.OUTDATED)
                {
                    ITextComponent mess = ClientProxy.getOutdatedMessage(result, "Pokecube Revival");
                    (event.player).sendMessage(mess);
                }
                if (PokecubeAdv.tesla)
                {
                    ITextComponent mess = new TextComponentTranslation("pokecube.power.tesla");
                    (event.player).sendMessage(mess);
                }
            }
        }
    }

    public static String       CUSTOMSPAWNSFILE;
    public static String       CUSTOMDROPSFILE;
    public static String       CUSTOMHELDFILE;

    @Instance("pokecube_compat")
    public static Compat       instance;

    private static PrintWriter out;

    private static FileWriter  fwriter;

    public static void setSpawnsFile(FMLPreInitializationEvent evt)
    {
        File file = evt.getSuggestedConfigurationFile();
        String seperator = System.getProperty("file.separator");
        String folder = file.getAbsolutePath();
        String name = file.getName();
        folder = folder.replace(name, "pokecube" + seperator + "compat" + seperator + "spawns.xml");
        CUSTOMSPAWNSFILE = folder;
        CUSTOMDROPSFILE = folder.replace("spawns.xml", "drops.xml");
        CUSTOMHELDFILE = folder.replace("spawns.xml", "held.xml");
        writeDefaultSpawnsConfig();
        writeDefaultDropsConfig();
        writeDefaultHeldConfig();
        return;
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
            File temp1 = new File(CUSTOMSPAWNSFILE);
            if (temp1.exists()) { return; }

            String bulba = "    <Spawn name=\"Bulbasaur\" starter=\"true\" overwrite=\"true\" "
                    + "rate=\"0.01\" min=\"1\" max=\"2\" types=\"forest\"/>";
            String squirt = "    <Spawn name=\"Squirtle\" starter=\"true\" overwrite=\"true\" "
                    + "rate=\"0.01\" min=\"1\" max=\"2\" types=\"lake\" water=\"true\"/>";
            String cater1 = "    <Spawn name=\"Caterpie\" overwrite=\"true\" "
                    + "rate=\"0.2\" min=\"4\" max=\"8\" types=\"forest\" typesBlacklist=\"sparse,cold,dry\"/>//Overwrite to clear original";
            String cater2 = "    <Spawn name=\"Caterpie\"  rate=\"0.2\" min=\"4\" max=\"8\" types=\"wet\""
                    + " typesBlacklist=\"sparse,cold,dry\"/>//Don't overwrite to add";

            fwriter = new FileWriter(CUSTOMSPAWNSFILE);
            out = new PrintWriter(fwriter);
            out.println("<?xml version=\"1.0\"?>");
            out.println("<Spawns>");
            out.println(bulba);
            out.println(squirt);
            out.println(cater1);
            out.println(cater2);
            out.println("</Spawns>");

            out.close();
            fwriter.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void writeDefaultDropsConfig()
    {
        try
        {
            File temp = new File(CUSTOMDROPSFILE.replace("drops.xml", ""));
            if (!temp.exists())
            {
                temp.mkdirs();
            }
            File temp1 = new File(CUSTOMDROPSFILE);
            if (temp1.exists()) { return; }

            String bulba = "    <Drop name=\"Bulbasaur\" overwrite=\"true\" "
                    + "n=\"1\" r=\"1\" id=\"minecraft:wheat_seeds\"/>";

            fwriter = new FileWriter(CUSTOMDROPSFILE);
            out = new PrintWriter(fwriter);
            out.println("<?xml version=\"1.0\"?>");
            out.println("<Drops>");
            out.println(bulba);
            out.println("</Drops>");

            out.close();
            fwriter.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void writeDefaultHeldConfig()
    {
        try
        {
            File temp = new File(CUSTOMHELDFILE.replace("held.xml", ""));
            if (!temp.exists())
            {
                temp.mkdirs();
            }
            File temp1 = new File(CUSTOMHELDFILE);
            if (temp1.exists()) { return; }

            String nidor = "    <Held name=\"NidoranF\" overwrite=\"true\" n=\"1\" r=\"1.0\" id=\"moonstone\"/>";

            fwriter = new FileWriter(CUSTOMHELDFILE);
            out = new PrintWriter(fwriter);
            out.println("<?xml version=\"1.0\"?>");
            out.println("<Helds>");
            out.println(nidor);
            out.println("</Helds>");

            out.close();
            fwriter.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    Map<CompatClass.Phase, Set<java.lang.reflect.Method>> initMethods = Maps.newHashMap();

    public Compat()
    {
        for (Phase phase : Phase.values())
        {
            initMethods.put(phase, new HashSet<java.lang.reflect.Method>());
        }
        CompatParser.findClasses(getClass().getPackage().getName(), initMethods);
        doPhase(Phase.CONSTRUCT, null);
    }

    private void doMetastuff()
    {
        ModMetadata meta = FMLCommonHandler.instance().findContainerFor(this).getMetadata();
        meta.parent = PokecubeMod.ID;
    }

    @SideOnly(Side.CLIENT)
    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        new UpdateNotifier();
    }

    @EventHandler
    public void init(FMLInitializationEvent evt)
    {
        doPhase(Phase.INIT, evt);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent evt)
    {
        if (!PokecubeAdv.tesla)
        {
            MinecraftForge.EVENT_BUS.register(new EnergyHandler());
        }
        doPhase(Phase.POST, evt);
    }

    @SubscribeEvent
    public void postPostInit(PostPostInit evt)
    {
        doPhase(Phase.POSTPOST, evt);
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        doMetastuff();
        MinecraftForge.EVENT_BUS.register(this);
        setSpawnsFile(evt);
        Database.addSpawnData(CUSTOMSPAWNSFILE);
        Database.addDropData(CUSTOMSPAWNSFILE.replace("spawns.xml", "drops.xml"));
        Database.addHeldData(CUSTOMSPAWNSFILE.replace("spawns.xml", "held.xml"));
        doPhase(Phase.PRE, evt);
    }

    private void doPhase(Phase pre, Object event)
    {
        for (java.lang.reflect.Method m : initMethods.get(pre))
        {
            try
            {
                CompatClass comp = m.getAnnotation(CompatClass.class);
                if (comp.takesEvent()) m.invoke(null, event);
                else m.invoke(null);
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
            {
                e.printStackTrace();
            }
        }
    }
}
