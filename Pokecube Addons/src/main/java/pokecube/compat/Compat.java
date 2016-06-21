package pokecube.compat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.text.ITextComponent;
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
import pokecube.compat.ai.AIElectricalInterferance;
import pokecube.core.database.Database;
import pokecube.core.events.PostPostInit;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import thut.core.client.ClientProxy;

@Mod(modid = "pokecube_compat", name = "Pokecube Compat", version = "1.0", acceptedMinecraftVersions = PokecubeAdv.MCVERSIONS)
public class Compat
{
    public static class WikiInfoNotifier
    {
        public WikiInfoNotifier()
        {
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void onPlayerJoin(TickEvent.PlayerTickEvent event)
        {
            if (event.player.worldObj.isRemote && event.player == FMLClientHandler.instance().getClientPlayerEntity())
            {
                MinecraftForge.EVENT_BUS.unregister(this);
                Object o = Loader.instance().getIndexedModList().get(PokecubeAdv.ID);
                CheckResult result = ForgeVersion.getResult(((ModContainer) o));
                if (result.status == Status.OUTDATED)
                {
                    ITextComponent mess = ClientProxy.getOutdatedMessage(result, "Pokecube Revival");
                    (event.player).addChatMessage(mess);
                }
            }
        }
    }

    public static String       CUSTOMSPAWNSFILE;

    @Instance("pokecube_compat")
    public static Compat       instance;

    private static PrintWriter out;

    private static FileWriter  fwriter;

    static String              header   = "Name,Special Cases,Biomes - any acceptable,Biomes - all needed,Excluded biomes,Replace";

    static String              example1 = "Rattata,day night ,mound 0.6:10:5,,,false";

    static String              example2 = "Spearow,day night ,plains 0.3;hills 0.3,,,true";

    public static void setSpawnsFile(FMLPreInitializationEvent evt)
    {
        File file = evt.getSuggestedConfigurationFile();
        String seperator = System.getProperty("file.separator");

        String folder = file.getAbsolutePath();
        String name = file.getName();
        folder = folder.replace(name, "pokecube" + seperator + "compat" + seperator + "spawns.csv");

        CUSTOMSPAWNSFILE = folder;
        writeDefaultConfig();
        return;
    }

    private static void writeDefaultConfig()
    {
        try
        {
            File temp = new File(CUSTOMSPAWNSFILE.replace("spawns.csv", ""));
            if (!temp.exists())
            {
                temp.mkdirs();
            }
            File temp1 = new File(CUSTOMSPAWNSFILE);
            if (temp1.exists()) { return; }

            fwriter = new FileWriter(CUSTOMSPAWNSFILE);
            out = new PrintWriter(fwriter);
            out.println(header);
            out.println(example1);
            out.println(example2);

            out.close();
            fwriter.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

//    GCCompat gccompat;

    public Compat()
    {
//        gccompat = new GCCompat();
//        MinecraftForge.EVENT_BUS.register(gccompat);
    }

    // @Optional.Method(modid = "PneumaticCraft")
    // @SubscribeEvent
    // public void addPneumaticcraftHeating(EntityJoinWorldEvent evt)
    // {
    // if (evt.getEntity() instanceof IPokemob && evt.getEntity() instanceof
    // EntityLiving)
    // {
    // EntityLiving living = (EntityLiving) evt.getEntity();
    // living.tasks.addTask(1, new AIThermalInteferance((IPokemob) living));
    // }
    // }

    @Optional.Method(modid = "DynamicLights")
    @EventHandler
    public void AS_DLCompat(FMLPostInitializationEvent evt)
    {
        System.out.println("DynamicLights Compat");
//        MinecraftForge.EVENT_BUS.register(new pokecube.compat.atomicstryker.DynamicLightsCompat());
    }

    @Optional.Method(modid = "AS_Ruins")
    @EventHandler
    public void AS_RuinsCompat(FMLPostInitializationEvent evt)
    {
        System.out.println("AS_Ruins Compat");
//        MinecraftForge.EVENT_BUS.register(new pokecube.compat.atomicstryker.RuinsCompat());
    }

    @SideOnly(Side.CLIENT)
    @Optional.Method(modid = "Baubles")
    @EventHandler
    public void BaublesCompat(FMLPostInitializationEvent evt)
    {
        MinecraftForge.EVENT_BUS.register(new pokecube.compat.baubles.BaublesEventHandler());
    }

    private void doMetastuff()
    {
        ModMetadata meta = FMLCommonHandler.instance().findContainerFor(this).getMetadata();

        meta.parent = PokecubeMod.ID;
    }

    @SubscribeEvent
    public void entityConstruct(EntityJoinWorldEvent evt)
    {
        if (evt.getEntity() instanceof IPokemob && evt.getEntity() instanceof EntityLiving)
        {
            EntityLiving living = (EntityLiving) evt.getEntity();
            living.tasks.addTask(1, new AIElectricalInterferance((IPokemob) living));
        }
    }

    @SideOnly(Side.CLIENT)
    @Optional.Method(modid = "jeresources")
    @SubscribeEvent
    public void JERInit(PostPostInit evt)
    {
        new pokecube.compat.jer.JERCompat().register();
    }

    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) new WikiInfoNotifier();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent evt)
    {
    }

    @SubscribeEvent
    public void postPostInit(PostPostInit evt)
    {
//        gccompat.register();
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        doMetastuff();
        MinecraftForge.EVENT_BUS.register(this);
        setSpawnsFile(evt);
        Database.addSpawnData(CUSTOMSPAWNSFILE);
    }

    @Optional.Method(modid = "reccomplex")
    @EventHandler
    public void RecComplex_Compat(FMLPostInitializationEvent evt)
    {
        System.out.println("Recurrent Complex Compat");
        pokecube.compat.reccomplex.ReComplexCompat.register();
    }

//    @Optional.Method(modid = "Thaumcraft")
//    @EventHandler
//    public void Thaumcraft_Compat(FMLPreInitializationEvent evt)
//    {
//        System.out.println("Thaumcraft Compat");
//
//        ThaumcraftCompat tccompat;
//        ThaumiumPokecube thaumiumpokecube;
//        thaumiumpokecube = new ThaumiumPokecube();
//        thaumiumpokecube.addThaumiumPokecube();
//
//        tccompat = new ThaumcraftCompat();
//        MinecraftForge.EVENT_BUS.register(tccompat);
//    }

    @SideOnly(Side.CLIENT)
    @Optional.Method(modid = "Waila")
    @EventHandler
    public void WAILA_Compat(FMLPostInitializationEvent evt)
    {
        System.out.println("Waila Compat");
        MinecraftForge.EVENT_BUS.register(new pokecube.compat.waila.WailaCompat());
    }
}
