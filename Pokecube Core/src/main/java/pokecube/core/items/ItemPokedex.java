/**
 *
 */
package pokecube.core.items;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import pokecube.core.mod_Pokecube;
import pokecube.core.blocks.healtable.BlockHealTable;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.PokecubeClientPacket;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.Vector4;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

/**
 * @author Manchou
 *
 */
public class ItemPokedex extends ItemTranslated
{
    public ItemPokedex()
    {
        super();
    }

    private void showGui(EntityPlayer player)
    {
        if (mod_Pokecube.isOnClientSide())
        {
            //entityplayer.openGui(mod_Pokecube.instance, Mod_Pokecube_Helper.GUIPOKEDEX_ID, entityplayer.worldObj, 0, 0, 0);
        }
        else
        {
        	NBTTagCompound nbt = new NBTTagCompound();
        	StatsCollector.writeToNBT(nbt);
        	//
    		NBTTagCompound tag = new NBTTagCompound();
    		TerrainManager.getInstance().getTerrainForEntity(player).saveToNBT(tag);
//            PokecubePacket packet = PokecubePacketHandler.makePacket((byte) 5, tag);
//            PokecubePacketHandler.sendToClient(packet, player);
            
        	nbt.setBoolean("hasTerrain", true);
        	nbt.setTag("terrain", tag);
        	
        	PokecubeClientPacket packet = new PokecubeClientPacket(PokecubePacketHandler.CHANNEL_ID_STATS, nbt);
        	PokecubePacketHandler.sendToClient(packet, player);
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player)
    {

    	if(world.isRemote)
    		return itemstack;
//    	System.out.println(player);
    	if(!player.isSneaking())
    	{
    		showGui(player);
    		return itemstack;
    	}
        
        return itemstack;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
    	Vector3 hit = Vector3.getNewVectorFromPool().set(pos);
    	Vector3 playerLoc = Vector3.getNewVectorFromPool().set(playerIn);
    	Block block = hit.getBlockState(worldIn).getBlock();
    	if(block instanceof BlockHealTable)
    	{
    		if(worldIn.isRemote)
    			playerIn.addChatMessage(new ChatComponentText("Pokecenter set as a Teleport Location"));
    		Vector4 loc = new Vector4(playerIn);
    		loc.y++;
    		PokecubeSerializer.getInstance().setTeleport(loc, playerIn.getUniqueID().toString());
    		PokecubeSerializer.getInstance().save();
    		if(!worldIn.isRemote)
    		{
				NBTTagCompound teletag = new NBTTagCompound();
				PokecubeSerializer.getInstance().writePlayerTeleports(playerIn.getUniqueID(), teletag);
				
	        	PokecubeClientPacket packet = new PokecubeClientPacket((byte)8, teletag);
	        	PokecubePacketHandler.sendToClient(packet, playerIn);
    		}
    		hit.freeVectorFromPool();
    		playerLoc.freeVectorFromPool();
    		return true;
    	}
    	
    	if(playerIn.isSneaking()&&worldIn.isRemote)
    	{
    		playerIn.addChatMessage(new ChatComponentText("There are "+Database.spawnables.size()+" Registered Spawns"));
    		playerIn.addChatMessage(new ChatComponentText("There are "+Pokedex.getInstance().getEntries().size()+" Registered Pokemon"));
    		TerrainSegment t = TerrainManager.getInstance().getTerrian(worldIn, hit);
    		int b = t.getBiome(hit);
    		playerIn.addChatMessage(new ChatComponentText(SpawnHandler.spawnLists.get(b)+" Spawn here"));
    	}
    	
    	hit.freeVectorFromPool();
    	playerLoc.freeVectorFromPool();
    	
    	if(!playerIn.isSneaking())
    		showGui(playerIn);
        return super.onItemUse(stack, playerIn, worldIn, pos, side, hitX, hitY, hitZ);
    }
    
}
