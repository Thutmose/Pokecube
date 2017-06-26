/**
 *
 */
package pokecube.core;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** @author Manchou */
public class CreativeTabPokecubeBlocks extends CreativeTabs
{
    /** @param par1
     * @param par2Str */
    public CreativeTabPokecubeBlocks(int par1, String par2Str)
    {
        super(par1, par2Str);
    }

    /** the itemID for the item to be displayed on the tab */
    @Override
    @SideOnly(Side.CLIENT)
    public Item getTabIconItem()
    {
        return Item.getItemFromBlock(PokecubeItems.pokecenter);
    }

    @SideOnly(Side.CLIENT)
    private ItemStack iconItemStack;

    @SideOnly(Side.CLIENT)
    public ItemStack getIconItemStack()
    {
        if (super.getIconItemStack().getItem() == null)
        {
            if (this.iconItemStack == null)
            {
                this.iconItemStack = new ItemStack(this.getTabIconItem(), 1, this.getIconItemDamage());
            }
            if (iconItemStack == null || iconItemStack.getItem() == null)
            {
                Thread.dumpStack();
                return new ItemStack(Items.STONE_AXE);
            }
            return iconItemStack;
        }
        return super.getIconItemStack();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getTabLabel()
    {
        return "Pok\u00e9cube Blocks";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getTranslatedTabLabel()
    {
        return getTabLabel();
    }
}
