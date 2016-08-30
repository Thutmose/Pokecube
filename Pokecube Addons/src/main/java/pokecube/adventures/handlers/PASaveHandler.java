package pokecube.adventures.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.items.bags.InventoryBag;
import pokecube.core.utils.PokecubeSerializer;

public class PASaveHandler
{
    private static PASaveHandler instance;

    private static PASaveHandler clientInstance;

    public static PASaveHandler getInstance()
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
        {
            if (instance == null) instance = new PASaveHandler();
            return instance;
        }
        if (clientInstance == null) clientInstance = new PASaveHandler();
        return clientInstance;
    }

    public HashMap<Integer, EntityTrainer> trainers = new HashMap<Integer, EntityTrainer>();

    private PASaveHandler()
    {
    }

    public void loadBag(String uuid)
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) return;
        try
        {
            File file = PokecubeSerializer.getFileForUUID(uuid, "BagInventory");
            if (file != null && file.exists())
            {
                FileInputStream fileinputstream = new FileInputStream(file);
                NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(fileinputstream);
                fileinputstream.close();
                readBagFromNBT(nbttagcompound.getCompoundTag("Data"));
            }
        }
        catch (FileNotFoundException e)
        {
        }
        catch (Exception e)
        {
        }
    }

    public void loadTeams()
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) return;

        try
        {
            World world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(0);
            File file = world.getSaveHandler().getMapFileFromName("PokecubeTeams");
            if (file != null && file.exists())
            {
                FileInputStream fileinputstream = new FileInputStream(file);
                NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(fileinputstream);
                fileinputstream.close();

                boolean old = !nbttagcompound.hasKey("VERSION");
                if (old) TeamManager.getInstance().loadFromNBTOld(nbttagcompound.getCompoundTag("Data"));
                else TeamManager.getInstance().loadFromNBT(nbttagcompound.getCompoundTag("Data"));
            }
            File upperDir = new File(file.getParentFile().getAbsolutePath());
            File dir = new File(upperDir, "PokeTeams");
            if (dir.exists() && dir.isDirectory())
            {
                for (File f : dir.listFiles())
                {
                    try
                    {
                        FileInputStream fileinputstream = new FileInputStream(f);
                        NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(fileinputstream);
                        fileinputstream.close();
                        TeamManager.getInstance().loadTeamFromNBT(nbttagcompound.getCompoundTag("Data"));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public void readBagFromNBT(NBTTagCompound nbt)
    {
        // Read PC Data from NBT
        NBTBase temp = nbt.getTag("PC");
        if (temp instanceof NBTTagList)
        {
            NBTTagList tagListPC = (NBTTagList) temp;
            InventoryBag.loadFromNBT(tagListPC);
        }
    }

    public void saveBag(String uuid)
    {

        if (FMLCommonHandler.instance().getMinecraftServerInstance() == null) return;
        try
        {
            File file = PokecubeSerializer.getFileForUUID(uuid, "BagInventory");
            if (file != null)
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                writeBagToNBT(nbttagcompound, uuid);
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setTag("Data", nbttagcompound);
                FileOutputStream fileoutputstream = new FileOutputStream(file);
                CompressedStreamTools.writeCompressed(nbttagcompound1, fileoutputstream);
                fileoutputstream.close();
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void saveTeams(String team)
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) return;

        try
        {
            World world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(0);
            if (team == null)
            {
                File file = world.getSaveHandler().getMapFileFromName("PokecubeTeams");
                if (file != null)
                {
                    NBTTagCompound nbttagcompound = new NBTTagCompound();
                    TeamManager.getInstance().saveToNBT(nbttagcompound);
                    NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                    nbttagcompound1.setTag("Data", nbttagcompound);
                    FileOutputStream fileoutputstream = new FileOutputStream(file);
                    CompressedStreamTools.writeCompressed(nbttagcompound1, fileoutputstream);
                    fileoutputstream.close();
                }
            }
            else
            {
                File file = world.getSaveHandler().getMapFileFromName("PokeTeams" + File.separator + team);
                File dir = new File(file.getParentFile().getAbsolutePath());
                if (file != null && !file.exists())
                {
                    dir.mkdirs();
                }
                if (file != null)
                {
                    NBTTagCompound nbttagcompound = new NBTTagCompound();
                    TeamManager.getInstance().saveTeamToNBT(team, nbttagcompound);
                    NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                    nbttagcompound1.setTag("Data", nbttagcompound);
                    FileOutputStream fileoutputstream = new FileOutputStream(file);
                    CompressedStreamTools.writeCompressed(nbttagcompound1, fileoutputstream);
                    fileoutputstream.close();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void writeBagToNBT(NBTTagCompound nbt)
    {
        NBTTagList tagsPC = InventoryBag.saveToNBT();
        nbt.setTag("PC", tagsPC);

    }

    // TODO call this to send data to clients
    public void writeBagToNBT(NBTTagCompound nbt, String uuid)
    {
        NBTTagList tagsPC = InventoryBag.saveToNBT(uuid);

        nbt.setTag("PC", tagsPC);

    }

}
