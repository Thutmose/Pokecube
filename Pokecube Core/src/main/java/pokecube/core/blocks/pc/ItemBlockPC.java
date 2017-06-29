package pokecube.core.blocks.pc;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockPC extends ItemBlock
{

    public ItemBlockPC(Block par1)
    {
        super(par1);
        this.setHasSubtypes(true);
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
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
        if (tab != getCreativeTab()) return;
        subItems.add(new ItemStack(itemIn, 1, 0));
        subItems.add(new ItemStack(itemIn, 1, 8));
    }

    /** Returns the unlocalized name of this item. This version accepts an
     * ItemStack so different stacks can have different names based on their
     * damage or NBT. */
    @Override
    public String getUnlocalizedName(ItemStack par1ItemStack)
    {
        int i = par1ItemStack.getItemDamage();

        return super.getUnlocalizedName() + "." + (i == 8 ? "top" : "base");
    }
}
