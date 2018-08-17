package pokecube.core.world.dimensions.secretpower;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.nfunk.jep.JEP;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.core.PokecubeCore;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.database.stats.EggStats;
import pokecube.core.database.stats.KillStats;
import pokecube.core.world.dimensions.PokecubeDimensionManager;

public class WorldProviderSecretBase extends WorldProvider
{
    public static final String PERMMINEOWNBASE   = "pokecube.secretbase.mine.own";
    public static final String PERMMINEOTHERBASE = "pokecube.secretbase.mine.other";

    public static int          DEFAULTSIZE       = 8;
    String                     owner;
    private static JEP         parser;

    public static void initPerms()
    {
        PermissionAPI.registerNode(PERMMINEOTHERBASE, DefaultPermissionLevel.OP,
                "Can the player mine blocks in someone else's secret base.");
        PermissionAPI.registerNode(PERMMINEOWNBASE, DefaultPermissionLevel.ALL,
                "Can the player mine blocks in their own secret base.");
    }

    public static void init(String function)
    {
        parser = new JEP();
        parser.initFunTab();
        parser.addStandardFunctions();
        parser.initSymTab(); // clear the contents of the symbol table
        parser.addStandardConstants();
        parser.addComplex();
        parser.addVariable("c", 0);
        parser.addVariable("k", 0);
        parser.addVariable("h", 0);
        parser.parseExpression(function);
    }

    public static void initToDefaults(WorldBorder border)
    {
        border.setCenter(0, 0);
        border.setSize(DEFAULTSIZE);
        border.setWarningDistance(1);
    }

    public WorldProviderSecretBase()
    {
    }

    @Override
    public DimensionType getDimensionType()
    {
        return PokecubeDimensionManager.SECRET_BASE_TYPE;
    }

    @Override
    public WorldBorder createWorldBorder()
    {
        WorldBorder ret = new WorldBorder();
        initToDefaults(ret);
        return ret;
    }

    @Override
    public IChunkGenerator createChunkGenerator()
    {
        return new ChunkProviderSecretBase(world);
    }

    /** Called when the world is performing a save. Only used to save the state
     * of the Dragon Boss fight in WorldProviderEnd in Vanilla. */
    @Override
    public void onWorldSave()
    {
        ISaveHandler saveHandler = world.getSaveHandler();
        File file = saveHandler.getWorldDirectory();
        file = new File(file, getSaveFolder());
        file = new File(file, "data" + File.separator + "worldInfo.dat");
        try
        {
            NBTTagCompound tag = new NBTTagCompound();
            int size = world.getWorldBorder().getSize();
            tag.setInteger("border", size);
            owner = PokecubeDimensionManager.getOwner(getDimension());
            if (owner != null && !owner.isEmpty()) tag.setString("owner", owner);
            FileOutputStream fileoutputstream = new FileOutputStream(file);
            CompressedStreamTools.writeCompressed(tag, fileoutputstream);
            fileoutputstream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void onWorldLoad()
    {
        ISaveHandler saveHandler = world.getSaveHandler();
        File file = saveHandler.getWorldDirectory();
        file = new File(file, getSaveFolder());
        file = new File(file, "data" + File.separator + "worldInfo.dat");
        if (file.exists())
        {
            try
            {
                FileInputStream fileinputstream = new FileInputStream(file);
                NBTTagCompound tag = CompressedStreamTools.readCompressed(fileinputstream);
                fileinputstream.close();
                owner = tag.getString("owner");
                int size = DEFAULTSIZE;
                if (!owner.isEmpty())
                {
                    try
                    {
                        UUID id = UUID.fromString(owner);
                        int c = CaptureStats.getNumberUniqueCaughtBy(id);
                        int k = KillStats.getNumberUniqueKilledBy(id);
                        int h = EggStats.getNumberUniqueHatchedBy(id);
                        parser.setVarValue("c", c);
                        parser.setVarValue("k", k);
                        parser.setVarValue("h", h);
                        size = (int) parser.getValue();
                        size = Math.min(size, PokecubeCore.core.getConfig().baseMaxSize * 16);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                world.getWorldBorder().setSize(size);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /** True if the player can respawn in this dimension (true = overworld,
     * false = nether). */
    @Override
    public boolean canRespawnHere()
    {
        return false;
    }

    /** Called when a Player is added to the provider's world. */
    @Override
    public void onPlayerAdded(EntityPlayerMP player)
    {
        if (!player.isDead) player.sendMessage(new TextComponentTranslation("pokecube.secretBase.enter"));
        owner = PokecubeDimensionManager.getOwner(getDimension());
        if (!owner.isEmpty())
        {
            try
            {
                int size = DEFAULTSIZE;
                UUID id = UUID.fromString(owner);
                int c = CaptureStats.getNumberUniqueCaughtBy(id);
                int k = KillStats.getNumberUniqueKilledBy(id);
                int h = EggStats.getNumberUniqueHatchedBy(id);
                parser.setVarValue("c", c);
                parser.setVarValue("k", k);
                parser.setVarValue("h", h);
                size = (int) parser.getValue();
                size = Math.min(size, PokecubeCore.core.getConfig().baseMaxSize * 16);
                world.getWorldBorder().setSize(size);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /** Called when a Player is removed from the provider's world. */
    @Override
    public void onPlayerRemoved(EntityPlayerMP player)
    {
        if (!player.isDead) player.sendMessage(new TextComponentTranslation("pokecube.secretBase.exit"));
    }

    @Override
    protected void init()
    {
        this.biomeProvider = new BiomeProviderSecretBase(world);
    }

    @Override
    public boolean canMineBlock(net.minecraft.entity.player.EntityPlayer player, BlockPos pos)
    {
        return PermissionAPI.hasPermission(player,
                player.getCachedUniqueIdString().equals(owner) ? PERMMINEOWNBASE : PERMMINEOTHERBASE);
    }

    /** Called to determine if the chunk at the given chunk coordinates within
     * the provider's world can be dropped. Used in WorldProviderSurface to
     * prevent spawn chunks from being unloaded. */
    @Override
    public boolean canDropChunk(int x, int z)
    {
        return !PokecubeCore.core.getConfig().basesLoaded || (x * x + z * z) > 2;
    }
}
