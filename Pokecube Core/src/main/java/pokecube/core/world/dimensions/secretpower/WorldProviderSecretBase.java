package pokecube.core.world.dimensions.secretpower;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.storage.ISaveHandler;
import pokecube.core.world.dimensions.PokecubeDimensionManager;

public class WorldProviderSecretBase extends WorldProvider
{

    public WorldProviderSecretBase()
    {
        // TODO Auto-generated constructor stub
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
        ret.setCenter(0, 0);
        ret.setSize(32);
        return ret;
    }

    @Override
    public IChunkGenerator createChunkGenerator()
    {
        return new ChunkProviderSecretBase(worldObj);
    }

    /** Called when the world is performing a save. Only used to save the state
     * of the Dragon Boss fight in WorldProviderEnd in Vanilla. */
    @Override
    public void onWorldSave()
    {
        // TODO save owner here?
        ISaveHandler saveHandler = worldObj.getSaveHandler();
        File file = saveHandler.getWorldDirectory();
        file = new File(file, getSaveFolder());
        file = new File(file, "data" + File.separator + "worldInfo.dat");
        try
        {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("border", worldObj.getWorldBorder().getSize());
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
        ISaveHandler saveHandler = worldObj.getSaveHandler();
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
                worldObj.getWorldBorder().setSize(tag.getInteger("border"));
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
    }

    /** Called when a Player is removed from the provider's world. */
    @Override
    public void onPlayerRemoved(EntityPlayerMP player)
    {
    }

    @Override
    protected void createBiomeProvider()
    {
        this.biomeProvider = new BiomeProviderSecretBase(worldObj);
    }
}
