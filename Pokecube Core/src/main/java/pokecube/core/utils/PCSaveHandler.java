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
import net.minecraft.world.World;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.blocks.pc.InventoryPC;

public class PCSaveHandler 
{
    private static PCSaveHandler instance;
    
    private static PCSaveHandler clientInstance;
    public static PCSaveHandler getInstance() 
    {
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
        {
            if(instance==null)
                instance = new PCSaveHandler();
            return instance;
        }
        if(clientInstance==null)
            clientInstance = new PCSaveHandler();
        return clientInstance;
    }
    public boolean seenPCCreator = false;
    
    private ISaveHandler saveHandler;
    
    public PCSaveHandler()
    {
    }
    
    public void loadPC()
    {
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) return;
        
        try
        {
            
            World world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(0);
            saveHandler = world.getSaveHandler();
            File file = saveHandler.getMapFileFromName("PCInventory");
            File upperDir = new File(file.getParentFile().getAbsolutePath());
            for(File f:upperDir.listFiles())
            {
                if(f.isDirectory())
                {
                    String s = f.getName();
                    try
                    {
                        UUID.fromString(s);
                        loadPC(s);
                    }
                    catch (Exception e)
                    {
                        
                    }
                }
            }
            if (file != null && file.exists())
            {
                FileInputStream fileinputstream = new FileInputStream(file);
                NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(fileinputstream);
                fileinputstream.close();
                readPcFromNBT(nbttagcompound.getCompoundTag("Data"));
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }
    
    public void loadPC(String uuid)
    {
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) return;
        try
        {
            World world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(0);
            saveHandler = world.getSaveHandler();
            String seperator = System.getProperty("file.separator");
            File file = saveHandler.getMapFileFromName(uuid+seperator+"PCInventory");
            if (file != null && file.exists())
            {
                FileInputStream fileinputstream = new FileInputStream(file);
                NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(fileinputstream);
                fileinputstream.close();
                readPcFromNBT(nbttagcompound.getCompoundTag("Data"));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void readPcFromNBT(NBTTagCompound nbt)
    {
        seenPCCreator = nbt.getBoolean("seenPCCreator");
        //Read PC Data from NBT
        NBTBase temp = nbt.getTag("PC");
        if(temp instanceof NBTTagList)
        {
            NBTTagList tagListPC = (NBTTagList) temp;
            InventoryPC.loadFromNBT(tagListPC);
        }
    }
    
    public void savePC()
    {
        if(FMLCommonHandler.instance().getMinecraftServerInstance()==null)return;
        
        try {
            
            World world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(0);
            saveHandler = world.getSaveHandler();
            File file = saveHandler.getMapFileFromName("PCInventory");
            if (file != null)
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                writePcToNBT(nbttagcompound);
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setTag("Data", nbttagcompound);
                FileOutputStream fileoutputstream = new FileOutputStream(file);
                CompressedStreamTools.writeCompressed(nbttagcompound1, fileoutputstream);
                fileoutputstream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void savePC(String uuid)
    {
        
        if(FMLCommonHandler.instance().getMinecraftServerInstance()==null)return;
        
        try {
            
            World world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(0);
            saveHandler = world.getSaveHandler();
            String seperator = System.getProperty("file.separator");
            File file = saveHandler.getMapFileFromName(uuid+seperator+"PCInventory");

            File dir = new File(file.getParentFile().getAbsolutePath());
            if(file!=null && !file.exists())
            {
                dir.mkdirs();
            }
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void writePcToNBT(NBTTagCompound nbt)
    {
        nbt.setBoolean("seenPCCreator", seenPCCreator);
        NBTTagList tagsPC = InventoryPC.saveToNBT();
        nbt.setTag("PC", tagsPC);
        
    }
    
    public void writePcToNBT(NBTTagCompound nbt, String uuid)
    {
        nbt.setBoolean("seenPCCreator", seenPCCreator);
        NBTTagList tagsPC = InventoryPC.saveToNBT(uuid);
        nbt.setTag("PC", tagsPC);
    }
    
}
