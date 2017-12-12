/**
 *
 */
package pokecube.core;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CreativeTabPokecubes extends CreativeTabs
{
    /** @param par1
     * @param par2Str */
    public CreativeTabPokecubes(int par1, String par2Str)
    {
        super(par1, par2Str);
    }

    /** the itemID for the item to be displayed on the tab */
    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack getTabIconItem()
    {
        ItemStack stack = PokecubeItems.getStack("pokecube");
        if (stack == null) stack = PokecubeItems.getStack("pokeseal");
        return stack;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getTabLabel()
    {
        return I18n.format("igwtab.entry.Pokecubes");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getTranslatedTabLabel()
    {
        return getTabLabel();
    }
}
