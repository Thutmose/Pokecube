/**
 *
 */
package pokecube.core;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.items.berries.BerryManager;

/** @author Manchou */
public class CreativeTabPokecubeBerries extends CreativeTabs
{
    /** @param par1
     * @param par2Str */
    public CreativeTabPokecubeBerries(int par1, String par2Str)
    {
        super(par1, par2Str);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack getIconItemStack()
    {
        return BerryManager.getBerryItem("null");
    }

    @Override
    public ItemStack getTabIconItem()
    {
        return BerryManager.getBerryItem("null");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getTabLabel()
    {
        return "Berries";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getTranslatedTabLabel()
    {
        return getTabLabel();
    }
}
