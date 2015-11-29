package pokecube.adventures.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.UUID;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.items.bags.InventoryBag;

public class PASaveHandler 
{
    private int lastId;
    
    private ISaveHandler saveHandler;
    private static PASaveHandler instance;
    private static PASaveHandler clientInstance;
    public HashMap<Integer, EntityTrainer> trainers = new HashMap<Integer, EntityTrainer>();
    
    private PASaveHandler()
    {
    	lastId = 0;
    }
    
	public static PASaveHandler getInstance() 
	{
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
		{
			if(instance==null)
				instance = new PASaveHandler();
			return instance;
		}
		if(clientInstance==null)
			clientInstance = new PASaveHandler();
		return clientInstance;
	}
    
    public void saveBag()
    {
    	
    	if(FMLCommonHandler.instance().getMinecraftServerInstance()==null)return;
    	
		
        try {
        	
    		World world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(0);
    		saveHandler = world.getSaveHandler();
    		File file = saveHandler.getMapFileFromName("BagInventory");
			if (file != null)
			{
//			    NBTTagCompound nbttagcompound = new NBTTagCompound();
//			    writeBagToNBT(nbttagcompound);
//			    NBTTagCompound nbttagcompound1 = new NBTTagCompound();
//			    nbttagcompound1.setTag("Data", nbttagcompound);
//			    FileOutputStream fileoutputstream = new FileOutputStream(file);
//			    CompressedStreamTools.writeCompressed(nbttagcompound1, fileoutputstream);
//			    fileoutputstream.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public void saveBag(String uuid)
    {
    	
    	if(FMLCommonHandler.instance().getMinecraftServerInstance()==null)return;
//    	if(true)
//    	{
//    		savePC();
//    		return;
//    	}
        try {
        	
    		World world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(0);
    		saveHandler = world.getSaveHandler();
        	String seperator = System.getProperty("file.separator");
    		File file = saveHandler.getMapFileFromName(uuid+seperator+"BagInventory");

    		File dir = new File(file.getParentFile().getAbsolutePath());
    		if(file!=null && !file.exists())
    		{
    			dir.mkdirs();
    		}
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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public void saveTeams()
    {
    	
    	if(FMLCommonHandler.instance().getMinecraftServerInstance()==null)return;
    	
		
        try {
        	
    		World world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(0);
    		saveHandler = world.getSaveHandler();
    		File file = saveHandler.getMapFileFromName("PokecubeTeams");
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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
   
    public void loadBag()
    {
    	if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) return;
    	
        try
        {
    		World world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(0);
    		saveHandler = world.getSaveHandler();
    		File file = saveHandler.getMapFileFromName("BagInventory");
    		File upperDir = new File(file.getParentFile().getAbsolutePath());
    		for(File f:upperDir.listFiles())
    		{
    			if(f.isDirectory())
    			{
    				String s = f.getName();
    				try
					{
						UUID.fromString(s);
						loadBag(s);
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
                readBagFromNBT(nbttagcompound.getCompoundTag("Data"));
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }
    
    public void loadBag(String uuid)
    {
    	if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) return;
        try
        {
    		World world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(0);
    		saveHandler = world.getSaveHandler();
        	String seperator = System.getProperty("file.separator");
    		File file = saveHandler.getMapFileFromName(uuid+seperator+"BagInventory");
            if (file != null && file.exists())
            {
                FileInputStream fileinputstream = new FileInputStream(file);
                NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(fileinputstream);
                fileinputstream.close();
                readBagFromNBT(nbttagcompound.getCompoundTag("Data"));
            }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public void loadTeams()
    {
    	if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) return;
    	
        try
        {
        	
    		World world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(0);
    		saveHandler = world.getSaveHandler();
    		File file = saveHandler.getMapFileFromName("PokecubeTeams");
            if (file != null && file.exists())
            {
                FileInputStream fileinputstream = new FileInputStream(file);
                NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(fileinputstream);
                fileinputstream.close();
                TeamManager.getInstance().loadFromNBT(nbttagcompound.getCompoundTag("Data"));
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }
  
    public void readBagFromNBT(NBTTagCompound nbt)
    {
        //Read PC Data from NBT
        NBTBase temp = nbt.getTag("PC");
        if(temp instanceof NBTTagList)
        {
	        NBTTagList tagListPC = (NBTTagList) temp;
	        InventoryBag.loadFromNBT(tagListPC);
        }
    }
    
    public void writeBagToNBT(NBTTagCompound nbt)
    {
        NBTTagList tagsPC = InventoryBag.saveToNBT();
        nbt.setTag("PC", tagsPC);
        
    }
    
    //TODO call this to send data to clients
    public void writeBagToNBT(NBTTagCompound nbt, String uuid)
    {
        NBTTagList tagsPC = InventoryBag.saveToNBT(uuid);
       
        nbt.setTag("PC", tagsPC);
        
    }
    
    public int getNewId()
    {
        lastId++;
        return lastId;
    }
    
}
