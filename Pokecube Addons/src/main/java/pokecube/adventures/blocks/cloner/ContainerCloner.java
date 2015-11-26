package pokecube.adventures.blocks.cloner;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.ItemPokemobEgg;
import pokecube.core.items.pokecubes.PokecubeManager;

public class ContainerCloner extends ContainerWorkbench {

	World worldObj;
	
	public ContainerCloner(InventoryPlayer inv, World world,
			int x, int y, int z) {
		super(inv, world, new BlockPos(x,y,z));
		worldObj = world;
	}
	
    /**
     * Callback for when the crafting matrix is changed.
     */
    public void onCraftMatrixChanged(IInventory p_75130_1_)
    {
    	ItemStack item = CraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.worldObj);
    	//System.out.println("test");
    	if(item!=null)
    	{
    		this.craftResult.setInventorySlotContents(0, item);
    		return;
    	}
    	else
    	{
    		ItemStack cube = null;
    		ItemStack egg = null;
    		ItemStack result = null;
    		boolean wrongnum = false;
    		for(int i = 0; i<craftMatrix.getSizeInventory(); i++)
    		{
    			item = craftMatrix.getStackInSlot(i);
    			if(item==null)
    				continue;
    			if(PokecubeManager.isFilled(item))
    			{
    				if(cube!=null)
    				{
    					wrongnum = true;
    					break;
    				}
    				cube = item.copy();
    				continue;
    			}
    			else if(item.getItem() instanceof ItemPokemobEgg)
    			{
    				if(egg!=null)
    				{
    					wrongnum = true;
    					break;
    				}
    				egg = item.copy();
    				continue;
    			}
				wrongnum = true;
				break;
    		}
    		if(!wrongnum && cube!=null && egg!=null)
    		{
    			item = egg;
    			int pokenb = PokecubeManager.getPokedexNb(cube);
    			PokedexEntry entry = Database.getEntry(pokenb);
    			pokenb = entry.getChildNb();
    			if(egg.getTagCompound() == null)
    				egg.setTagCompound(new NBTTagCompound());
    			egg.getTagCompound().setInteger("pokemobNumber", pokenb);
    			
    			IPokemob mob = PokecubeManager.itemToPokemob(cube, worldObj);
    			if(mob.isShiny() && egg.hasTagCompound())
    				egg.getTagCompound().setBoolean("shiny", true);
    			
    			
    			egg.stackSize = 1;
    			this.craftResult.setInventorySlotContents(0, egg);
    			return;
    		}
    		this.craftResult.setInventorySlotContents(0, null);
    	}
    }
    
    public boolean canInteractWith(EntityPlayer p)
    {
    	return true;
    }
}
