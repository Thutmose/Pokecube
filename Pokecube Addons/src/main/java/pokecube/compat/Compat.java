package pokecube.compat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.ForgeVersion.CheckResult;
import net.minecraftforge.common.ForgeVersion.Status;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.adventures.PokecubeAdv;
import pokecube.compat.ai.AITeslaInterferance;
import pokecube.compat.tesla.TeslaHandler;
import pokecube.core.database.Database;
import pokecube.core.events.PostPostInit;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import thut.core.client.ClientProxy;

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
                    (event.player).addChatMessage(mess);
                }
                if (!PokecubeAdv.hasEnergyAPI)
                {
                    ITextComponent mess = new TextComponentTranslation("pokecube.power.disabled");
                    (event.player).addChatMessage(mess);
                }
                if (PokecubeAdv.tesla)
                {
                    ITextComponent mess = new TextComponentTranslation("pokecube.power.tesla");
                    (event.player).addChatMessage(mess);
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

    public Compat()
    {
    }

    @Optional.Method(modid = "DynamicLights")
    @EventHandler
    public void AS_DLCompat(FMLPostInitializationEvent evt)
    {
        System.out.println("DynamicLights Compat");
        // MinecraftForge.EVENT_BUS.register(new
        // pokecube.compat.atomicstryker.DynamicLightsCompat());
    }

    @Optional.Method(modid = "AS_Ruins")
    @EventHandler
    public void AS_RuinsCompat(FMLPostInitializationEvent evt)
    {
        System.out.println("AS_Ruins Compat");
        MinecraftForge.EVENT_BUS.register(new pokecube.compat.atomicstryker.RuinsCompat());
    }

    @Optional.Method(modid = "soulshardstow")
    @EventHandler
    public void SoulShardsCompat(FMLPostInitializationEvent evt)
    {
        System.out.println("SoulShards Compat");
        new pokecube.compat.soulshards.SoulShardsCompat();
    }

    private void doMetastuff()
    {
        ModMetadata meta = FMLCommonHandler.instance().findContainerFor(this).getMetadata();
        meta.parent = PokecubeMod.ID;
    }

    @Optional.Method(modid = "tesla")
    @SubscribeEvent
    public void addTeslaInterferance(EntityJoinWorldEvent evt)
    {
        if (evt.getEntity() instanceof IPokemob && evt.getEntity() instanceof EntityLiving)
        {
            EntityLiving living = (EntityLiving) evt.getEntity();
            living.tasks.addTask(1, new AITeslaInterferance((IPokemob) living));
        }
    }

    @Optional.Method(modid = "thutessentials")
    @EventHandler
    public void thutEssentialsCompat(FMLPreInitializationEvent e)
    {
        new pokecube.compat.tecompat.EssentialsCompat();
    }

    @Optional.Method(modid = "tesla")
    @EventHandler
    public void TeslaCompat(FMLPreInitializationEvent evt)
    {
        new TeslaHandler();
    }
//
//    @SideOnly(Side.CLIENT)
//    @Optional.Method(modid = "jeresources")
//    @SubscribeEvent
//    public void JERInit(PostPostInit evt)
//    {
//        new pokecube.compat.jer.JERCompat().register();
//    }

    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) new UpdateNotifier();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent evt)
    {
    }

    @SubscribeEvent
    public void postPostInit(PostPostInit evt)
    {
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
    }

    @Optional.Method(modid = "theoneprobe")
    @EventHandler
    public void TheOneProbe_Compat(FMLPostInitializationEvent evt)
    {
        System.out.println("TheOneProbe Compat");
        new pokecube.compat.top.TheOneProbeCompat();
    }

    @SideOnly(Side.CLIENT)
    @Optional.Method(modid = "Waila")
    @EventHandler
    public void WAILA_Compat(FMLPostInitializationEvent evt)
    {
        System.out.println("Waila Compat");
        MinecraftForge.EVENT_BUS.register(new pokecube.compat.waila.WailaCompat());
    }

    @Method(modid = "thut_wearables")
    @EventHandler
    public void preInitWearables(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new pokecube.compat.wearables.WearableCompat());
    }

    @Method(modid = "thut_bling")
    @EventHandler
    public void postInitBling(FMLPostInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new pokecube.compat.bling.BlingCompat());
    }
}
