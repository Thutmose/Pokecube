package pokecube.adventures.handlers;

import java.util.HashMap;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.network.PacketPokeAdv.MessageClient;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.Tools;

public class PlayerAsPokemobManager
{
	private static PlayerAsPokemobManager instance;
	private static PlayerAsPokemobManager instance2;

	private HashMap<String, NBTTagCompound> playerData = new HashMap<String, NBTTagCompound>();
	
	private PlayerAsPokemobManager(){}

	public static PlayerAsPokemobManager getInstance()
	{
		Side side = FMLCommonHandler.instance().getEffectiveSide();
		if(side==Side.CLIENT)
		{
			if(instance2==null)
				instance2 = new PlayerAsPokemobManager();
			return instance2;
		}
		
		if(instance==null)
			instance = new PlayerAsPokemobManager();
		return instance;
	}
	
	public IPokemob getTransformed(EntityPlayer player)
	{
		if(!playerData.containsKey(player.getName()))
			return null;
		NBTTagCompound tag = playerData.get(player.getName());
		int num = tag.getInteger("PokedexNb");
		   if(num!=0)
		   {
			   IPokemob pokemob = (IPokemob) PokecubeMod.core.createEntityByPokedexNb(num, player.worldObj);
		        if(pokemob==null)
		        {
		        	return null;
		        }
			   Entity poke = (Entity) pokemob;
			   NBTTagCompound pokeTag = tag.getCompoundTag("Pokemob");
			   poke.readFromNBT(pokeTag);
		       pokemob.popFromPokecube();// should reinit status
	           ((EntityLivingBase) pokemob).setHealth(Tools.getHealth((int) ((EntityLivingBase) pokemob).getMaxHealth(), PokecubeMod.FULL_HEALTH));
	           ((EntityLivingBase) pokemob).extinguish();
			   return pokemob;
		   }
		   
		   return null;
	}
	
	public void setPlayerTransform(EntityPlayer player, IPokemob pokemob)
	{
		if(pokemob==null)
		{
			playerData.remove(player.getName());
			return;
		}
		
		
		ItemStack stack = PokecubeManager.pokemobToItem(pokemob);
		playerData.put(player.getName(), stack.getTagCompound());
	}
	
	public void setData(String playerName, NBTTagCompound data)
	{
		if(!data.hasKey("Pokemob"))
		{
			playerData.remove(playerName);
		}
		else
		{
			playerData.put(playerName, data);
		}
	}
	
	public NBTTagCompound getData(EntityPlayer player)
	{
		return playerData.get(player.getName());
	}
	
	public void sendClientUpdatePacket(EntityPlayer sendTo, String infoToSend)
	{
		NBTTagCompound tag = playerData.get(infoToSend);
		if(tag==null || !(sendTo instanceof EntityPlayerMP))
		{
			if(sendTo instanceof EntityPlayerMP)
			{
				tag = new NBTTagCompound();
				tag.setString("playername", infoToSend);
				PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
				buffer.writeByte(8);
				try
				{
					buffer.writeNBTTagCompoundToBuffer(tag);
					MessageClient message = new MessageClient(buffer);
					PokecubeMod.packetPipeline.sendTo(message, (EntityPlayerMP) sendTo);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			return;
		}
		tag.setString("playername", infoToSend);
		PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
		buffer.writeByte(8);
		try
		{
			buffer.writeNBTTagCompoundToBuffer(tag);
			MessageClient message = new MessageClient(buffer);
			PokecubeMod.packetPipeline.sendTo(message, (EntityPlayerMP) sendTo);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void sendClientUpdates(EntityPlayer sendTo)
	{
		for(String s: playerData.keySet())
		{
			sendClientUpdatePacket(sendTo, s);
		}
	}
	
	public static void copyEntity(EntityLivingBase from, EntityLivingBase to)
	{
		to.lastTickPosX = from.lastTickPosX;
		to.lastTickPosY = from.lastTickPosY;
		to.lastTickPosZ = from.lastTickPosZ;
		
		to.chunkCoordX = from.chunkCoordX;
		to.chunkCoordY = from.chunkCoordY;
		to.chunkCoordZ = from.chunkCoordZ;
		
		to.motionX = from.motionX;
		to.motionY = from.motionY;
		to.motionZ = from.motionZ;
		
		to.posX = from.posX;
		to.posY = from.posY;
		to.posZ = from.posZ;
		
		to.rotationPitch = from.rotationPitch;
		to.rotationYaw = from.rotationYaw;
		to.rotationYawHead = from.rotationYawHead;
		
		to.prevCameraPitch = from.prevCameraPitch;
		to.prevRotationPitch = from.prevRotationPitch;
		to.prevRotationYaw = from.prevRotationYaw;
		to.prevRenderYawOffset = from.prevRenderYawOffset;
		to.prevRotationYawHead = from.prevRotationYawHead;
		
		to.renderYawOffset = from.renderYawOffset;
		
		to.dimension = from.dimension;
		
		to.swingProgress = from.swingProgress;
		to.swingProgressInt = from.swingProgressInt;
		to.limbSwing = from.limbSwing;
		to.limbSwingAmount = from.limbSwingAmount;
		to.prevLimbSwingAmount = from.prevLimbSwingAmount;
	}
}
