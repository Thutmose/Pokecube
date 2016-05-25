package pokecube.core.blocks.berries;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBerryLeaf extends ItemBlock
{
    public ItemBerryLeaf(Block block)
    {
        super(block);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    /** Returns the unlocalized name of this item. This version accepts an
     * ItemStack so different stacks can have different names based on their
     * damage or NBT. */
    public String getUnlocalizedName(ItemStack stack)
    {
        return stack.getTagCompound().getString("berryId");
    }
}
