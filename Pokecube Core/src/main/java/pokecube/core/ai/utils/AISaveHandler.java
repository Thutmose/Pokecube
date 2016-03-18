package pokecube.core.ai.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.UUID;

import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;

public class AISaveHandler
{

	public static class PokemobAI
	{
		
		
		
		
		public void readEntityFromNBT(NBTTagCompound nbttagcompound)
		{

		}

		public void writeEntityToNBT(NBTTagCompound nbttagcompound)
		{

		}

	}
	private static final String	POKEAI	= "pokecubeAI";

	private static final String	DATA	= "data";

	private static AISaveHandler instance;
	public static void clearInstance()
	{
		if (instance != null) instance.saveData();
		instance = null;
	}

	public static AISaveHandler instance()
	{
		if (instance == null) instance = new AISaveHandler();
		return instance;
	}

	ISaveHandler				saveHandler;

	HashMap<UUID, PokemobAI>	aiMap	= new HashMap<UUID, PokemobAI>();

	private AISaveHandler()
	{
		saveHandler = PokecubeCore.proxy.getWorld().getSaveHandler();
		loadData();
	}

	public PokemobAI getAI(IPokemob entityAiPokemob)
	{
		if (!entityAiPokemob.getPokemonAIState(IMoveConstants.TAMED)) { return new PokemobAI(); }
		EntityLiving entity = (EntityLiving) entityAiPokemob;
		if (aiMap.containsKey(entity.getUniqueID()))
		{
			return aiMap.get(entity.getUniqueID());
		}
		else
		{
			aiMap.put(entity.getUniqueID(), new PokemobAI());
			return aiMap.get(entity.getUniqueID());
		}
	}

	public void loadData()
	{
		if (saveHandler != null)
		{
			try
			{
				File file = saveHandler.getMapFileFromName(POKEAI);

				if (file != null && file.exists())
				{
					FileInputStream fileinputstream = new FileInputStream(file);
					NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(fileinputstream);
					fileinputstream.close();
					readFromNBT(nbttagcompound.getCompoundTag(DATA));
				}
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
		}
	}

	private void readFromNBT(NBTTagCompound nbttagcompound)
	{

		NBTBase temp = nbttagcompound.getTag("entityMemories");
		if (temp instanceof NBTTagList)
		{
			NBTTagList list = (NBTTagList) temp;
			for (int i = 0; i < list.tagCount(); i++)
			{
				NBTTagCompound tag = list.getCompoundTagAt(i);
				String s = tag.getString("name");
				NBTTagCompound aiTag = tag.getCompoundTag("value");
				PokemobAI ai = new PokemobAI();
				ai.readEntityFromNBT(aiTag);
				if (!s.isEmpty())
				{
					try
					{
						aiMap.put(UUID.fromString(s), ai);
					}
					catch (Exception e)
					{
					}
				}
			}
		}
	}

	public void removeAI(EntityLiving pokemob)
	{
		if (aiMap.containsKey(pokemob.getUniqueID()))
		{
			aiMap.remove(pokemob.getUniqueID());
		}
	}

	private void saveData()
	{
		if (saveHandler == null || FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) { return; }

		try
		{
			File file = saveHandler.getMapFileFromName(POKEAI);
			if (file != null)
			{
				NBTTagCompound nbttagcompound = new NBTTagCompound();
				writeToNBT(nbttagcompound);
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setTag(DATA, nbttagcompound);
				FileOutputStream fileoutputstream = new FileOutputStream(file);
				CompressedStreamTools.writeCompressed(nbttagcompound1, fileoutputstream);
				fileoutputstream.close();
			}
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

	private void writeToNBT(NBTTagCompound nbttagcompound)
	{
		NBTTagList people = new NBTTagList();
		for (UUID u : aiMap.keySet())
		{
			NBTTagCompound tag = new NBTTagCompound();
			NBTTagCompound aiTag = new NBTTagCompound();
			aiMap.get(u).writeEntityToNBT(aiTag);
			tag.setTag("value", aiTag);
			tag.setString("name", u.toString());
			people.appendTag(tag);
		}
		nbttagcompound.setTag("entityMemories", people);
	}
}
