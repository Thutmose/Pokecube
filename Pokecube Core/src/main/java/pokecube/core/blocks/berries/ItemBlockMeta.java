package pokecube.core.blocks.berries;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockMeta extends ItemBlock {

	Block b;
	String[] names;
	
	public ItemBlockMeta(Block p_i45328_1_) {
		super(p_i45328_1_);
		b = p_i45328_1_;
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
	}
	@Override
    /**
     * Returns the metadata of the block which this Item (ItemBlock) can place
     */
    public int getMetadata(int par1)
    {
        return par1;
    }
	@Override
    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
    public String getUnlocalizedName(ItemStack par1ItemStack)
    {
    	if(b instanceof IMetaBlock)
    	{
    		return ((IMetaBlock)b).getUnlocalizedName(par1ItemStack);
    	}
    	
        return this.b.getUnlocalizedName();
    }
	
}
