package pokecube.adventures.blocks.cloner;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockCloner extends ItemBlock {

	public ItemBlockCloner(Block par1) {
		super(par1);
        this.setHasSubtypes(true);
	}
	@Override
	public int getMetadata (int damageValue) {
		return damageValue;
	}
	   @Override
    @SideOnly(Side.CLIENT)
    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List)
    {
    	par3List.add(new ItemStack(par1, 1, 0));
    	par3List.add(new ItemStack(par1, 1, 1));
    }
	 
	 /**
	  * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
	  * different names based on their damage or NBT.
	  */
	 @Override
	public String getUnlocalizedName(ItemStack par1ItemStack)
	 {
	     int i = par1ItemStack.getItemDamage();
	     
	     return super.getUnlocalizedName() + "." + (i==1?"splicer":"reanimator");
	 }
}
