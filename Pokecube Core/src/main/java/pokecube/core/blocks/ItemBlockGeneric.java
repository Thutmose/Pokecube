package pokecube.core.blocks;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockGeneric extends ItemBlock
{

    public ItemBlockGeneric(Block par1)
    {
        super(par1);
        this.setRegistryName(par1.getRegistryName());
    }

    @Override
    public int getMetadata(int damageValue)
    {
        return damageValue;
    }

    @Override
    @SideOnly(Side.CLIENT)
    /** returns a list of items with the same ID, but different meta (eg: dye
     * returns 16 items) */
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems)
    {
        if (tab != getCreativeTab()) return;
        subItems.add(new ItemStack(this, 1, 0));
    }
}
