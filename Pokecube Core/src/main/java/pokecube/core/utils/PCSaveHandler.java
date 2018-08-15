package pokecube.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.UUID;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.blocks.pc.InventoryPC;
import pokecube.core.interfaces.PokecubeMod;
import thut.core.common.handlers.PlayerDataHandler;

public class PCSaveHandler
{
    private static PCSaveHandler instance;

    private static PCSaveHandler clientInstance;

    public static PCSaveHandler getInstance()
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
        {
            if (instance == null) instance = new PCSaveHandler();
            return instance;
        }
        if (clientInstance == null) clientInstance = new PCSaveHandler();
        return clientInstance;
    }

    public boolean seenPCCreator = false;

    public PCSaveHandler()
    {
    }

    public void loadPC(UUID uuid)
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) return;
        try
        {
            File file = PlayerDataHandler.getFileForUUID(uuid.toString(), "PCInventory");
            if (file != null && file.exists())
            {
                if (PokecubeMod.debug) PokecubeMod.log("Loading PC: " + uuid);
                FileInputStream fileinputstream = new FileInputStream(file);
                NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(fileinputstream);
                fileinputstream.close();
                readPcFromNBT(nbttagcompound.getCompoundTag("Data"));
            }
        }
        catch (FileNotFoundException e)
        {
        }
        catch (Exception e)
        {
        }
    }

    public void readPcFromNBT(NBTTagCompound nbt)
    {
        seenPCCreator = nbt.getBoolean("seenPCCreator");
        // Read PC Data from NBT
        NBTBase temp = nbt.getTag("PC");
        if (temp instanceof NBTTagList)
        {
            NBTTagList tagListPC = (NBTTagList) temp;
            InventoryPC.loadFromNBT(tagListPC);
        }
    }

    public void savePC(UUID uuid)
    {
        if (FMLCommonHandler.instance().getMinecraftServerInstance() == null
                || FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            return;
        try
        {
            File file = PlayerDataHandler.getFileForUUID(uuid.toString(), "PCInventory");
            if (file != null)
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                writePcToNBT(nbttagcompound, uuid);
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

    public void writePcToNBT(NBTTagCompound nbt, UUID uuid)
    {
        nbt.setBoolean("seenPCCreator", seenPCCreator);
        NBTTagList tagsPC = InventoryPC.saveToNBT(uuid);
        nbt.setTag("PC", tagsPC);
    }

}
