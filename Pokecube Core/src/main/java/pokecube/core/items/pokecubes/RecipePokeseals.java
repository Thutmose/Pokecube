package pokecube.core.items.pokecubes;

import java.util.ArrayList;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import pokecube.core.PokecubeItems;

public class RecipePokeseals implements IRecipe {
    private ItemStack toCraft;
 //   private static final String __OBFID = "CL_00000083";

    /**
     * Used to check if a recipe matches current crafting inventory
     */
    @SuppressWarnings({ "unused", "rawtypes" })
    @Override
	public boolean matches(InventoryCrafting craft, World world)
    {
        this.toCraft = null;
        int cube = 0;
        int paper = 0;
        int gunpowder = 0;
        int dye = 0;
        int fireworkcharge = 0;
        int sparklystuff = 0;
        int boomboomstuff = 0;
        int addons = 0;

        for (int k1 = 0; k1 < craft.getSizeInventory(); ++k1)
        {
            ItemStack itemstack = craft.getStackInSlot(k1);

            if (itemstack != null)
            {
            	if (itemstack.getItem() == PokecubeItems.getEmptyCube(-2) && PokecubeManager.isFilled(itemstack) == false)
            	{
            		++cube;
            	toCraft = itemstack.copy();
            	}
/*               if (itemstack.getItem() == Items.gunpowder)
                {
                    ++j;
                }
                else if (itemstack.getItem() == Items.firework_charge)
                {
                    ++l;
                }
*/                else if (itemstack.getItem() == Items.dye)
                {
                    ++addons;
                }
/*                else if (itemstack.getItem() == Items.paper)
                {
                    ++paper;
                }
*/                else if (itemstack.getItem() == Items.water_bucket)
                {
                    ++addons;
                }
                else if (itemstack.getItem() == Items.coal)
                {
                    ++addons;
                }
                else if (itemstack.getItem() == Item.getItemFromBlock(Blocks.leaves))
                {
                    ++addons;
                }
                else if (itemstack.getItem() == Items.feather)
                {
                    ++boomboomstuff;
                }
                else if (itemstack.getItem() == Items.gold_nugget)
                {
                    ++boomboomstuff;
                }
                else
                {
                    if (itemstack.getItem() != Items.skull)
                    {
                        return false;
                    }

                    ++boomboomstuff;
                }
            }
        }

        sparklystuff += dye + boomboomstuff;

       
            NBTTagCompound nbttagcompound;
            NBTTagCompound nbttagcompound1;

            if (cube == 1 && addons > 0)
            {
                toCraft = new ItemStack(PokecubeItems.getEmptyCube(-2), 1);
                nbttagcompound = new NBTTagCompound();
                nbttagcompound1 = new NBTTagCompound();
                byte b0 = 0;
                ArrayList arraylist = new ArrayList();

                for (int l1 = 0; l1 < craft.getSizeInventory(); ++l1)
                {
                    ItemStack itemstack2 = craft.getStackInSlot(l1);

                    if (itemstack2 != null)
                    {
                       if (itemstack2.getItem() == Items.coal)
                        {
                            nbttagcompound1.setBoolean("Flames", true);
                        }
                       if (itemstack2.getItem() == Items.water_bucket)
                        {
                            nbttagcompound1.setBoolean("Bubbles", true);
                        }
                       if (itemstack2.getItem() == Item.getItemFromBlock(Blocks.leaves))
                       {
                       	nbttagcompound1.setBoolean("Leaves", true);
                       }
                       if (itemstack2.getItem() == Items.dye)
                       {
                       	nbttagcompound1.setInteger("dye", itemstack2.getItemDamage());
                       }
                       
                    }
                }

                int[] aint1 = new int[arraylist.size()];

                for (int l2 = 0; l2 < aint1.length; ++l2)
                {
                    aint1[l2] = ((Integer)arraylist.get(l2)).intValue();
                }
                
   //             toCraft = ();
                nbttagcompound.setTag("Explosion", nbttagcompound1);
                this.toCraft.setTagCompound(nbttagcompound);
                return true;
            }
             else
            {
                return false;
            }
         }

    /**
     * Returns an Item that is the result of this recipe
     */
    @Override
	public ItemStack getCraftingResult(InventoryCrafting p_77572_1_)
    {
        return this.toCraft;
    }

    /**
     * Returns the size of the recipe area
     */
    @Override
	public int getRecipeSize()
    {
        return 10;
    }

    @Override
	public ItemStack getRecipeOutput()
    {
        return this.toCraft;
    }

	@Override
	public ItemStack[] getRemainingItems(InventoryCrafting inv) {
		ItemStack[] ret = new ItemStack[inv.getSizeInventory()];
		return ret;
	}
}
